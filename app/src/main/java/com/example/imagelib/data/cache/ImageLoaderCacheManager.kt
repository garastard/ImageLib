package com.example.imagelib.data.cache

import com.example.imagelib.domain.repository.ImageCacheManager
import com.example.imageloader.ImageLoader
import javax.inject.Inject

class ImageLoaderCacheManager @Inject constructor(
    private val imageLoader: ImageLoader
) : ImageCacheManager {

    override fun invalidate() {
        imageLoader.invalidateCache()
    }
}