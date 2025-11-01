package dev.slne.surf.cloud.api.common.netty.network.codec

import io.netty.buffer.ByteBuf
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.UnaryOperator

interface StreamCodec<B, V> : StreamDecoder<B, V>, StreamEncoder<B, V> {
    fun <O> apply(function: CodecOperation<B, V, O>) = function.apply(this)

    fun <O> map(
        to: Function<in V, out O>,
        from: Function<in O, out V>
    ): StreamCodec<B, O> = object : StreamCodec<B, O> {
        override fun decode(buf: B): O = to.apply(this@StreamCodec.decode(buf))
        override fun encode(buf: B, value: O) = this@StreamCodec.encode(buf, from.apply(value))
    }

    fun <O : ByteBuf> mapStream(function: Function<O, out B>): StreamCodec<O, V> =
        object : StreamCodec<O, V> {
            override fun decode(buf: O): V = this@StreamCodec.decode(function.apply(buf))
            override fun encode(buf: O, value: V) =
                this@StreamCodec.encode(function.apply(buf), value)
        }

    fun <U> dispatch(
        type: Function<in U, out V>,
        codec: Function<in V, out StreamCodec<in B, out U>>
    ): StreamCodec<B, U> = object : StreamCodec<B, U> {
        override fun decode(buf: B): U = codec.apply(this@StreamCodec.decode(buf)).decode(buf)
        override fun encode(buf: B, value: U) {
            val obj = type.apply(value)
            val streamCodec = codec.apply(obj) as StreamCodec<B, U>
            this@StreamCodec.encode(buf, obj)
            streamCodec.encode(buf, value)
        }
    }

    companion object {
        @JvmStatic
        fun <B, V> of(
            encoder: StreamEncoder<B, V>,
            decoder: StreamDecoder<B, V>
        ): StreamCodec<B, V> = object : StreamCodec<B, V> {
            override fun decode(buf: B): V = decoder.decode(buf)
            override fun encode(buf: B, value: V) = encoder.encode(buf, value)
        }

        @JvmStatic
        fun <B, V> ofMember(
            encoder: StreamMemberEncoder<B, V>,
            decoder: StreamDecoder<B, V>
        ): StreamCodec<B, V> = object : StreamCodec<B, V> {
            override fun decode(buf: B): V = decoder.decode(buf)
            override fun encode(buf: B, value: V) = encoder.encode(value, buf)
        }

        @JvmStatic
        fun <B, V> unit(value: V): StreamCodec<B, V> {
            return object : StreamCodec<B, V> {
                override fun decode(buf: B): V = value
                override fun encode(buf: B, otherValue: V) {
                    check(value == otherValue) { "Can't encode '$otherValue', expected '$value'" }
                }
            }
        }

        @JvmStatic
        fun <B, C, T1> composite(
            codec: StreamCodec<in B, T1>,
            from: Function<C, T1>,
            to: Function<T1, C>
        ): StreamCodec<B, C> = object : StreamCodec<B, C> {
            override fun decode(buf: B): C = to.apply(codec.decode(buf))
            override fun encode(buf: B, value: C) = codec.encode(buf, from.apply(value))
        }

        fun <B, C, T1, T2> composite(
            codec1: StreamCodec<in B, T1>,
            from1: Function<C, T1>,
            codec2: StreamCodec<in B, T2>,
            from2: Function<C, T2>,
            to: BiFunction<T1, T2, C>
        ): StreamCodec<B, C> = object : StreamCodec<B, C> {
            override fun decode(buf: B): C = to.apply(codec1.decode(buf), codec2.decode(buf))
            override fun encode(buf: B, value: C) {
                codec1.encode(buf, from1.apply(value))
                codec2.encode(buf, from2.apply(value))
            }
        }

        fun <B, T> recursive(codecGetter: UnaryOperator<StreamCodec<B, T>>) =
            object : StreamCodec<B, T> {
                private val inner by lazy { codecGetter.apply(this) }
                override fun decode(buf: B): T = inner.decode(buf)
                override fun encode(buf: B, value: T) = inner.encode(buf, value)
            }
    }
}

@FunctionalInterface
fun interface CodecOperation<B, S, T> {
    fun apply(codec: StreamCodec<B, S>): StreamCodec<B, T>
}

fun <B, V, O> StreamCodec<B, V>.apply(operation: (StreamCodec<B, V>) -> StreamCodec<B, O>) =
    operation(this)

fun <B, V, O> StreamCodec<B, V>.map(
    to: (V) -> O,
    from: (O) -> V
) = map({ to(it) }, { from(it) })

