package dev.slne.surf.cloud.api.common.util

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readVarInt
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeVarInt
import dev.slne.surf.cloud.api.common.util.ByIdMap.OutOfBoundsStrategy
import io.netty.buffer.ByteBuf

interface IdRepresentable {
    val id: Int

    companion object {

        fun <E> codec(itemLookup: (Int) -> E): IdRepresentableStreamCodec<E> where E : IdRepresentable {
            return IdRepresentableStreamCodec(itemLookup)
        }

        fun <E : IdRepresentable> continuousIdMap(
            values: Array<E>,
            outOfBoundsStrategy: OutOfBoundsStrategy
        ) = ByIdMap.continuous(IdRepresentable::id, values, outOfBoundsStrategy)

        inline fun <reified E> enumIdMap(
            outOfBoundsStrategy: OutOfBoundsStrategy
        ) where E : IdRepresentable, E : Enum<E> =
            ByIdMap.continuous(IdRepresentable::id, enumValues<E>(), outOfBoundsStrategy)


        class IdRepresentableStreamCodec<S : IdRepresentable>(
            private val itemLookup: (Int) -> S,
        ) : StreamCodec<ByteBuf, S> {

            override fun decode(buf: ByteBuf): S {
                return itemLookup(buf.readVarInt())
            }

            override fun encode(buf: ByteBuf, value: S) {
                buf.writeVarInt(value.id)
            }
        }
    }
}