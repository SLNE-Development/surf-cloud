@file:OptIn(ExperimentalContracts::class)

package dev.slne.surf.cloud.api.common.util.codec

import com.mojang.datafixers.kinds.App
import com.mojang.datafixers.util.Either
import com.mojang.serialization.*
import com.mojang.serialization.Codec.recursive
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import java.util.*
import java.util.function.Function
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

@DslMarker
annotation class RecordCodecDsl

fun <T> createRecordCodec(@RecordCodecDsl block: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): Codec<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return RecordCodecBuilder.create(block)
}

fun <T> createRecordMapCodec(@RecordCodecDsl block: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): MapCodec<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return RecordCodecBuilder.mapCodec(block)
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
fun <T> RecordCodecBuilder.Instance<T>.stringOptionalLenient(
    fieldName: String,
    getter: T.() -> String?
) =
    Codec.STRING.lenientOptionalFieldOf(fieldName).forGetter<T> { Optional.ofNullable(getter(it)) }

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

@Suppress("UnusedReceiverParameter")
inline fun <T, reified E : Enum<E>> RecordCodecBuilder.Instance<T>.enum(
    fieldName: String,
    noinline getter: T.() -> E
) =
    ExtraCodecs.enumCodec(enumEntries<E>()).fieldOf(fieldName).getter(getter)

object ExtraCodecs {
    val JAVA = converter(JavaOps.INSTANCE);

    fun <T> converter(ops: DynamicOps<T>): Codec<T> =
        Codec.PASSTHROUGH.xmap({ it.convert(ops).value }, { Dynamic(ops, it as T) })


    fun <T : Enum<T>> enumCodec(entries: EnumEntries<T>) = Codec.STRING.validate { input ->
        if (entries.any { it.name == input })
            DataResult.success(input)
        else DataResult.error { "Invalid enum value: $input" }
    }.xmap({ input -> entries.first { it.name == input } }, { it.name })

    // region adventure
    val COMPONENT: Codec<Component> = recursive("cloud adventure Component", ::createComponentCodec)

//    private val TEXT_COMPONENT_MAP_CODEC: MapCodec<TextComponent> =
//        RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<TextComponent> ->
//            instance.group(
//                Codec.STRING.fieldOf("text")
//                    .forGetter { obj: TextComponent -> obj.content() })
//                .apply(
//                    instance
//                ) { content: String? ->
//                    Component.text(
//                        content!!
//                    )
//                }
//        }

    private val TEXT_COMPONENT_MAP_CODEC = createRecordMapCodec<TextComponent> {
        group(
            string("text") { content() }
        ).apply(this) { Component.text(it) }
    }

    private val TRANSLATABLE_PRIMITIVE_ARG_CODEC =
        JAVA.validate { if (it is Number || it is Boolean || it is String) DataResult.success(it) else DataResult.error { "This value needs to be parsed as component" } }
    private val TRANSLATABLE_ARG_CODEC =
        Codec.either(TRANSLATABLE_PRIMITIVE_ARG_CODEC, COMPONENT).flatXmap({ either ->
            either.map({
                val arg: TranslationArgument = when (it) {
                    is String -> TranslationArgument.component(Component.text(it))
                    is Boolean -> TranslationArgument.bool(it)
                    is Number -> TranslationArgument.numeric(it)
                    else -> return@map DataResult.error { "$it is not a valid translation argument primitive" }
                }
                DataResult.success(arg)
            }, { DataResult.success(TranslationArgument.component(it)) })
        }, {
            if (it.value() is Number || it.value() is Boolean) {
                return@flatXmap DataResult.success<Either<Any, Component>>(
                    Either.left<Any, Component>(
                        it.value()
                    )
                )
            }
            val component = it.asComponent()
            val collapsed = tryCollapseToString(component)
            if (collapsed != null) {
                return@flatXmap DataResult.success(Either.left<Any, Component>(collapsed)) // attempt to collapse all text components to strings
            }
            return@flatXmap DataResult.success<Either<Any, Component>>(
                Either.right<Any, Component>(
                    component
                )
            )
        })

    private val TRANSLATABLE_COMPONENT_MAP_CODEC = createRecordMapCodec<TranslatableComponent> {
        group(
            string("translatable") { key() },
            stringOptionalLenient("fallback") { fallback() },
            TRANSLATABLE_ARG_CODEC.listOf().optionalFieldOf("with").forGetter {
                if (it.arguments().isEmpty()) Optional.empty() else Optional.of(it.arguments())
            }
        ).apply(this) { key, fallback, components ->
            Component.translatable(
                key,
                components.orElseGet { emptyList() }).fallback(fallback.orElse(null))
        }
    }

    private val PLAIN_COMPONENT =
        ComponentType(TEXT_COMPONENT_MAP_CODEC, { it is TextComponent }, "text")

    private val TRANSLATABLE_COMPONENT = ComponentType(TRANSLATABLE_COMPONENT_MAP_CODEC, {it is TranslatableComponent}, "translatable")

    private fun createComponentCodec(selfCodec: Codec<Component>): Codec<Component> {

    }

    private data class ComponentType<C : Component>(
        val codec: MapCodec<C>,
        val test: (Component) -> Boolean,
        val serializedName: String
    )

    // endregion

}

fun tryCollapseToString(component: Component): String? {
    if (component is TextComponent) {
        if (component.children().isEmpty() && component.style().isEmpty) {
            return component.content()
        }
    }
    return null
}