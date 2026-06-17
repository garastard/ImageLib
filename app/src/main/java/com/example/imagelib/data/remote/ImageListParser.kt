package com.example.imagelib.data.remote

import com.example.imagelib.data.remote.dto.ImageDto
import org.json.JSONArray

object ImageListParser {
    fun parse(json: String): List<ImageDto> {
        val array = JSONArray(json)
        val items = ArrayList<ImageDto>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(
                ImageDto(
                    id = obj.getInt("id"),
                    imageUrl = obj.getString("imageUrl")
                )
            )
        }
        return items
    }
}