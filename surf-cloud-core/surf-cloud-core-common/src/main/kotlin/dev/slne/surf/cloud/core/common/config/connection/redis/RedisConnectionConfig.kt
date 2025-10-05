package dev.slne.surf.cloud.core.common.config.connection.redis

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class RedisConnectionConfig(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String? = null,
    val eventChannel: String = "cloud:events"
)
