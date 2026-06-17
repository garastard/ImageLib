package com.example.imagelib.data.mapper

import com.example.imagelib.data.remote.dto.ImageDto
import com.example.imagelib.domain.model.ImageResource

fun ImageDto.toDomain(): ImageResource = ImageResource(id = id, url = imageUrl)