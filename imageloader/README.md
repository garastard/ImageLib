# ImageLoader

Lightweight image **downloading and caching** library for Android, written in
Kotlin with Coroutines and **no third-party dependencies** (no Glide/Picasso/Coil,
no OkHttp, no JSON libs). Java-friendly public API.

- **Version:** `1.0.0`
- **Coordinates:** `com.example.imageloader:imageloader:1.0.0`
- **minSdk:** 24 · **compileSdk:** 36
- **License:** Apache-2.0

## Tech stack

- **Language:** Kotlin 2.0.21
- **Concurrency:** Kotlin Coroutines (`kotlinx-coroutines-android` 1.9.0)
- **Networking:** `java.net.HttpURLConnection` (JDK - no OkHttp)
- **Caching:** `android.util.LruCache` (memory) + plain files (disk)
- **Decoding:** `android.graphics.BitmapFactory` with `inSampleSize`
- **Build / publish:** AGP 8.10.0, Gradle 8.11.1, `maven-publish` + Dokka (javadoc)
- **Runtime dependencies:** only `androidx-core-ktx` and Coroutines - no
  image/HTTP/JSON third-party libraries

## Install

The library publishes a standard Android AAR (with sources & javadoc).

```kotlin
// settings.gradle.kts -> dependencyResolutionManagement { repositories { mavenLocal(); google(); mavenCentral() } }

// app/build.gradle.kts
dependencies {
    implementation("com.example.imageloader:imageloader:1.0.0")
}
```

Publish to your local Maven for testing:

```bash
./gradlew :imageloader:publishToMavenLocal
```

In this repository the example `:app` consumes the module directly via
`implementation(project(":imageloader"))`.

The `INTERNET` permission is declared by the library manifest and merged into
the consuming app automatically.

## Usage

### Kotlin

```kotlin
ImageLoader.getInstance(context)
    .load(url)
    .placeholder(R.drawable.ic_placeholder) // Drawable or @DrawableRes
    .error(R.drawable.ic_error)             // shown if the load fails
    .into(imageView)
```

### Java

The public API has no suspend functions, so it reads the same from Java:

```java
ImageLoader.getInstance(context)
    .load(url)
    .placeholder(R.drawable.ic_placeholder)
    .error(R.drawable.ic_error)
    .into(imageView);
```

### Cache invalidation

The disk cache is valid for **4 hours**; entries are re-fetched afterwards.
Invalidate manually at any time:

```kotlin
ImageLoader.getInstance(context).invalidateCache() // clears memory + disk
```

### Custom configuration

Defaults cover the common case; override any collaborator via the builder
(useful for tests or special requirements):

```kotlin
val loader = ImageLoader.Builder(context)
    .cacheTtlMillis(TimeUnit.HOURS.toMillis(1))
    .logging(true)                 // Logcat tracing under the "ImageLoader" tag
    .downloader(myDownloader)      // custom ImageDownloader
    .decoder(myDecoder)            // custom BitmapDecoder
    .memoryCache(myMemoryCache)    // custom MemoryCache
    .diskCache(myDiskCache)        // custom DiskCache
    .build()
```

## How it works

Each `load(...).into(view)` resolves through three layers, falling through on a miss:

```
load(url) ──► memory cache ──► disk cache ──► network (HttpURLConnection)
                  │                │                 │
              LruCache         raw bytes        downloaded bytes
              (+ 4h TTL)       (+ 4h TTL)       stored to disk, then decoded
```

1. **Memory cache** (`LruMemoryCache`) - `LruCache<String, Bitmap>` sized to a
   fraction of the heap; a memory hit is applied synchronously. Each entry
   carries a timestamp and honours the TTL.
2. **Disk cache** (`FileDiskCache`) - stores the **original downloaded bytes**
   (no re-encoding) as `[8-byte timestamp][bytes]`. The embedded timestamp makes
   the 4-hour TTL robust and testable. Writes are atomic (temp file + rename).
3. **Network** (`HttpImageDownloader`) - plain `HttpURLConnection` on
   `Dispatchers.IO`, honouring coroutine cancellation while streaming.

Other behaviours:

- **Downsampling** - decoded via `inSampleSize` to the target `ImageView` size
  (waiting for layout if the view is not measured yet) to avoid `OutOfMemoryError`.
- **RecyclerView-safe** - the in-flight request is tagged on the view; rebinding
  cancels the previous request and a finished request only applies its bitmap if
  the view is still bound to it.
- **Request coalescing** - concurrent requests for the same URL share a single
  download.

## Architecture (SOLID)

The facade depends only on small interfaces, all injectable via `Builder`:

| Interface         | Default                | Responsibility                    |
|-------------------|------------------------|-----------------------------------|
| `ImageDownloader` | `HttpImageDownloader`  | fetch raw bytes over HTTP         |
| `BitmapDecoder`   | `DefaultBitmapDecoder` | decode + downsample bytes         |
| `MemoryCache`     | `LruMemoryCache`       | fast in-memory layer (+ TTL)      |
| `DiskCache`       | `FileDiskCache`        | persistent bytes layer (+ TTL)    |
| `Target`          | `ImageViewTarget`      | where the result is delivered     |

`CachePolicy` (with an injectable `Clock`) is the single source of truth for the
4-hour TTL, shared by both cache layers.

## Testing

```bash
./gradlew :imageloader:testDebugUnitTest
```

Unit tests cover the TTL policy (`CachePolicyTest`) and the disk-cache lifecycle
including expiry (`FileDiskCacheTest`).