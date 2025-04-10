package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.nbt

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import net.querz.nbt.tag.CompoundTag

typealias SerializableCompoundTag = @Serializable(with = CompoundTagSerializer::class) CompoundTag

object CompoundTagSerializer : CloudBufSerializer<CompoundTag>() {
    override val descriptor = SerialDescriptor("CompoundTag", ByteArraySerializer().descriptor)

    override fun serialize0(
        buf: SurfByteBuf,
        value: CompoundTag
    ) {
        buf.writeCompoundTag(value)
    }

    override fun deserialize0(buf: SurfByteBuf): CompoundTag {
        return buf.readCompoundTag()
    }
}