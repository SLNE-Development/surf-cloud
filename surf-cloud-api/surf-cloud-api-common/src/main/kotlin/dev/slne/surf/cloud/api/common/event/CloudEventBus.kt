package dev.slne.surf.cloud.api.common.event

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface CloudEventBus {

    fun register(listener: Any)
    fun unregister(listener: Any)
    suspend fun post(event: CloudEvent)

    companion object : CloudEventBus by instance {
        @InternalApi
        val instance: CloudEventBus = dev.slne.surf.cloud.api.common.event.instance
    }
}

private val instance = requiredService<CloudEventBus>()