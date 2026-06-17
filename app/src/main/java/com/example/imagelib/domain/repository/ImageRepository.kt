package com.example.imagelib.domain.repository

import com.example.imagelib.domain.model.ImageResource

interface ImageRepository {
    suspend fun getImages(): List<ImageResource>
}