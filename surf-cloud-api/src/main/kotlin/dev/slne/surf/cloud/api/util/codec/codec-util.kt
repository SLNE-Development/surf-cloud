@file:OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)

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
import kotlin.experimental.ExperimentalTypeInference

@DslMarker
annotation class RecordCodecDsl

@DslMarker
annotation class GroupDsl

fun <T> createRecordCodec( @RecordCodecDsl block: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): Codec<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return RecordCodecBuilder.create(block)
}

fun <A, O> MapCodec<A>.getter(getter: O.() -> A): RecordCodecBuilder<O, A> =
    this.forGetter(Function(getter))
