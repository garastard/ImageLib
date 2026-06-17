package com.example.imageloader.cache

import java.security.MessageDigest

internal object CacheKeys {
    fun of(url: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}