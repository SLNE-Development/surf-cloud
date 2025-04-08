@file:Suppress("OPT_IN_USAGE")

package dev.slne.surf.cloud.api.server.jpa.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import dev.slne.surf.cloud.api.server.exposed.service.DirectCacheAccess
import kotlinx.coroutines.*
import org.jetbrains.annotations.ApiStatus
import kotlin.coroutines.CoroutineContext

abstract class AbstractSpringJpaService<K, V>(
    cacheCustomizer: Caffeine<Any, Any>.() -> Unit = {},
    private val context: CoroutineContext = Dispatchers.IO
) {

    /**
     * Cache for storing loaded entities, using Caffeine with coroutine-based loading.
     */
    @DirectCacheAccess
    protected val cache = Caffeine.newBuilder()
        .apply(cacheCustomizer)
        .run {
            val cacheName = CoroutineName("${this::class.simpleName} Cache")
            val cacheContext = context + cacheName + SupervisorJob()

            asLoadingCache<K, V?>(CoroutineScope(cacheContext)) { load(it) }
        }

    /**
     * Loads an entity based on the given key. Must be implemented by subclasses.
     *
     * @param key The key used to identify the entity.
     * @return The loaded entity, or `null` if not found.
     */
    protected abstract suspend fun load(key: K): V?

    @ApiStatus.OverrideOnly
    protected abstract suspend fun <R> runInTransaction(block: suspend () -> R): R

    protected suspend fun update(key: K, block: suspend V.() -> Unit) = runInTransaction {
        withContext(context) {
            val entity = cache.get(key) ?: return@withContext null
            block(entity)
            entity
        }
    }

    protected suspend fun find(key: K) = cache.get(key)

    protected fun evict(key: K) = cache.invalidate(key)
}