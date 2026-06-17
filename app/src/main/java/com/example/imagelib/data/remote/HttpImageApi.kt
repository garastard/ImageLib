package com.example.imagelib.data.remote

import com.example.imagelib.data.remote.dto.ImageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * [ImageApi] implementation over [HttpURLConnection] (no third-party HTTP/JSON
 * library), running on [Dispatchers.IO].
 */
class HttpImageApi @Inject constructor() : ImageApi {

    override suspend fun fetchImageList(): List<ImageDto> = withContext(Dispatchers.IO) {
        val connection = (URL(LIST_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            requestMethod = "GET"
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("Unexpected HTTP status $code while loading image list")
            }
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            ImageListParser.parse(json)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val LIST_URL =
            "https://zipoapps-storage-test.nyc3.digitaloceanspaces.com/image_list.json"
    }
}