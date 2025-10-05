package dev.slne.surf.cloud.standalone.spring.config.redis

import com.mojang.serialization.Codec
import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.server.redis.RedisEvent
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.springframework.stereotype.Component


object RedisEventRegistry {
    private val eventClass2Metadata =
        Object2ObjectOpenHashMap<Class<out RedisEvent>, RedisEventMetadata<*>>()
    private val id2Metadata = Object2ObjectOpenHashMap<String, RedisEventMetadata<*>>()
    private var frozen = false

    fun registerAll(events: Map<Class<out RedisEvent>, RedisEventMetadata<*>>) {
        require(!frozen) { "Cannot register metadata after the registry was frozen" }
        for ((eventClass, metadata) in events) {
            val previousByClass = eventClass2Metadata.putIfAbsent(eventClass, metadata)
            require(previousByClass == null) { "Event class ${eventClass.name} is already registered" }
            val previousById = id2Metadata.putIfAbsent(metadata.id, metadata)
            require(previousById == null) { "Event id ${metadata.id} is already registered" }
        }
    }

    fun freeze() {
        frozen = true
    }

    fun <E : RedisEvent> getCodec(event: E): Codec<E>? {
        val eventClass = event.javaClass
        val metadata = eventClass2Metadata[eventClass] ?: return null

        @Suppress("UNCHECKED_CAST")
        return metadata.codec as Codec<E>
    }

    fun <E : RedisEvent> getCodec(id: String): Codec<E>? {
        val metadata = id2Metadata.get(id) ?: return null

        @Suppress("UNCHECKED_CAST")
        return metadata.codec as Codec<E>
    }

    fun getId(event: RedisEvent): String {
        val eventClass = event.javaClass
        val metadata = eventClass2Metadata.get(eventClass)
            ?: error("No metadata found for event class ${eventClass.name}")

        return metadata.id
    }
}

@Component
class RedisEventRegistryFreezer : CloudLifecycleAware {
    override suspend fun onEnable(timeLogger: TimeLogger) {
        RedisEventRegistry.freeze()
    }
}