package dev.slne.surf.cloud.api.common.netty.network.codec

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps


infix fun <E> Codec<E>.orCompressed(second: Codec<E>): Codec<E> {
    return object : Codec<E> {
        override fun <T> encode(input: E, ops: DynamicOps<T>, prefix: T): DataResult<T> {
            return if (ops.compressMaps()) second.encode(
                input,
                ops,
                prefix
            ) else this@orCompressed.encode<T>(input, ops, prefix)
        }

        override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<E, T>> {
            return if (ops.compressMaps()) second.decode<T?>(
                ops,
                input
            ) else this@orCompressed.decode(
                ops,
                input
            )
        }

        override fun toString(): String {
            return this@orCompressed.toString() + " orCompressed " + second
        }
    }
}

fun <E> idResolverCodec(
    encoder: (E) -> Int,
    decoder: (Int) -> E?,
    notFoundValue: Int
): Codec<E> = Codec.INT
    .flatXmap({ integer ->
        val decoded = decoder(integer)
        if (decoded == null) {
            DataResult.error { "Unknown element id: $integer" }
        } else {
            DataResult.success(decoded)
        }
    }, { `object` ->
        val id = encoder(`object`)
        if (id == notFoundValue) {
            DataResult.error { "Element with unknown id: $`object`" }
        } else {
            DataResult.success(id)
        }
    })
