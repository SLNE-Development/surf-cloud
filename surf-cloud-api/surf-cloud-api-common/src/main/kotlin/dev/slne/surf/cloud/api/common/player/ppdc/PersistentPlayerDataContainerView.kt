package dev.slne.surf.cloud.api.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable

/**
 * Represents a read-only view of a persistent player data container.
 *
 * Provides methods to access metadata associated with a [dev.slne.surf.cloud.api.common.player.CloudPlayer],
 * check for the existence of data, and retrieve data using defined types.
 * The container does not support modifications directly through this interface.
 */
@ApiStatus.NonExtendable
interface PersistentPlayerDataContainerView {

    /**
     * Checks if metadata exists for the given key and type.
     *
     * @param key The key under which the metadata is stored.
     * @param type The data type to match against the stored primitive.
     * @return `true` if metadata exists with the given key and type, otherwise `false`.
     */

    fun <P : Any, C> has(key: Key, type: PersistentPlayerDataType<P, C>): Boolean

    /**
     * Checks if metadata exists for the given key, regardless of type.
     *
     * @param key The key under which the metadata is stored.
     * @return `true` if metadata exists with the given key, otherwise `false`.
     */
    fun has(key: Key): Boolean

    /**
     * Retrieves metadata stored under the given key and type.
     *
     * @param key The key under which the metadata is stored.
     * @param type The data type to cast the stored value to.
     * @return The metadata value, or `null` if no matching data exists.
     */
    fun <P : Any, C> get(key: Key, type: PersistentPlayerDataType<P, C>): C?

    // region Primitive-specific get methods.
    fun getBoolean(key: Key): Boolean?
    fun getNumber(key: Key): Number?
    fun getByte(key: Key): Byte?
    fun getShort(key: Key): Short?
    fun getInt(key: Key): Int?
    fun getLong(key: Key): Long?
    fun getFloat(key: Key): Float?
    fun getDouble(key: Key): Double?
    fun getString(key: Key): String?
    fun getByteArray(key: Key): ByteArray?
    fun getIntArray(key: Key): IntArray?
    fun getLongArray(key: Key): LongArray?
    // endregion

    /**
     * Retrieves all keys present in this container.
     *
     * @return An unmodifiable set of keys.
     */
    val keys: @Unmodifiable ObjectSet<Key>

    /**
     * Checks if the container is empty.
     *
     * @return `true` if the container has no entries, otherwise `false`.
     */
    val empty: Boolean

    /**
     * Retrieves the adapter context used by this container.
     *
     * @return The [PersistentPlayerDataAdapterContext] associated with this container.
     */
    val adapterContext: PersistentPlayerDataAdapterContext

    /**
     * Serializes the container's contents into a [SurfByteBuf].
     *
     * @param buf The buffer to write to.
     */
    fun writeToBuf(buf: SurfByteBuf)

    fun snapshot(): PersistentPlayerDataContainerView

    companion object {
        /**
         * Maximum nesting depth for compound tags.
         * This limit prevents memory exhaustion from extremely large nested structures.
         * Set to a reasonable limit that should handle most legitimate use cases while
         * protecting against pathological inputs.
         */
        const val MAX_NESTING_DEPTH = 512

        /**
         * Ensures that the nesting depth does not exceed the maximum allowed limit.
         *
         * This function validates that the provided depth is within acceptable bounds,
         * preventing memory exhaustion from extremely deep nested structures.
         *
         * @param depth The current nesting depth to validate.
         * @param exceptionFactory A factory function that creates the exception to throw
         *                         when the depth exceeds the limit. Defaults to [IllegalStateException].
         * @throws Throwable When the depth exceeds [MAX_NESTING_DEPTH]. The specific exception
         *                   type is determined by the [exceptionFactory] parameter.
         */
        inline fun ensureValidNestingDepth(
            depth: Int,
            exceptionFactory: (message: String) -> Throwable = ::IllegalStateException
        ) {
            if (depth > MAX_NESTING_DEPTH) {
                throw exceptionFactory("Exceeded maximum allowed nesting depth of $MAX_NESTING_DEPTH. This likely indicates a corrupted or maliciously crafted data structure.")
            }
        }
    }
}