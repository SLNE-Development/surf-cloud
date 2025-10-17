package dev.slne.surf.cloud.core.common.player.ppdc.network

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readList
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUtf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeCollection
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUtf
import dev.slne.surf.cloud.api.common.util.ByIdMap
import io.netty.buffer.ByteBuf
import net.kyori.adventure.nbt.BinaryTag

sealed interface PdcOp {
    val path: List<String>
    val type: Type

    data class Put(
        override val path: List<String>,
        val value: BinaryTag
    ) : PdcOp {
        override val type = Type.PUT
    }

    data class Remove(
        override val path: List<String>
    ) : PdcOp {
        override val type = Type.REMOVE
    }

    companion object {
        val STREAM_CODEC = StreamCodec.of(::write, ::read)

        private fun write(buf: ByteBuf, op: PdcOp) {
            Type.STREAM_CODEC.encode(buf, op.type)
            buf.writeCollection(op.path) { buf, segment ->
                buf.writeUtf(segment)
            }

            when (op) {
                is Put -> ByteBufCodecs.BINARY_TAG_CODEC_COMPRESSED.encode(buf, op.value)
                is Remove -> Unit
            }
        }

        private fun read(buf: ByteBuf): PdcOp {
            val type = Type.STREAM_CODEC.decode(buf)
            val path = buf.readList { it.readUtf() }
            return when (type) {
                Type.PUT -> {
                    val value = ByteBufCodecs.BINARY_TAG_CODEC_COMPRESSED.decode(buf)
                    Put(path, value)
                }

                Type.REMOVE -> Remove(path)
            }
        }
    }

    enum class Type(val id: Int) {
        PUT(0),
        REMOVE(1);

        companion object {
            val BY_ID = ByIdMap.continuous(
                Type::id,
                entries.toTypedArray(),
                ByIdMap.OutOfBoundsStrategy.CLAMP
            )

            val STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID::invoke, Type::id)
        }
    }
}
