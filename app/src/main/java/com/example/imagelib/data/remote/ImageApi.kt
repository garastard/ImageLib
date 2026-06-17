package com.example.imagelib.data.remote

import com.example.imagelib.data.remote.dto.ImageDto

interface ImageApi {
    suspend fun fetchImageList(): List<ImageDto>
}