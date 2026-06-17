package com.example.imagelib.data.repository

import com.example.imagelib.data.mapper.toDomain
import com.example.imagelib.data.remote.ImageApi
import com.example.imagelib.domain.model.ImageResource
import com.example.imagelib.domain.repository.ImageRepository
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val api: ImageApi
) : ImageRepository {

    override suspend fun getImages(): List<ImageResource> =
        api.fetchImageList().map { it.toDomain() }
}