# ImageLib

An Android image-loading **library** built from scratch (no Glide/Picasso/Coil)
plus an **example app** that consumes it to display a grid of images fetched from
a remote JSON list.

## Modules

```
:imageloader   reusable image downloading & caching library (Kotlin, Java-friendly)
:app           example app - Android Views, MVVM + Clean Architecture, Coroutines
```

## Library - `:imageloader`

Downloads, decodes (with downsampling), and caches images in two layers - an
in-memory `LruCache` and a file-based disk cache - with a **4-hour TTL** and
manual invalidation. Built on Coroutines and `HttpURLConnection`, with no
third-party dependencies. Fluent, Java-callable API:

```kotlin
ImageLoader.getInstance(context)
    .load(url)
    .placeholder(R.drawable.ic_placeholder)
    .error(R.drawable.ic_error)
    .into(imageView)
```

Full details, usage and architecture: **[imageloader/README.md](imageloader/README.md)**.

**Not yet released to Maven.** 
The module is fully publish ready - `maven-publish` 
Is configured with coordinates `com.example.imageloader:imageloader:1.0.0`, sources & javadoc artifacts and POM metadata

But it is not pushed to a public repository yet! 

The example `:app` consumes it directly via `implementation(project(":imageloader"))`
you can also run `./gradlew :imageloader:publishToMavenLocal` to install the AAR locally.

## Example app - `:app`

A deliberately small but faithful slice of a full **MVVM + Clean Architecture**
app - the same layering you'd use in production, scaled down to one screen:

```
ui (Views)         MainActivity + RecyclerView, ViewBinding, observes UiState
   │  StateFlow
presentation       MainViewModel (Hilt, viewModelScope) - no Android Context
   │  use cases
domain             ImageResource model, repository/cache interfaces, use cases
   │  interfaces
data               ImageApi (HttpURLConnection) + DTO + mapper,
                   ImageRepositoryImpl, ImageLoaderCacheManager
   │
di                 Hilt modules wiring interfaces → implementations
```

- **Views, not Compose** - `RecyclerView` (2-column grid) with `ViewBinding`.
- Each cell shows the loaded image (placeholder while loading) and its `id`.
- An **Invalidate cache** button clears the library cache on tap.
- The JSON list is fetched at runtime and parsed with `org.json` (no third-party
  JSON library); image loading is delegated to `:imageloader`.

The layering (domain interfaces, use cases, DI-injected ViewModel) is full-size;
only the breadth - one screen, a couple of use cases - is compact.

## Tech stack

- **Language:** Kotlin 2.0.21
- **Async:** Kotlin Coroutines + `StateFlow`
- **UI:** Android Views, ViewBinding, Material 3, RecyclerView, ConstraintLayout,
  SwipeRefreshLayout (no Jetpack Compose)
- **Architecture:** MVVM + Clean Architecture (domain / data / di layers)
- **DI:** Hilt 2.52 (+ KSP)
- **Lifecycle:** AndroidX Lifecycle ViewModel
- **Networking / JSON:** `HttpURLConnection` + `org.json` (no Retrofit/OkHttp/Gson)
- **Image pipeline:** the in-house `:imageloader` library
- **Build:** AGP 8.10.0, Gradle 8.11.1, version catalog · **minSdk** 24 · **compileSdk** 36
- **Tests:** JUnit4, kotlinx-coroutines-test

## Build, run & test

```bash
./gradlew :app:assembleDebug                 # build the example app
./gradlew :imageloader:publishToMavenLocal   # publish the library AAR locally
./gradlew :imageloader:testDebugUnitTest :app:testDebugUnitTest
```

Unit tests cover the cache TTL and disk lifecycle, JSON parsing, and the
repository DTO→domain mapping.