fun <B, V, O : ByteBuf> StreamCodec<B, V>.mapStream(function: (O) -> B) =
    mapStream(Function<O, B> { function(it) })


fun <B, V> streamCodec(
    encoder: (B, V) -> Unit,
    decoder: (B) -> V
) = StreamCodec.of(encoder, decoder)

fun <B, V> streamCodecMember(
    encoder: (V, B) -> Unit,
    decoder: (B) -> V
) = StreamCodec.ofMember(encoder, decoder)

fun <B, V> streamCodecUnit(value: V) = StreamCodec.unit<B, V>(value)
fun <V> streamCodecUnitSimple(value: V) = StreamCodec.unit<ByteBuf, V>(value)

fun <B, C, T1> streamCodecComposite(
    codec: StreamCodec<B, T1>,
    from: (C) -> T1,
    to: (T1) -> C
) = StreamCodec.composite(codec, { from(it) }, { to(it) })

fun <B, C, T1, T2> streamCodecComposite(
    codec1: StreamCodec<B, T1>,
    from1: (C) -> T1,
    codec2: StreamCodec<B, T2>,
    from2: (C) -> T2,
    to: (T1, T2) -> C
) = StreamCodec.composite(codec1, { from1(it) }, codec2, { from2(it) }, { t1, t2 -> to(t1, t2) })

fun <B, C, T1, T2, T3> streamCodecComposite(
    codec1: StreamCodec<B, T1>,
    from1: (C) -> T1,
    codec2: StreamCodec<B, T2>,
    from2: (C) -> T2,
    codec3: StreamCodec<B, T3>,
    from3: (C) -> T3,
    to: (T1, T2, T3) -> C
) = StreamCodec.composite(
    codec1,
    { from1(it) },
    codec2,
    { from2(it) },
    codec3,
    { from3(it) },
    { t1, t2, t3 -> to(t1, t2, t3) }
)

fun <B, C, T1, T2, T3, T4> streamCodecComposite(
    codec1: StreamCodec<B, T1>,
    from1: (C) -> T1,
    codec2: StreamCodec<B, T2>,
    from2: (C) -> T2,
    codec3: StreamCodec<B, T3>,
    from3: (C) -> T3,
    codec4: StreamCodec<B, T4>,
    from4: (C) -> T4,
    to: (T1, T2, T3, T4) -> C
) = StreamCodec.composite(
    codec1,
    { from1(it) },
    codec2,
    { from2(it) },
    codec3,
    { from3(it) },
    codec4,
    { from4(it) },
    { t1, t2, t3, t4 -> to(t1, t2, t3, t4) }
)

fun <B, C, T1, T2, T3, T4, T5> streamCodecComposite(
    codec1: StreamCodec<B, T1>,
    from1: (C) -> T1,
    codec2: StreamCodec<B, T2>,
    from2: (C) -> T2,
    codec3: StreamCodec<B, T3>,
    from3: (C) -> T3,
    codec4: StreamCodec<B, T4>,
    from4: (C) -> T4,
    codec5: StreamCodec<B, T5>,
    from5: (C) -> T5,
    to: (T1, T2, T3, T4, T5) -> C
) = StreamCodec.composite(
    codec1,
    { from1(it) },
    codec2,
    { from2(it) },
    codec3,
    { from3(it) },
    codec4,
    { from4(it) },
    codec5,
    { from5(it) },
    { t1, t2, t3, t4, t5 -> to(t1, t2, t3, t4, t5) }
)

fun <B, C, T1, T2, T3, T4, T5, T6> streamCodecComposite(
    codec1: StreamCodec<B, T1>,
    from1: (C) -> T1,
    codec2: StreamCodec<B, T2>,
    from2: (C) -> T2,
    codec3: StreamCodec<B, T3>,
    from3: (C) -> T3,
    codec4: StreamCodec<B, T4>,
    from4: (C) -> T4,
    codec5: StreamCodec<B, T5>,
    from5: (C) -> T5,
    codec6: StreamCodec<B, T6>,
    from6: (C) -> T6,
    to: (T1, T2, T3, T4, T5, T6) -> C
) = StreamCodec.composite(
    codec1,
    { from1(it) },
    codec2,
    { from2(it) },
    codec3,
    { from3(it) },
    codec4,
    { from4(it) },
    codec5,
    { from5(it) },
    codec6,
    { from6(it) },
    { t1, t2, t3, t4, t5, t6 -> to(t1, t2, t3, t4, t5, t6) }
)

fun <B, C> streamCodecRecursive(codecGetter: (StreamCodec<B, C>) -> StreamCodec<B, C>) =
    StreamCodec.recursive(codecGetter)