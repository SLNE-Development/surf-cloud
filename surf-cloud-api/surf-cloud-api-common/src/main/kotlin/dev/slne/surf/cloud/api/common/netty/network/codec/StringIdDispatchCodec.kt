@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)

package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUtf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUtf
import dev.slne.surf.cloud.api.common.util.freeze
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import java.util.function.Function
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference


class StringIdDispatchCodec<B : ByteBuf, V, T> private constructor(
    private val typeGetter: Function<V, out T>,
    private val byId: Object2ObjectMap<String, Entry<B, V, T>>,
    private val toId: Object2ObjectMap<T, String>
) : StreamCodec<B, V> {

    override fun decode(buf: B): V {
        val id = buf.readUtf()
        val entry = byId[id]

        if (id.isNotEmpty() && entry != null) {
            try {
                return entry.serializer.decode(buf)
            } catch (e: Exception) {
                throw DecoderException("Failed to decode packet '${entry.type}'", e)
            }
        } else {
            throw DecoderException("Received unknown packet id $id")
        }
    }

    override fun encode(buf: B, value: V) {
        val type = typeGetter.apply(value)
        val id = toId[type] ?: throw EncoderException("Sending unknown packet '$type'")

        buf.writeUtf(id)
        val entry = byId[id] ?: throw EncoderException("Messed up mapping for packet '$type'")

        try {
            val streamCodec = entry.serializer as StreamCodec<in B, V>
            streamCodec.encode(buf, value)
        } catch (e: Exception) {
            throw EncoderException("Failed to encode packet '$type'", e)
        }
    }

    class Builder<B : ByteBuf, V, T> internal constructor(private val typeGetter: Function<V, out T>) {
        private val entries = mutableObjectListOf<Entry<B, V, T>>()

        fun add(id: T, codec: StreamCodec<in B, out V>) = apply {
            entries.add(Entry(codec, id))
        }

        fun build(typeToIdMapper: (T) -> String): StringIdDispatchCodec<B, V, T> {
            val typeToIdMap = mutableObject2ObjectMapOf<T, String>(entries.size)
            val idToTypeMap = mutableObject2ObjectMapOf<String, Entry<B, V, T>>(entries.size)

            for (entry in this.entries) {
                val type = entry.type
                val id = typeToIdMapper(type)

                val previousValue = typeToIdMap.putIfAbsent(type, id)
                check(previousValue == null) { "Duplicate registration for type $type" }
                idToTypeMap.put(id, entry)
            }

            return StringIdDispatchCodec(
                this.typeGetter,
                idToTypeMap.freeze(),
                typeToIdMap.freeze()
            )
        }
    }

    internal data class Entry<B, V, T>(val serializer: StreamCodec<in B, out V>, val type: T)

    companion object {
        @JvmStatic
        fun <B : ByteBuf, V, T> builder(packetIdGetter: Function<V, out T>): Builder<B, V, T> {
            return Builder(packetIdGetter)
        }
    }
}


fun <B : ByteBuf, V, T> buildStringIdDispatchCodec(
    packetIdGetter: (V) -> T,
    typeToIdMapper: (T) -> String,
    @BuilderInference block: StringIdDispatchCodec.Builder<B, V, T>.() -> Unit
): StringIdDispatchCodec<B, V, T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return StringIdDispatchCodec.builder<B, V, T> { packetIdGetter(it) }.apply(block)
        .build(typeToIdMapper)
}