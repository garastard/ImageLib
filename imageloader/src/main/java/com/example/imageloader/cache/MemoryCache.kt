package com.example.imageloader.cache

import android.graphics.Bitmap

interface MemoryCache {
    fun get(key: String): Bitmap?
    fun put(key: String, bitmap: Bitmap)
    fun remove(key: String)
    fun clear()
}