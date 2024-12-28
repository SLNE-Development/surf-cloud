package dev.slne.surf.cloud.core.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.cloud.api.common.util.nbt.set
import net.kyori.adventure.key.Key
import net.querz.nbt.io.NBTInputStream
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.Tag
import java.io.ByteArrayInputStream

class PersistentPlayerDataContainerImpl(
    private val tag: CompoundTag = CompoundTag()
) : PersistentPlayerDataContainerViewImpl(),
    PersistentPlayerDataContainer {

    override fun getTag(key: String): Tag<*>? = tag[key]

    override fun <P : Any, C> set(
        key: Key,
        type: PersistentPlayerDataType<P, C>,
        value: C
    ) {
        tag.put(
            key.asString(),
            PersistentPlayerDataTypeRegistry.wrap(
                type,
                type.toPrimitive(value, PersistentPlayerDataAdapterContextImpl)
            )
        )
    }

    fun put(key: String, value: Tag<*>) {
        tag[key] = value
    }

    override fun putBoolean(key: Key, value: Boolean) {
        tag[key.asString()] = value
    }

    override fun putByte(key: Key, value: Byte) {
        tag[key.asString()] = value
    }

    override fun putShort(key: Key, value: Short) {
        tag[key.asString()] = value
    }

    override fun putInt(key: Key, value: Int) {
        tag[key.asString()] = value
    }

    override fun putLong(key: Key, value: Long) {
        tag[key.asString()] = value
    }

    override fun putFloat(key: Key, value: Float) {
        tag[key.asString()] = value
    }

    override fun putDouble(key: Key, value: Double) {
        tag[key.asString()] = value
    }

    override fun putString(key: Key, value: String) {
        tag[key.asString()] = value
    }

    override fun putByteArray(key: Key, value: ByteArray) {
        tag[key.asString()] = value
    }

    override fun putIntArray(key: Key, value: IntArray) {
        tag[key.asString()] = value
    }

    override fun putLongArray(key: Key, value: LongArray) {
        tag[key.asString()] = value
    }

    override fun getBoolean(key: Key): Boolean? {
        return tag.getByteTag(key.asString())?.let { it.asByte() > 0 }
    }

    override fun getNumber(key: Key): Number? {
        val stringKey = key.asString()
        if (!tag.containsKey(stringKey)) return null

        return tag.getNumber(stringKey)
    }

    override fun getByte(key: Key): Byte? {
        return tag.getByteTag(key.asString())?.asByte()
    }

    override fun getShort(key: Key): Short? {
        return tag.getShortTag(key.asString())?.asShort()
    }

    override fun getInt(key: Key): Int? {
        return tag.getIntTag(key.asString())?.asInt()
    }

    override fun getLong(key: Key): Long? {
        return tag.getLongTag(key.asString())?.asLong()
    }

    override fun getFloat(key: Key): Float? {
        return tag.getFloatTag(key.asString())?.asFloat()
    }

    override fun getDouble(key: Key): Double? {
        return tag.getDoubleTag(key.asString())?.asDouble()
    }

    override fun getString(key: Key): String? {
        return tag.getString(key.asString())
    }

    override fun getByteArray(key: Key): ByteArray? {
        return tag.getByteArrayTag(key.asString())?.value
    }

    override fun getIntArray(key: Key): IntArray? {
        return tag.getIntArrayTag(key.asString())?.value
    }

    override fun getLongArray(key: Key): LongArray? {
        return tag.getLongArrayTag(key.asString())?.value
    }

    override fun remove(key: Key) {
        tag.remove(key.asString())
    }

    override val empty: Boolean
        get() = tag.size() == 0

    override fun toTagCompound(): CompoundTag = tag

    override fun readFromBuf(buf: SurfByteBuf) {
        tag.clear()
        val size = buf.readVarInt()
        val bytes = buf.readBytes(size).array()

        ByteArrayInputStream(bytes).use {
            NBTInputStream(it).use {
                val tag = it.readRawTag(Int.MAX_VALUE)
                check(tag is CompoundTag) { "Expected a CompoundTag, got $tag" }
                tag.forEach { (key, value) -> tag[key] = value }
            }
        }
    }

    fun fromTagCompound(tag: CompoundTag) {
        this.tag.clear()
        tag.forEach { (key, value) -> this.tag[key] = value }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersistentPlayerDataContainerImpl) return false

        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }

}