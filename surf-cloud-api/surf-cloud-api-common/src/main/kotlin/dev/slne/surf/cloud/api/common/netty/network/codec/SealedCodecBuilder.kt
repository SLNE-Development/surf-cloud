package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecodedNotNull
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkEncodedNotNull
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class SealedCodecBuilder<B, T : Any>(private val idOf: (T) -> Int) {
    private val entries = Int2ObjectOpenHashMap<StreamCodec<B, out T>>()

    fun <S : T> variant(id: Int, codec: StreamCodec<B, S>) {
        entries[id] = codec
    }

    fun build(idCodec: StreamCodec<B, Int>): StreamCodec<B, T> {
        entries.trim()

        return object : StreamCodec<B, T> {
            override fun encode(buf: B, value: T) {
                val id = idOf(value)
                idCodec.encode(buf, id)
                val codec = entries[id]
                checkEncodedNotNull(codec) { "Unknown id: $id" }
                @Suppress("UNCHECKED_CAST")
                (codec as StreamCodec<B, T>).encode(buf, value)
            }

            override fun decode(buf: B): T {
                val id = idCodec.decode(buf)
                val codec = entries[id]
                checkDecodedNotNull(codec) { "Unknown id: $id" }
                return codec.decode(buf)
            }
        }
    }
}