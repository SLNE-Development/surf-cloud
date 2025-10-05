package dev.slne.surf.cloud.standalone.spring.config.redis

import dev.slne.surf.cloud.api.server.redis.RedisEvent
import dev.slne.surf.cloud.api.server.redis.RedisEventBus
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisEventBusImpl(
    private val template: ReactiveRedisTemplate<String, RedisEvent>,
    private val configHolder: StandaloneConfigHolder
) : RedisEventBus {
    private val log = logger()

    override fun publish(event: RedisEvent) {
        val eventChannel = configHolder.config.connectionConfig.redis.eventChannel

        template.convertAndSend(eventChannel, event)
            .doOnError { error ->
                log.atWarning()
                    .withCause(error)
                    .log("Failed to publish Redis event: $event")
            }
            .subscribe()
    }
}