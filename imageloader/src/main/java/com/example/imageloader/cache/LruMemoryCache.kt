package com.example.imageloader.cache

import android.graphics.Bitmap
import android.util.LruCache

class LruMemoryCache(
    maxSizeBytes: Int,
    private val policy: CachePolicy
) : MemoryCache {

    private class Entry(val bitmap: Bitmap, val createdAt: Long)

    private val lru = object : LruCache<String, Entry>(maxSizeBytes) {
        override fun sizeOf(key: String, value: Entry): Int = value.bitmap.byteCount
    }

    override fun get(key: String): Bitmap? {
        val entry = lru.get(key) ?: return null
        if (!policy.isFresh(entry.createdAt)) {
            lru.remove(key)
            return null
        }
        return entry.bitmap
    }

    override fun put(key: String, bitmap: Bitmap) {
        lru.put(key, Entry(bitmap, System.currentTimeMillis()))
    }

    override fun remove(key: String) {
        lru.remove(key)
    }

    override fun clear() {
        lru.evictAll()
    }

    companion object {
        fun defaultSizeBytes(): Int =
            (Runtime.getRuntime().maxMemory() / 8).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}