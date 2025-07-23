package dev.slne.surf.cloud.core.common.event

import dev.slne.surf.cloud.api.common.event.CloudEvent

/**
 * Internal invoker abstraction (generated implementations call the target method without reflection).
 */
interface EventListenerInvoker {
    val owner: Any

    suspend fun invoke(event: CloudEvent)
}