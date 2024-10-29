@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)

package dev.slne.surf.cloud.api.netty.network.codec

import dev.slne.surf.cloud.api.netty.protocol.buffer.readVarInt
import dev.slne.surf.cloud.api.netty.protocol.buffer.writeVarInt
import dev.slne.surf.cloud.api.util.freeze
import dev.slne.surf.cloud.api.util.mutableObjectListOf
import dev.slne.surf.cloud.api.util.object2IntMapOf
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.ObjectList
import java.util.function.Function
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

private const val UNKNOWN_TYPE = -1

class IdDispatchCodec<B : ByteBuf, V, T> private constructor(
    private val typeGetter: Function<V, out T>,
    private val byId: ObjectList<Entry<B, V, T>>,
    private val toId: Object2IntMap<T>
) : StreamCodec<B, V> {
    init {
        toId.defaultReturnValue(UNKNOWN_TYPE)
    }

    override fun decode(buf: B): V {
        val id = buf.readVarInt()

        if (id >= 0 && id < byId.size) {
            val entry = byId[id]

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
        val id = toId.getInt(type)

        if (id == UNKNOWN_TYPE) {
            throw EncoderException("Sending unknown packet '$type'")
        } else {
            buf.writeVarInt(id)
            val entry = byId[id]

            try {
                val streamCodec = entry.serializer as StreamCodec<in B, V>
                streamCodec.encode(buf, value)
            } catch (e: Exception) {
                throw EncoderException("Failed to encode packet '$type'", e)
            }
        }
    }

    class Builder<B : ByteBuf, V, T> internal constructor(private val typeGetter: Function<V, out T>) {
        private val entries = mutableObjectListOf<Entry<B, V, T>>()

        fun add(id: T, codec: StreamCodec<in B, out V>) = apply {
            entries.add(Entry(codec, id))
        }

        fun build(): IdDispatchCodec<B, V, T> {
            val typeToIdMap = object2IntMapOf<T>()
            typeToIdMap.defaultReturnValue(-2)

            for ((_, type) in this.entries) {
                val currentSize = typeToIdMap.size
                val previousValue = typeToIdMap.putIfAbsent(type, currentSize)
                check(previousValue == -2) { "Duplicate registration for type $type" }
            }

            return IdDispatchCodec(
                this.typeGetter,
                this.entries.freeze(),
                typeToIdMap
            )
        }
    }

    @JvmRecord
    internal data class Entry<B, V, T>(val serializer: StreamCodec<in B, out V>, val type: T)

    companion object {
        @JvmStatic
        fun <B : ByteBuf, V, T> builder(packetIdGetter: Function<V, out T>): Builder<B, V, T> {
            return Builder(packetIdGetter)
        }
    }
}


fun <B : ByteBuf, V, T> buildIdDispatchCodec(
    packetIdGetter: (V) -> T,
    @BuilderInference block: IdDispatchCodec.Builder<B, V, T>.() -> Unit
): IdDispatchCodec<B, V, T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return IdDispatchCodec.builder<B, V, T> { packetIdGetter(it) }.apply(block).build()
}