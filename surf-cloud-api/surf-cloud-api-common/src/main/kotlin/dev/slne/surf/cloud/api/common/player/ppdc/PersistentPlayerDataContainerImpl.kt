package dev.slne.surf.cloud.api.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import net.kyori.adventure.key.Key
import net.querz.nbt.io.NBTInputStream
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.Tag
import java.io.ByteArrayInputStream

internal class PersistentPlayerDataContainerImpl : PersistentPlayerDataContainerViewImpl(),
    PersistentPlayerDataContainer {
    private val tags = mutableObject2ObjectMapOf<String, Tag<*>>()

    override fun getTag(key: String): Tag<*>? = tags[key]

    override fun <P : Any, C> set(
        key: Key,
        type: PersistentPlayerDataType<P, C>,
        value: C
    ) {
        tags.put(
            key.asString(),
            PersistentPlayerDataTypeRegistry.wrap(
                type,
                type.toPrimitive(value, PersistentPlayerDataAdapterContextImpl)
            )
        )
    }

    override fun remove(key: Key) {
        tags.remove(key.asString())
    }

    override val empty: Boolean
        get() = tags.isEmpty

    override fun toTagCompound(): CompoundTag = CompoundTag().apply {
        tags.forEach { (key, tag) -> put(key, tag) }
    }

    fun put(string: String, tag: Tag<*>) {
        tags[string] = tag
    }

    override fun readFromBuf(buf: SurfByteBuf) {
        tags.clear()
        val size = buf.readVarInt()
        val bytes = buf.readBytes(size).array()

        ByteArrayInputStream(bytes).use {
            NBTInputStream(it).use {
                val tag = it.readRawTag(Int.MAX_VALUE)
                check(tag is CompoundTag) { "Expected a CompoundTag, got $tag" }
                tag.forEach { (key, value) -> tags[key] = value }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersistentPlayerDataContainerImpl) return false

        if (tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        return tags.hashCode()
    }
}