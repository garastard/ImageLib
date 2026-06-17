package com.example.imageloader.cache

import com.example.imageloader.Clock
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

class FileDiskCache(
    private val directory: File,
    private val policy: CachePolicy,
    private val clock: Clock = Clock.SYSTEM
) : DiskCache {

    private val lock = Any()

    init {
        if (!directory.exists()) directory.mkdirs()
    }

    override fun get(key: String): ByteArray? = synchronized(lock) {
        val file = fileFor(key)
        if (!file.exists()) return null
        return try {
            DataInputStream(file.inputStream().buffered()).use { input ->
                val createdAt = input.readLong()
                if (!policy.isFresh(createdAt)) {
                    file.delete()
                    return null
                }
                input.readBytes()
            }
        } catch (e: Exception) {
            file.delete()
            null
        }
    }

    override fun put(key: String, bytes: ByteArray): Unit = synchronized(lock) {
        val target = fileFor(key)
        val tmp = File(directory, "${key}.tmp")
        try {
            DataOutputStream(tmp.outputStream().buffered()).use { output ->
                output.writeLong(clock.now())
                output.write(bytes)
            }
            if (!tmp.renameTo(target)) {
                tmp.copyTo(target, overwrite = true)
                tmp.delete()
            }
        } catch (e: Exception) {
            tmp.delete()
        }
    }

    override fun remove(key: String): Unit = synchronized(lock) {
        fileFor(key).delete()
    }

    override fun clear(): Unit = synchronized(lock) {
        directory.listFiles()?.forEach { it.delete() }
    }

    private fun fileFor(key: String): File = File(directory, key)
}