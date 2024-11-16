package dev.slne.surf.cloud.api.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable

/**
 * This represents a view of a persistent data container. No
 * methods on this interface mutate the container.
 *
 * @see PersistentDataContainer
 */
@ApiStatus.NonExtendable
interface PersistentPlayerDataContainerView {

    /**
     * Returns if the persistent metadata provider has metadata registered
     * matching the provided parameters.
     *
     * This method will only return true if the found value has the same primitive
     * data type as the provided key.
     *
     * Storing a value using a custom [PersistentPlayerDataType] implementation
     * will not store the complex data type. Therefore, storing a UUID (by
     * storing a [ByteArray] will match `has("key" ,
     * [PersistentPlayerDataType.BYTE_ARRAY])`. Likewise, a stored [ByteArray] will
     * always match your UUID [PersistentPlayerDataType] even if it is not 16
     * bytes long.
     *
     * @param key the key the value is stored under
     * @param type the type the primitive stored value has to match
     * @param <P> the generic type of the stored primitive
     * @param <C> the generic type of the eventually created complex object
     * @return if a value with the provided key and type exists
     * @throws IllegalArgumentException if the key to look up is null
     * @throws IllegalArgumentException if the type to cast the found object to is null
     */
    fun <P: Any, C> has(key: Key, type: PersistentPlayerDataType<P, C>): Boolean

    /**
     * Returns if the persistent metadata provider has metadata registered matching
     * the provided parameters.
     *
     * This method will return true as long as a value with the given key exists,
     * regardless of its type.
     *
     * This method is only usable for custom object keys. Overwriting existing tags,
     * like the display name, will not work as the values are stored using your
     * namespace.
     *
     * @param key the key the value is stored under
     * @return if a value with the provided key exists
     * @throws IllegalArgumentException if the key to look up is null
     */
    fun has(key: Key): Boolean

    /**
     * Returns the metadata value that is stored on the
     * [dev.slne.surf.cloud.api.common.player.CloudPlayer] instance.
     *
     * @param key the key to look up in the custom tag map
     * @param type the type the value must have and will be casted to
     * @param <P> the generic type of the stored primitive
     * @param <C> the generic type of the eventually created complex object
     * @return the value or `null` if no value was mapped under the given
     * value
     * @throws IllegalArgumentException if the key to look up is null
     * @throws IllegalArgumentException if the type to cast the found object to is
     * null
     * @throws IllegalArgumentException if a value exists under the given key,
     * but cannot be accessed using the given type
     * @throws IllegalArgumentException if no suitable adapter was found for
     * the [PersistentPlayerDataType.complexType] of the provided
     */
    fun <P: Any, C> get(key: Key, type: PersistentPlayerDataType<P, C>): C?

    /**
     * Get the set of keys present on this [PersistentPlayerDataContainerView]
     * instance.
     *
     * Any changes made to the returned set will not be reflected on the
     * instance.
     *
     * @return the key set
     */
    val keys: @Unmodifiable ObjectSet<Key>

    /**
     * Returns if the container instance is empty, therefore, has no entries
     * inside it.
     *
     * @return the boolean
     */
    val empty: Boolean

    /**
     * Returns the adapter context this tag container uses.
     *
     * @return the tag context
     */
    val adapterContext: PersistentPlayerDataAdapterContext

    fun writeToBuf(buf: SurfByteBuf)
}