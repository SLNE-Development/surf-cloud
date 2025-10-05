package dev.slne.surf.cloud.standalone.spring.config.redis

import com.mojang.serialization.Codec
import dev.slne.surf.cloud.api.server.redis.RedisEvent

data class RedisEventMetadata<E: RedisEvent>(
    val codec: Codec<E>,
    val id: String
)
