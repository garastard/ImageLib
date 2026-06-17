package com.example.imageloader.cache

import com.example.imageloader.Clock
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.concurrent.TimeUnit

class FileDiskCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class FakeClock(var nowMillis: Long) : Clock {
        override fun now(): Long = nowMillis
    }

    private fun newCache(clock: Clock): FileDiskCache {
        val policy = CachePolicy(TimeUnit.HOURS.toMillis(4), clock)
        return FileDiskCache(tempFolder.newFolder("cache"), policy, clock)
    }

    @Test
    fun `stored bytes are returned within ttl`() {
        val clock = FakeClock(0)
        val cache = newCache(clock)
        val data = byteArrayOf(1, 2, 3, 4, 5)

        cache.put("key", data)
        clock.nowMillis = TimeUnit.HOURS.toMillis(3)

        assertArrayEquals(data, cache.get("key"))
    }

    @Test
    fun `entry expires after ttl`() {
        val clock = FakeClock(0)
        val cache = newCache(clock)

        cache.put("key", byteArrayOf(1, 2, 3))
        clock.nowMillis = TimeUnit.HOURS.toMillis(4) + 1

        assertNull(cache.get("key"))
    }

    @Test
    fun `missing key returns null`() {
        val cache = newCache(FakeClock(0))
        assertNull(cache.get("absent"))
    }

    @Test
    fun `remove deletes a single entry`() {
        val clock = FakeClock(0)
        val cache = newCache(clock)
        cache.put("key", byteArrayOf(9))

        cache.remove("key")

        assertNull(cache.get("key"))
    }

    @Test
    fun `clear removes all entries`() {
        val clock = FakeClock(0)
        val cache = newCache(clock)
        cache.put("a", byteArrayOf(1))
        cache.put("b", byteArrayOf(2))

        cache.clear()

        assertNull(cache.get("a"))
        assertNull(cache.get("b"))
    }
}