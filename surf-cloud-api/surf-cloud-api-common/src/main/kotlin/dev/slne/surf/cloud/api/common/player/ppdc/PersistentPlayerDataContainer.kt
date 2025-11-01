package dev.slne.surf.cloud.api.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.ApiStatus

/**
 * Represents a modifiable persistent player data container.
 *
 * Extends [PersistentPlayerDataContainerView] to include methods for modifying
 * metadata entries and managing the stored data.
 */
@ApiStatus.NonExtendable
interface PersistentPlayerDataContainer : PersistentPlayerDataContainerView {

    /**
     * Stores a metadata value in the container.
     *
     * @param key The key to store the value under.
     * @param type The data type of the value.
     * @param value The value to store.
     * @throws IllegalArgumentException If no suitable adapter is found for the type.
     */
    fun <P : Any, C> set(key: Key, type: PersistentPlayerDataType<P, C>, value: C)

    // region Primitive-specific set methods.
    fun setBoolean(key: Key, value: Boolean?)
    fun setByte(key: Key, value: Byte?)
    fun setShort(key: Key, value: Short?)
    fun setInt(key: Key, value: Int?)
    fun setLong(key: Key, value: Long?)
    fun setFloat(key: Key, value: Float?)
    fun setDouble(key: Key, value: Double?)
    fun setString(key: Key, value: String?)
    fun setByteArray(key: Key, value: ByteArray?)
    fun setIntArray(key: Key, value: IntArray?)
    fun setLongArray(key: Key, value: LongArray?)
    // endregion

    /**
     * Removes metadata associated with the given key.
     *
     * @param key The key to remove.
     */
    fun remove(key: Key)

    /**
     * Deserializes data from a [SurfByteBuf] and stores it in the container.
     *
     * @param buf The buffer to read from.
     */
    fun readFromBuf(buf: SurfByteBuf)

    override fun snapshot(): PersistentPlayerDataContainer
}