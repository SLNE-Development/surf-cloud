package dev.slne.surf.cloud.api.common.util

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import java.util.function.ToIntFunction

interface StringRepresentable {
    val serializedName: String

    companion object {
        const val PRE_BUILT_MAP_THRESHOLD = 16

        fun <T : StringRepresentable> fromValues(values: () -> Array<T>): Codec<T> {
            val stringRepresentables = values()
            val nameLookup = createNameLookup(stringRepresentables) { it }
            val toIntFunction: ToIntFunction<T?> = createIndexLookup(stringRepresentables.toList())
            return StringRepresentableCodec(
                stringRepresentables,
                nameLookup,
                toIntFunction
            )
        }

        fun <T : StringRepresentable> createNameLookup(
            values: Array<T>,
            valueNameTransformer: (String) -> String
        ): (String?) -> T? {
            if (values.size > PRE_BUILT_MAP_THRESHOLD) {
                val map = values.associateBy { valueNameTransformer(it.serializedName) }
                return { if (it == null) null else map[it] }
            } else {
                return { name ->
                    values.find { valueNameTransformer(it.serializedName) == name }
                }
            }
        }
    }

    class StringRepresentableCodec<S : StringRepresentable>(
        values: Array<S>,
        idToIdentifiable: (String?) -> S?,
        identifiableToOrdinal: ToIntFunction<S?>
    ) : Codec<S> {
        private val codec = ExtraCodecs.orCompressed(
            Codec.stringResolver({ it?.serializedName }, idToIdentifiable),
            ExtraCodecs.idResolverCodec(
                identifiableToOrdinal,
                { ordinal -> if (ordinal >= 0 && ordinal < values.size) values[ordinal] else null },
                -1
            )
        )

        override fun <T> decode(
            dynamicOps: DynamicOps<T>,
            obj: T
        ): DataResult<Pair<S?, T>> {
            return codec.decode(dynamicOps, obj)
        }

        override fun <T> encode(
            stringRepresentable: S,
            dynamicOps: DynamicOps<T>,
            obj: T
        ): DataResult<T> {
            return codec.encode(stringRepresentable, dynamicOps, obj)
        }
    }
}