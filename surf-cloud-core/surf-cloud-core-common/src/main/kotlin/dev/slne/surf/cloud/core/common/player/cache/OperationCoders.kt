package dev.slne.surf.cloud.core.common.player.cache

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.toByteArraySafe
import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundCacheOpPacket.Kind
import dev.slne.surf.cloud.core.common.player.cache.CacheOperation.*
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

object OperationCoders {
    private val emptyByteArray = ByteArray(0)

    fun <T : Any> encodeValueOp(
        key: CacheKey.Value<T>,
        op: ValueSet<T>
    ): Pair<Kind, ByteArray> {
        val buf = SurfByteBuf.buffer()
        buf.writeNullable(op.value) { buf, value -> buf.writeWithCodec(key.codec, value) }
        return Kind.VALUE_SET to buf.toByteArraySafe()
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Any> encodeListOp(
        key: CacheKey.List<E>,
        op: ListOperation
    ): Pair<Kind, ByteArray> {
        val buf = SurfByteBuf.buffer()
        return when (op) {
            is ListOperation.Append<*> -> {
                op as ListOperation.Append<E>
                buf.writeCollection(op.elements) { buf, elem ->
                    buf.writeWithCodec(key.elementCodec, elem)
                }
                Kind.LIST_APPEND to buf.toByteArraySafe()
            }

            is ListOperation.Insert<*> -> {
                op as ListOperation.Insert<E>
                buf.writeVarInt(op.index)
                buf.writeCollection(op.elements) { buf, elem ->
                    buf.writeWithCodec(key.elementCodec, elem)
                }
                Kind.LIST_INSERT to buf.toByteArraySafe()
            }

            is ListOperation.Set<*> -> {
                op as ListOperation.Set<E>
                buf.writeVarInt(op.index)
                buf.writeWithCodec(key.elementCodec, op.element)
                Kind.LIST_SET to buf.toByteArraySafe()
            }

            is ListOperation.RemoveAt -> {
                buf.writeVarInt(op.index)
                buf.writeVarInt(op.count)
                Kind.LIST_REMOVE_AT to buf.toByteArraySafe()
            }

            ListOperation.Clear -> {
                Kind.LIST_CLEAR to emptyByteArray
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Any> encodeSetOp(key: CacheKey.Set<E>, op: SetOperation): Pair<Kind, ByteArray> {
        val buf = SurfByteBuf.buffer()

        return when (op) {
            is SetOperation.Add<*> -> {
                op as SetOperation.Add<E>
                buf.writeCollection(op.elements) { buf, elem ->
                    buf.writeWithCodec(key.elementCodec, elem)
                }
                Kind.SET_ADD to buf.toByteArraySafe()
            }

            is SetOperation.Remove<*> -> {
                op as SetOperation.Remove<E>
                buf.writeCollection(op.elements) { buf, elem ->
                    buf.writeWithCodec(key.elementCodec, elem)
                }
                Kind.SET_REMOVE to buf.toByteArraySafe()
            }

            SetOperation.Clear -> {
                Kind.SET_CLEAR to emptyByteArray
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <K : Any, V : Any> encodeMapOp(
        key: CacheKey.Map<K, V>,
        op: MapOperation
    ): Pair<Kind, ByteArray> {
        val buf = SurfByteBuf.buffer()

        return when (op) {
            is MapOperation.Put<*, *> -> {
                op as MapOperation.Put<K, V>
                buf.writeMap(
                    op.entries,
                    { buf, k -> buf.writeWithCodec(key.keyCodec, k) },
                    { buf, v -> buf.writeWithCodec(key.valueCodec, v) }
                )
                Kind.MAP_PUT to buf.toByteArraySafe()
            }

            is MapOperation.Remove<*> -> {
                op as MapOperation.Remove<K>
                buf.writeCollection(op.keys) { buf, k -> buf.writeWithCodec(key.keyCodec, k) }
                Kind.MAP_REMOVE to buf.toByteArraySafe()
            }

            MapOperation.Clear -> {
                Kind.MAP_CLEAR to emptyByteArray
            }
        }
    }

    fun <T : Any, D : Any> encodeStructuredOp(
        key: CacheKey.Structured<T, D>,
        op: StructuredDelta<D>
    ): Pair<Kind, ByteArray> {
        val buf = SurfByteBuf.buffer()
        buf.writeWithCodec(key.deltaCodec, op.delta)
        return Kind.STRUCTURED_DELTA to buf.toByteArraySafe()
    }

    fun <T : Any> decodeValueOp(
        key: CacheKey.Value<T>,
        kind: Kind,
        payload: ByteArray
    ): ValueSet<T> {
        require(kind == Kind.VALUE_SET) { "Invalid kind $kind for ValueSet operation" }
        val buf = SurfByteBuf.of(payload)
        val value = buf.readNullable { buf.readWithCodec(key.codec) }
        return ValueSet(value)
    }

    fun <E : Any> decodeListOp(
        key: CacheKey.List<E>,
        kind: Kind,
        payload: ByteArray
    ): ListOperation = when (kind) {
        Kind.LIST_APPEND -> SurfByteBuf.of(payload).let { buf ->
            val elements = buf.readList { buf.readWithCodec(key.elementCodec) }
            ListOperation.Append(elements)
        }

        Kind.LIST_INSERT -> SurfByteBuf.of(payload).let { buf ->
            val index = buf.readVarInt()
            val elements = buf.readList { buf.readWithCodec(key.elementCodec) }
            ListOperation.Insert(index, elements)
        }

        Kind.LIST_SET -> SurfByteBuf.of(payload).let { buf ->
            val index = buf.readVarInt()
            val element = buf.readWithCodec(key.elementCodec)
            ListOperation.Set(index, element)
        }

        Kind.LIST_REMOVE_AT -> SurfByteBuf.of(payload).let { buf ->
            val index = buf.readVarInt()
            val count = buf.readVarInt()
            ListOperation.RemoveAt(index, count)
        }

        Kind.LIST_CLEAR -> ListOperation.Clear
        else -> error("Invalid kind $kind for ListOperation")
    }

    fun <E : Any> decodeSetOp(
        key: CacheKey.Set<E>,
        kind: Kind,
        payload: ByteArray
    ): SetOperation = when (kind) {
        Kind.SET_ADD, Kind.SET_REMOVE -> SurfByteBuf.of(payload).let { buf ->
            val elements =
                buf.readCollection(::ObjectOpenHashSet) { buf.readWithCodec(key.elementCodec) }
            if (kind == Kind.SET_ADD) SetOperation.Add(elements) else SetOperation.Remove(elements)
        }

        Kind.SET_CLEAR -> SetOperation.Clear
        else -> error("Invalid kind $kind for SetOperation")
    }

    fun <K : Any, V : Any> decodeMapOp(
        key: CacheKey.Map<K, V>,
        kind: Kind,
        payload: ByteArray
    ): MapOperation = when (kind) {
        Kind.MAP_PUT -> SurfByteBuf.of(payload).let { buf ->
            val entries = buf.readMap(
                { buf.readWithCodec(key.keyCodec) },
                { buf.readWithCodec(key.valueCodec) }
            )
            MapOperation.Put(entries)
        }

        Kind.MAP_REMOVE -> SurfByteBuf.of(payload).let { buf ->
            val keys = buf.readCollection(::ObjectOpenHashSet) { buf.readWithCodec(key.keyCodec) }
            MapOperation.Remove(keys)
        }

        Kind.MAP_CLEAR -> MapOperation.Clear
        else -> error("Invalid kind $kind for MapOperation")
    }

    fun <T : Any, D : Any> decodeStructuredOp(
        key: CacheKey.Structured<T, D>,
        kind: Kind,
        payload: ByteArray
    ): StructuredDelta<D> {
        require(kind == Kind.STRUCTURED_DELTA) { "Invalid kind $kind for StructuredDelta operation" }
        val buf = SurfByteBuf.of(payload)
        val delta = buf.readWithCodec(key.deltaCodec)
        return StructuredDelta(delta)
    }
}