package com.example.imageloader

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.example.imageloader.cache.CachePolicy
import com.example.imageloader.cache.CacheKeys
import com.example.imageloader.cache.DiskCache
import com.example.imageloader.cache.FileDiskCache
import com.example.imageloader.cache.LruMemoryCache
import com.example.imageloader.cache.MemoryCache
import com.example.imageloader.decode.BitmapDecoder
import com.example.imageloader.decode.DefaultBitmapDecoder
import com.example.imageloader.network.HttpImageDownloader
import com.example.imageloader.network.ImageDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.coroutines.coroutineContext

/**
 * Public entry point of the image-loading library.
 *
 * Pipeline for each request: memory cache -> disk cache -> network. Results from
 * disk/network are decoded off the main thread and promoted into
 * the faster caches. The whole public surface avoids suspend functions so it is
 * equally usable from Java.
 *
 * All collaborators ([ImageDownloader], [MemoryCache], [DiskCache],
 * [BitmapDecoder]) are injected behind interfaces (DIP) and can be replaced via
 * [Builder] without changing this class (OCP).
 */
class ImageLoader internal constructor(
    context: Context,
    private val downloader: ImageDownloader,
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache,
    private val decoder: BitmapDecoder,
    private val loggingEnabled: Boolean = false
) {
    private val appContext = context.applicationContext

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val inFlightLock = Any()
    private val inFlight = HashMap<String, Deferred<ByteArray>>()

    fun load(url: String): RequestBuilder = RequestBuilder(this, appContext, url)

    fun invalidateCache() {
        memoryCache.clear()
        scope.launch(Dispatchers.IO) { diskCache.clear() }
        log { "cache invalidated (all)" }
    }

    private inline fun log(message: () -> String) {
        if (loggingEnabled) Log.d(TAG, message())
    }

    private fun logError(message: String, throwable: Throwable?) {
        if (loggingEnabled) Log.e(TAG, message, throwable)
    }

    internal fun enqueue(request: ImageRequest) {
        val imageView = request.imageView
        imageView?.let { cancelExisting(it) }

        request.target.onLoading(request.placeholder)
        log { "load: ${request.url}" }

        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                val key = CacheKeys.of(request.url)
                val cached = memoryCache.get(key)
                val bitmap = if (cached != null) {
                    log { "memory hit: ${request.url}" }
                    cached
                } else {
                    val (reqWidth, reqHeight) = resolveTargetSize(imageView)
                    loadAndDecode(key, request.url, reqWidth, reqHeight)
                }
                if (isStillBound(imageView)) {
                    request.target.onSuccess(bitmap)
                } else {
                    log { "skipped (view recycled): ${request.url}" }
                }
            } catch (cancellation: kotlinx.coroutines.CancellationException) {
                log { "cancelled: ${request.url}" }
                throw cancellation
            } catch (error: Throwable) {
                logError("load failed: ${request.url}", error)
                if (isStillBound(imageView)) {
                    request.target.onError(request.error, error)
                }
            } finally {
                val view = imageView
                if (view != null &&
                    view.getTag(R.id.imageloader_request_tag) === coroutineContext[Job]
                ) {
                    view.setTag(R.id.imageloader_request_tag, null)
                }
            }
        }
        imageView?.setTag(R.id.imageloader_request_tag, job)
        job.start()
    }

    private suspend fun loadAndDecode(
        key: String,
        url: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        val bitmap = withContext(Dispatchers.IO) {
            val cachedBytes = diskCache.get(key)
            val bytes = if (cachedBytes != null) {
                log { "disk hit: $url" }
                cachedBytes
            } else {
                downloadShared(key, url)
            }
            decoder.decode(bytes, reqWidth, reqHeight)
        } ?: throw IOException("Unable to decode image: $url")

        memoryCache.put(key, bitmap)
        log { "decoded: $url -> ${bitmap.width}x${bitmap.height} (target ${reqWidth}x${reqHeight})" }
        return bitmap
    }

    private suspend fun downloadShared(key: String, url: String): ByteArray {
        val deferred = synchronized(inFlightLock) {
            inFlight[key] ?: scope.async(Dispatchers.IO) {
                log { "downloading: $url" }
                val startedAt = System.nanoTime()
                val bytes = downloader.download(url)
                diskCache.put(key, bytes)
                val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
                log { "downloaded: $url (${bytes.size} bytes in ${elapsedMs} ms)" }
                bytes
            }.also { started ->
                inFlight[key] = started
                started.invokeOnCompletion {
                    synchronized(inFlightLock) { inFlight.remove(key) }
                }
            }
        }
        return deferred.await()
    }

    private suspend fun isStillBound(imageView: ImageView?): Boolean {
        if (imageView == null) return true
        return imageView.getTag(R.id.imageloader_request_tag) === coroutineContext[Job]
    }

    private fun cancelExisting(imageView: ImageView) {
        (imageView.getTag(R.id.imageloader_request_tag) as? Job)?.cancel()
    }

    private suspend fun resolveTargetSize(imageView: ImageView?): Pair<Int, Int> {
        if (imageView == null) return screenSize()

        if (imageView.width > 0 && imageView.height > 0) {
            return imageView.width to imageView.height
        }

        return suspendCancellableCoroutine { continuation ->
            val observer = imageView.viewTreeObserver
            val listener = object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    val width = imageView.width
                    val height = imageView.height
                    if (width > 0 && height > 0) {
                        removeListener(imageView, observer, this)
                        if (continuation.isActive) continuation.resume(width to height) {}
                    }
                    return true
                }
            }
            observer.addOnPreDrawListener(listener)
            continuation.invokeOnCancellation { removeListener(imageView, observer, listener) }
        }
    }

    private fun removeListener(
        imageView: ImageView,
        fallback: ViewTreeObserver,
        listener: ViewTreeObserver.OnPreDrawListener
    ) {
        val observer = imageView.viewTreeObserver.takeIf { it.isAlive } ?: fallback
        if (observer.isAlive) observer.removeOnPreDrawListener(listener)
    }

    private fun screenSize(): Pair<Int, Int> {
        val metrics = appContext.resources.displayMetrics
        return metrics.widthPixels to metrics.heightPixels
    }

    class Builder(context: Context) {
        private val appContext = context.applicationContext
        private var downloader: ImageDownloader = HttpImageDownloader()
        private var decoder: BitmapDecoder = DefaultBitmapDecoder()
        private var ttlMillis: Long = CachePolicy.DEFAULT_TTL_MILLIS
        private var memoryCache: MemoryCache? = null
        private var diskCache: DiskCache? = null
        private var diskDirectory: File = File(appContext.cacheDir, "image_loader_cache")
        private var loggingEnabled: Boolean = false

        fun downloader(downloader: ImageDownloader) = apply { this.downloader = downloader }
        fun decoder(decoder: BitmapDecoder) = apply { this.decoder = decoder }
        fun cacheTtlMillis(ttlMillis: Long) = apply { this.ttlMillis = ttlMillis }
        fun memoryCache(cache: MemoryCache) = apply { this.memoryCache = cache }
        fun diskCache(cache: DiskCache) = apply { this.diskCache = cache }
        fun diskDirectory(directory: File) = apply { this.diskDirectory = directory }

        /** Enables Logcat tracing of the request lifecycle under the "ImageLoader" tag. */
        fun logging(enabled: Boolean) = apply { this.loggingEnabled = enabled }

        fun build(): ImageLoader {
            val policy = CachePolicy(ttlMillis)
            val memory = memoryCache
                ?: LruMemoryCache(LruMemoryCache.defaultSizeBytes(), policy)
            val disk = diskCache
                ?: FileDiskCache(diskDirectory, policy)
            return ImageLoader(appContext, downloader, memory, disk, decoder, loggingEnabled)
        }
    }

    companion object {
        private const val TAG = "ImageLoader"

        @Volatile
        private var instance: ImageLoader? = null

        @JvmStatic
        fun getInstance(context: Context): ImageLoader {
            return instance ?: synchronized(this) {
                instance ?: Builder(context).build().also { instance = it }
            }
        }
    }
}