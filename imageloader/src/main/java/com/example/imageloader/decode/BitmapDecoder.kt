package com.example.imageloader.decode

import android.graphics.Bitmap

interface BitmapDecoder {
    fun decode(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap?
}