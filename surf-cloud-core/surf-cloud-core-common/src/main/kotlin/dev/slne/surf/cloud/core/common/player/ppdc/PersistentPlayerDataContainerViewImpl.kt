package dev.slne.surf.cloud.core.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataAdapterContext
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import org.jetbrains.annotations.Unmodifiable

abstract class PersistentPlayerDataContainerViewImpl : PersistentPlayerDataContainerView {

    abstract fun toTagCompound(): CompoundBinaryTag
    abstract fun getTag(key: String): BinaryTag?

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
            PersistentPlayerDataTypeRegistry.extract<P, BinaryTag>(type, value),
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
        buf.writeCompoundTag(root)
    }
}