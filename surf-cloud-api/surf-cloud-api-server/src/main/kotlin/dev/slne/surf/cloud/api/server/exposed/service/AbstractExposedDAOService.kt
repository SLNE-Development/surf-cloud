package dev.slne.surf.cloud.api.server.exposed.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.coroutines.CoroutineContext

/**
 * Abstract service class for managing Exposed DAO entities with caching support.
 *
 * @param K The type of the key used for caching.
 * @param V The type of the Exposed [Entity] being managed.
 * @param cacheCustomizer A lambda function to customize the Caffeine cache configuration.
 * @param context The [CoroutineContext] in which transactions and cache operations are executed. Defaults to [Dispatchers.IO].
 */
@OptIn(DirectCacheAccess::class)
abstract class AbstractExposedDAOService<K, V : Entity<*>>(
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

            asLoadingCache<K, V?>(CoroutineScope(cacheContext)) {
                withTransaction {
                    load(it)
                }
            }
        }

    /**
     * Loads an entity based on the given key. Must be implemented by subclasses.
     *
     * @param key The key used to identify the entity.
     * @return The loaded entity, or `null` if not found.
     */
    protected abstract suspend fun load(key: K): V?

    /**
     * Executes a suspendable transaction within the configured coroutine context.
     *
     * @param block The transactional block to execute.
     * @return The result of the transaction.
     */
    protected suspend fun <T> withTransaction(block: suspend Transaction.() -> T) =
        newSuspendedTransaction(context) {
            block()
        }

    /**
     * Retrieves an entity from the cache or loads it if not present.
     *
     * @param key The key used to identify the entity.
     * @return The cached or loaded entity, or `null` if not found.
     */
    protected suspend fun find(key: K) = cache.get(key)

    /**
     * Updates an entity by applying the given modification block.
     *
     * @param key The key used to identify the entity.
     * @param block The modification block to apply to the entity.
     * @return The updated entity, or `null` if not found.
     */
    protected suspend fun update(key: K, block: suspend V.() -> Unit) = withTransaction {
        val entity = cache.get(key) ?: return@withTransaction null
        entity.block()
        entity
    }

    /**
     * Removes an entity from the cache.
     *
     * @param key The key of the entity to evict.
     */
    protected fun evict(key: K) = cache.invalidate(key)
}

/**
 * Opt-in annotation to indicate that direct access to the cache should be avoided unless necessary.
 * Prefer using `find`, `update`, or other provided methods instead.
 */
@RequiresOptIn(
    message = "Direct access to the cache is discouraged. Consider using provided methods like find() or update() instead.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class DirectCacheAccess