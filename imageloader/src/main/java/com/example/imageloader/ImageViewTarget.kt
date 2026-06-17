package com.example.imageloader

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import java.lang.ref.WeakReference


internal class ImageViewTarget(imageView: ImageView) : Target {

    private val viewRef = WeakReference(imageView)

    override fun onLoading(placeholder: Drawable?) {
        viewRef.get()?.setImageDrawable(placeholder)
    }

    override fun onSuccess(bitmap: Bitmap) {
        viewRef.get()?.setImageBitmap(bitmap)
    }

    override fun onError(error: Drawable?, cause: Throwable) {
        if (error != null) viewRef.get()?.setImageDrawable(error)
    }
}