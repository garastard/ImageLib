package com.example.imageloader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

interface Target {
    fun onLoading(placeholder: Drawable?)
    fun onSuccess(bitmap: Bitmap)
    fun onError(error: Drawable?, cause: Throwable)
}