package com.example.imagelib.domain.usecase

import com.example.imagelib.domain.model.ImageResource
import com.example.imagelib.domain.repository.ImageRepository
import javax.inject.Inject

class GetImagesUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    suspend operator fun invoke(): List<ImageResource> = repository.getImages()
}