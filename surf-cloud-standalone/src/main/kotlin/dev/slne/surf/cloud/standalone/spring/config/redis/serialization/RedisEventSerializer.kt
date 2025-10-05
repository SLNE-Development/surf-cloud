package dev.slne.surf.cloud.standalone.spring.config.redis.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import dev.slne.surf.cloud.api.server.redis.RedisEvent
import dev.slne.surf.cloud.standalone.spring.config.redis.FasterXmlJsonOps
import dev.slne.surf.cloud.standalone.spring.config.redis.RedisEventRegistry
import java.io.Serial

class RedisEventSerializer :
    StdSerializer<RedisEvent>(RedisEvent::class.java) {
    override fun serialize(
        value: RedisEvent,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        val codec = RedisEventRegistry.getCodec(value)
        if (codec == null) {
            serializers.reportMappingProblem("No codec found for class ${value.javaClass.name}")
            return
        }

        val id = RedisEventRegistry.getId(value)
        gen.writeStartObject()
        gen.writeStringField("id", id)
        gen.writeFieldName("data")
        val result = codec.encodeStart(FasterXmlJsonOps.Companion.INSTANCE, value)
            .resultOrPartial { msg ->
                serializers.reportMappingProblem("Failed to encode ${value.javaClass.name} to JSON: $msg")
            }

        result.ifPresentOrElse({
            gen.writeTree(it)
            gen.writeEndObject()
        }, {
            serializers.reportMappingProblem("Failed to encode ${value.javaClass.name} to JSON")
        })
    }


    companion object {
        @Serial
        private const val serialVersionUID: Long = -5183783397202605807L
    }
}