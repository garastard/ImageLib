package com.example.imageloader.network

import java.io.IOException

interface ImageDownloader {
    @Throws(IOException::class)
    suspend fun download(url: String): ByteArray
}