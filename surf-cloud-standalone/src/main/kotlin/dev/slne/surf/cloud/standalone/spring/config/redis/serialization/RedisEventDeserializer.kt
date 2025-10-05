package dev.slne.surf.cloud.standalone.spring.config.redis.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import dev.slne.surf.cloud.api.server.redis.RedisEvent
import dev.slne.surf.cloud.standalone.spring.config.redis.FasterXmlJsonOps
import dev.slne.surf.cloud.standalone.spring.config.redis.RedisEventRegistry
import java.io.Serial

class RedisEventDeserializer :
    StdDeserializer<RedisEvent>(RedisEvent::class.java) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): RedisEvent {
        val node = p.readValueAsTree<ObjectNode>()
        val id = node.get("id")?.asText() ?: ctxt.reportInputMismatch(
            RedisEvent::class.java,
            "Missing 'id' field in RedisEvent JSON"
        )
        val data = node.get("data") ?: ctxt.reportInputMismatch(
            RedisEvent::class.java,
            "Missing 'data' field in RedisEvent JSON"
        )

        val codec = RedisEventRegistry.getCodec<RedisEvent>(id) ?: ctxt.reportInputMismatch(
            RedisEvent::class.java,
            "No codec found for RedisEvent with id '$id'"
        )

        val result = codec.decode(FasterXmlJsonOps.INSTANCE, data)
            .resultOrPartial { msg ->
                ctxt.reportInputMismatch(
                    RedisEvent::class.java,
                    "Failed to decode RedisEvent with id '$id' from JSON: $msg"
                )
            }.map { it.first }

        return result.orElseGet {
            ctxt.reportInputMismatch(
                RedisEvent::class.java,
                "Failed to decode RedisEvent with id '$id' from JSON"
            )
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 7420363236179113904L
    }
}