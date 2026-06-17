package com.example.imagelib.data.repository

import com.example.imagelib.data.remote.ImageApi
import com.example.imagelib.data.remote.dto.ImageDto
import com.example.imagelib.domain.model.ImageResource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageRepositoryImplTest {

    private class FakeImageApi(private val dtos: List<ImageDto>) : ImageApi {
        override suspend fun fetchImageList(): List<ImageDto> = dtos
    }

    @Test
    fun `maps dtos to domain model`() = runTest {
        val api = FakeImageApi(
            listOf(
                ImageDto(0, "https://example.com/a.jpg"),
                ImageDto(7, "https://example.com/b.jpg")
            )
        )

        val result = ImageRepositoryImpl(api).getImages()

        assertEquals(
            listOf(
                ImageResource(0, "https://example.com/a.jpg"),
                ImageResource(7, "https://example.com/b.jpg")
            ),
            result
        )
    }
}