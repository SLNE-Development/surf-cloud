package dev.slne.surf.cloud.api.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.key.Key
import net.querz.nbt.io.NBTOutputStream
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.Tag
import okio.use
import org.jetbrains.annotations.Unmodifiable
import java.io.ByteArrayOutputStream

internal abstract class PersistentPlayerDataContainerViewImpl : PersistentPlayerDataContainerView {

    abstract fun toTagCompound(): CompoundTag
    abstract fun getTag(key: String): Tag<*>?

    override fun <P : Any, C> has(
        key: Key,
        type: PersistentPlayerDataType<P, C>
    ): Boolean {
        val value = getTag(key.asString()) ?: return false
        return PersistentPlayerDataTypeRegistry.isInstanceOf(type, value)
    }

    override fun has(key: Key): Boolean {
        return getTag(key.asString()) != null
    }

    override fun <P : Any, C> get(
        key: Key,
        type: PersistentPlayerDataType<P, C>
    ): C? {
        val value = getTag(key.asString()) ?: return null

        if (!type.primitiveType.isInstance(value)) {
            error("Value under key ${key.asString()} is not of type ${type.primitiveType.simpleName}")
        }

        return type.fromPrimitive(
            PersistentPlayerDataTypeRegistry.extract<P, Tag<*>>(type, value),
            PersistentPlayerDataAdapterContextImpl
        )
    }

    override val keys: @Unmodifiable ObjectSet<Key>
        get() = toTagCompound().keySet().asSequence()
            .map { it.split(":", limit = 2) }
            .filter { it.size == 2 }
            .map { Key.key(it[0], it[1]) }
            .toObjectSet()


    override val empty: Boolean
        get() = toTagCompound().size() == 0

    override val adapterContext: PersistentPlayerDataAdapterContext
        get() = PersistentPlayerDataAdapterContextImpl

    override fun writeToBuf(buf: SurfByteBuf) {
        val root = toTagCompound()
        ByteArrayOutputStream().use {
            NBTOutputStream(it).use { it.writeRawTag(root, Int.MAX_VALUE) }
            val bytes = it.toByteArray()
            buf.writeVarInt(bytes.size)
            buf.writeBytes(bytes)
        }
    }
}