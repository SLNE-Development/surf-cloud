package dev.slne.surf.cloud.api.server.redis

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class KotlinJsonSerializationCodec<A>(
    private val serializer: KSerializer<A>,
    private val json: Json
) : Codec<A> {
    private val codec = Codec.STRING.flatXmap<A>({ json ->
        try {
            DataResult.success(this.json.decodeFromString(serializer, json))
        } catch (e: SerializationException) {
            DataResult.error { "Unable to deserialize JSON: $json: ${e.message}" }
        } catch (e: IllegalArgumentException) {
            DataResult.error { "Cannot represent JSON as object: $json: ${e.message}" }
        }

    }, { obj ->
        try {
            DataResult.success(json.encodeToString(serializer, obj))
        } catch (e: SerializationException) {
            DataResult.error { "Unable to serialize object: $obj: ${e.message}" }
        }
    })


    override fun <T : Any> encode(
        input: A,
        ops: DynamicOps<T>,
        prefix: T
    ): DataResult<T> {
        return codec.encode(input, ops, prefix)
    }

    override fun <T : Any> decode(
        ops: DynamicOps<T>,
        input: T
    ): DataResult<Pair<A, T>> {
        return codec.decode(ops, input)
    }
}