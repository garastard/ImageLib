package com.example.imageloader

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Сhainable request configuration. Kept free of suspend functions and
 * coroutine types so the whole public API is callable from Java as well.
 *
 *     ImageLoader.with(context)
 *         .load(url)
 *         .placeholder(R.drawable.placeholder)
 *         .error(R.drawable.error)
 *         .into(imageView)
 */
class RequestBuilder internal constructor(
    private val loader: ImageLoader,
    private val context: Context,
    private val url: String
) {
    private var placeholder: Drawable? = null
    private var error: Drawable? = null

    fun placeholder(drawable: Drawable?): RequestBuilder = apply { this.placeholder = drawable }

    fun placeholder(@DrawableRes resId: Int): RequestBuilder = apply {
        this.placeholder = ContextCompat.getDrawable(context, resId)
    }

    fun error(drawable: Drawable?): RequestBuilder = apply { this.error = drawable }

    fun error(@DrawableRes resId: Int): RequestBuilder = apply {
        this.error = ContextCompat.getDrawable(context, resId)
    }

    /** Loads into an [ImageView]; cancels any previous request bound to it. */
    fun into(imageView: ImageView) {
        val request = ImageRequest(url, placeholder, error, ImageViewTarget(imageView), imageView)
        loader.enqueue(request)
    }

    /** Loads into a custom [Target]. */
    fun into(target: Target) {
        val request = ImageRequest(url, placeholder, error, target, imageView = null)
        loader.enqueue(request)
    }
}