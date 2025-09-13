package dev.slne.surf.cloud.standalone.player.cache

import com.google.common.util.concurrent.Striped
import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.key.CacheNetworkKey
import dev.slne.surf.cloud.api.common.player.cache.key.PlayerCacheKeyRegistry
import dev.slne.surf.cloud.core.common.player.cache.AbstractCloudPlayerCache
import dev.slne.surf.cloud.core.common.player.cache.CacheOperation
import dev.slne.surf.cloud.core.common.player.cache.CacheOperationResult
import dev.slne.surf.cloud.core.common.player.cache.ChangeCounter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.withLock

class ServerPlayerCache(uuid: UUID) : AbstractCloudPlayerCache(uuid, ChangeCounter()) {
    companion object {
        private val locks = Striped.lock(64)
    }

    private val entries = ConcurrentHashMap<CacheNetworkKey, Entry>()

    suspend fun applyOperationInternal(
        key: CacheKey<*>,
        eVersion: Long,
        op: CacheOperation
    ): CacheOperationResult {
        val nKey = key.toNetworkKey()
        locks.get(uuid).withLock {
            when (op) {
                is CacheOperation.ValueSet<*> -> applyValueSet(nKey, op, eVersion)

                is CacheOperation.ListOperation.Append<*> -> applyListAppend(nKey, op, eVersion)
                is CacheOperation.ListOperation.Insert<*> -> TODO()
                is CacheOperation.ListOperation.Set<*> -> TODO()
                is CacheOperation.ListOperation.RemoveAt -> TODO()
                CacheOperation.ListOperation.Clear -> TODO()

                CacheOperation.MapOperation.Clear -> TODO()
                is CacheOperation.MapOperation.Put<*, *> -> TODO()
                is CacheOperation.MapOperation.Remove<*> -> TODO()
                is CacheOperation.SetOperation.Add<*> -> TODO()

                CacheOperation.SetOperation.Clear -> TODO()
                is CacheOperation.SetOperation.Remove<*> -> TODO()

                is CacheOperation.StructuredDelta<*> -> TODO()
            }
        }

    }

    private fun applyValueSet(
        key: CacheNetworkKey,
        op: CacheOperation.ValueSet<*>,
        expectedVersion: Long
    ): CacheOperationResult {
        val entry = entries[key]
        val newValue = op.value

        if (newValue != null) {
            if (entry == null) {
                if (expectedVersion != 0L) { // The new entry must have version 0
                    return CacheOperationResult.VersionMismatch
                } else {
                    entries[key] = Entry(newValue).also { it.incrementVersion() }
                    return CacheOperationResult.Success(1)
                }
            } else {
                if (entry.version() != expectedVersion) {
                    return CacheOperationResult.VersionMismatch
                } else {
                    entry.value = newValue
                    val newVersion = entry.incrementVersion()
                    return CacheOperationResult.Success(newVersion)
                }
            }
        } else {
            if (entry == null) {
                return if (expectedVersion != 0L) { // The non-existing entry must have version 0
                    CacheOperationResult.VersionMismatch
                } else {
                    CacheOperationResult.Success(0) // No change, but success
                }
            } else {
                if (entry.version() != expectedVersion) {
                    return CacheOperationResult.VersionMismatch
                } else {
                    entries.remove(key)
                    return CacheOperationResult.Success(entry.incrementVersion())
                }
            }
        }
    }

    private fun applyListAppend(
        key: CacheNetworkKey,
        op: CacheOperation.ListOperation.Append<*>,
        expectedVersion: Long
    ): CacheOperationResult {
        val entry = entries[key]
        op.elements

        if (entry == null) {
            if (expectedVersion != 0L) { // The new entry must have version 0
                return CacheOperationResult.VersionMismatch
            } else {

            }
        }

    }

    fun exportData(): List<Pair<CacheNetworkKey, Entry>> {

    }

    class Entry(initial: Any?) {
        private val version = AtomicLong(0)

        @Volatile
        var value: Any? = initial

        @Volatile
        var lastAccessNanos: Long = System.nanoTime()

        fun version() = version.get()
        fun incrementVersion() = version.incrementAndGet()

        fun valueBytes(keyRegistry: PlayerCacheKeyRegistry): ByteArray {
        }
    }
}