package com.example.imageloader.cache

import com.example.imageloader.Clock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class CachePolicyTest {

    private class FakeClock(var nowMillis: Long) : Clock {
        override fun now(): Long = nowMillis
    }

    @Test
    fun `entry is fresh within ttl`() {
        val clock = FakeClock(0)
        val policy = CachePolicy(TimeUnit.HOURS.toMillis(4), clock)

        val createdAt = 0L
        clock.nowMillis = TimeUnit.HOURS.toMillis(3)

        assertTrue(policy.isFresh(createdAt))
    }

    @Test
    fun `entry is stale after ttl`() {
        val clock = FakeClock(0)
        val policy = CachePolicy(TimeUnit.HOURS.toMillis(4), clock)

        val createdAt = 0L
        clock.nowMillis = TimeUnit.HOURS.toMillis(4) + 1

        assertFalse(policy.isFresh(createdAt))
    }

    @Test
    fun `entry is stale exactly at ttl boundary`() {
        val clock = FakeClock(0)
        val policy = CachePolicy(TimeUnit.HOURS.toMillis(4), clock)

        clock.nowMillis = TimeUnit.HOURS.toMillis(4)

        assertFalse(policy.isFresh(0L))
    }

    @Test
    fun `default ttl is four hours`() {
        assertTrue(CachePolicy.DEFAULT_TTL_MILLIS == TimeUnit.HOURS.toMillis(4))
    }
}