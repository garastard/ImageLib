package com.example.imagelib.domain.usecase

import com.example.imagelib.domain.repository.ImageCacheManager
import javax.inject.Inject

class InvalidateImageCacheUseCase @Inject constructor(
    private val cacheManager: ImageCacheManager
) {
    operator fun invoke() = cacheManager.invalidate()
}