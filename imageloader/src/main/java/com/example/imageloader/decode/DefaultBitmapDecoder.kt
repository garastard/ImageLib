package com.example.imageloader.decode

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class DefaultBitmapDecoder : BitmapDecoder {

    override fun decode(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, bounds)

        val options = BitmapFactory.Options().apply {
            inSampleSize = computeSampleSize(bounds.outWidth, bounds.outHeight, reqWidth, reqHeight)
        }
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    private fun computeSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        if (reqWidth <= 0 || reqHeight <= 0 || width <= 0 || height <= 0) return 1
        var sampleSize = 1
        while (width / (sampleSize * 2) >= reqWidth && height / (sampleSize * 2) >= reqHeight) {
            sampleSize *= 2
        }
        return sampleSize
    }
}