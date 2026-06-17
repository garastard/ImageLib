package com.example.imageloader

import android.graphics.drawable.Drawable
import android.widget.ImageView

class ImageRequest internal constructor(
    val url: String,
    val placeholder: Drawable?,
    val error: Drawable?,
    val target: Target,
    val imageView: ImageView?
)