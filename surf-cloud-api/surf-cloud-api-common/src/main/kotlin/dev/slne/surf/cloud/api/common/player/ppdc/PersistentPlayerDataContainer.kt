package dev.slne.surf.cloud.api.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface PersistentPlayerDataContainer: PersistentPlayerDataContainerView {

    /**
     * Stores a metadata value on the [dev.slne.surf.cloud.api.common.player.CloudPlayer] instance.
     *
     * This method will override any existing
     * value the [dev.slne.surf.cloud.api.common.player.CloudPlayer] may have stored under the provided
     * key.
     *
     * @param key the key this value will be stored under
     * @param type the type this tag uses
     * @param value the value to store in the tag
     * @param <P> the generic java type of the tag value
     * @param <C> the generic type of the object to store
     *
     * @throws IllegalArgumentException if no suitable adapter was found for
     * the [PersistentPlayerDataType.primitiveType]
     */
    fun <P : Any, C> set(key: Key, type: PersistentPlayerDataType<P, C>, value: C)

    fun setBoolean(key: Key, value: Boolean)
    fun setByte(key: Key, value: Byte)
    fun setShort(key: Key, value: Short)
    fun setInt(key: Key, value: Int)
    fun setLong(key: Key, value: Long)
    fun setFloat(key: Key, value: Float)
    fun setDouble(key: Key, value: Double)
    fun setString(key: Key, value: String)
    fun setByteArray(key: Key, value: ByteArray)
    fun setIntArray(key: Key, value: IntArray)
    fun setLongArray(key: Key, value: LongArray)

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

    /**
     * Removes a metadata value from the [dev.slne.surf.cloud.api.common.player.CloudPlayer] instance.
     *
     * @param key the key to remove from the custom tag map
     */
    fun remove(key: Key)

    /**
     * Reads the data from the provided [SurfByteBuf] and stores it in the container.
     *
     * @param buf the buffer to read the data from
     */
    fun readFromBuf(buf: SurfByteBuf)
}