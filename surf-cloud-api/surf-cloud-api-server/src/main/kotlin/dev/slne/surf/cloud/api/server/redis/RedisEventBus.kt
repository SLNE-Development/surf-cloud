package dev.slne.surf.cloud.api.server.redis

import dev.slne.surf.cloud.api.common.CloudInstance

interface RedisEventBus {
    fun publish(event: RedisEvent)

    companion object : RedisEventBus by CloudInstance.instance.getBean(RedisEventBus::class.java)
}