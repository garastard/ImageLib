package com.example.imageloader.cache

interface DiskCache {
    fun get(key: String): ByteArray?
    fun put(key: String, bytes: ByteArray)
    fun remove(key: String)
    fun clear()
}