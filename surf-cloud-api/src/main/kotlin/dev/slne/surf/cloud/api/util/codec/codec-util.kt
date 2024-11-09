@file:OptIn(ExperimentalContracts::class)

package dev.slne.surf.cloud.api.util.codec

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.checkerframework.checker.units.qual.A
import java.util.function.Function
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@DslMarker
annotation class RecordCodecDsl

fun <T> createRecordCodec(@RecordCodecDsl block: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): Codec<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return RecordCodecBuilder.create(block)
}

fun <A, O> MapCodec<A>.getter(getter: O.() -> A): RecordCodecBuilder<O, A> =
    this.forGetter(Function(getter))

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.int(fieldName: String, getter: T.() -> Int) =
    Codec.INT.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.bool(fieldName: String, getter: T.() -> Boolean) =
    Codec.BOOL.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.string(fieldName: String, getter: T.() -> String) =
    Codec.STRING.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.float(fieldName: String, getter: T.() -> Float) =
    Codec.FLOAT.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.double(fieldName: String, getter: T.() -> Double) =
    Codec.DOUBLE.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.long(fieldName: String, getter: T.() -> Long) =
    Codec.LONG.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.byte(fieldName: String, getter: T.() -> Byte) =
    Codec.BYTE.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.short(fieldName: String, getter: T.() -> Short) =
    Codec.SHORT.fieldOf(fieldName).getter(getter)