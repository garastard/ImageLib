package com.example.imageloader.cache

import com.example.imageloader.Clock
import java.util.concurrent.TimeUnit

class CachePolicy(
    private val ttlMillis: Long,
    private val clock: Clock = Clock.SYSTEM
) {
    fun isFresh(createdAtMillis: Long): Boolean =
        clock.now() - createdAtMillis < ttlMillis

    companion object {
        val DEFAULT_TTL_MILLIS: Long = TimeUnit.HOURS.toMillis(4)
    }
}