package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.nbt

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import net.kyori.adventure.nbt.CompoundBinaryTag

typealias SerializableCompoundTag = @Serializable(with = CompoundTagSerializer::class) CompoundBinaryTag

object CompoundTagSerializer : CloudBufSerializer<CompoundBinaryTag>() {
    override val descriptor =
        SerialDescriptor("CompoundBinaryTag", ByteArraySerializer().descriptor)

    override fun serialize0(
        buf: SurfByteBuf,
        value: CompoundBinaryTag
    ) {
        buf.writeCompoundTag(value)
    }

    override fun deserialize0(buf: SurfByteBuf): CompoundBinaryTag {
        return buf.readCompoundTag()
    }
}