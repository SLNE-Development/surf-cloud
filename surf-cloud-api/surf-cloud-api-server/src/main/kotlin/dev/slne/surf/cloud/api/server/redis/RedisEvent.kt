package dev.slne.surf.cloud.api.server.redis

import dev.slne.surf.cloud.api.common.util.findAnnotation

abstract class RedisEvent {
    @delegate:Transient
    val meta by lazy {
        this::class.findAnnotation<RedisEventMeta>()
            ?: error("@RedisEventMeta annotation is missing on ${this::class.qualifiedName}")
    }

    fun publish() {
        RedisEventBus.publish(this)
    }

    override fun toString(): String {
        return "RedisEvent(meta=$meta)"
    }
}