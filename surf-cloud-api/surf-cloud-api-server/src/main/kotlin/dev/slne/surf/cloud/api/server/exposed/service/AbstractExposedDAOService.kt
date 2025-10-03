package dev.slne.surf.cloud.api.server.exposed.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.exposed.dao.Entity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
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
    @Autowired
    lateinit var applicationContext: ApplicationContext

    protected val self get() = applicationContext.getBean(javaClass)

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
                self.load(it)
            }
        }

    /**
     * Loads an entity based on the given key. Must be implemented by subclasses.
     *
     * @param key The key used to identify the entity.
     * @return The loaded entity, or `null` if not found.
     */
    protected abstract suspend fun load(key: K): V?
    protected abstract suspend fun create(key: K): V
    protected open suspend fun loadOrCreate(key: K): V = load(key) ?: create(key)

    /**
     * Retrieves an entity from the cache or loads it if not present.
     *
     * @param key The key used to identify the entity.
     * @return The cached or loaded entity, or `null` if not found.
     */
    protected suspend fun find(key: K) = cache.get(key)

    @CoroutineTransactional
    protected suspend fun <R> find(key: K, result: V.() -> R): R? {
        val entity = cache.get(key) ?: return null
        return entity.result()
    }

    /**
     * Updates an entity by applying the given modification block.
     *
     * @param key The key used to identify the entity.
     * @param block The modification block to apply to the entity.
     * @return The updated entity, or `null` if not found.
     */
    @CoroutineTransactional
    protected suspend fun update(
        key: K,
        createIfMissing: Boolean = true,
        block: suspend V.() -> Unit
    ) {
        val entity = cache.get(key) ?: if (createIfMissing) self.create(key) else null
        if (entity != null) {
            entity.block()
            cache.put(key, entity)
        } else {
            null
        }
    }

    protected suspend fun getOrCreate(key: K): V = cache.get(key) {
        self.loadOrCreate(key)
    } ?: error("Failed to load or create entity for key $key")


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