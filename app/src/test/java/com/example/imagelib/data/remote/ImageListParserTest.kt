package com.example.imagelib.data.remote

import com.example.imagelib.data.remote.dto.ImageDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageListParserTest {

    @Test
    fun `parses id and imageUrl from the expected json shape`() {
        val json = """
            [
              { "id": 0, "imageUrl": "https://example.com/a.jpg" },
              { "id": 1, "imageUrl": "https://example.com/b.jpg" }
            ]
        """.trimIndent()

        val items = ImageListParser.parse(json)

        assertEquals(2, items.size)
        assertEquals(ImageDto(0, "https://example.com/a.jpg"), items[0])
        assertEquals(ImageDto(1, "https://example.com/b.jpg"), items[1])
    }

    @Test
    fun `empty array yields empty list`() {
        assertEquals(emptyList<ImageDto>(), ImageListParser.parse("[]"))
    }
}