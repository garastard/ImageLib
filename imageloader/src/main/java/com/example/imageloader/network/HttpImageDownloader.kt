package com.example.imageloader.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/**
 * [ImageDownloader] built on [HttpURLConnection] - no third-party HTTP library.
 * Runs on [Dispatchers.IO] and honours coroutine cancellation while streaming.
 */
class HttpImageDownloader(
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000
) : ImageDownloader {

    override suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = connectTimeoutMillis
                readTimeout = readTimeoutMillis
                requestMethod = "GET"
                instanceFollowRedirects = true
            }

            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("Unexpected HTTP status $code for $url")
            }

            connection.inputStream.buffered().use { input ->
                val buffer = ByteArray(16 * 1024)
                val output = java.io.ByteArrayOutputStream()
                while (true) {
                    coroutineContext.ensureActive()
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            }
        } finally {
            connection?.disconnect()
        }
    }
}