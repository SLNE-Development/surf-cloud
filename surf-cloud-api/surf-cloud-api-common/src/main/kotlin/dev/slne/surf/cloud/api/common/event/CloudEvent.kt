package dev.slne.surf.cloud.api.common.event

import java.io.Serial

/**
 * Base class for all cloud-related events.
 *
 * @param source The object on which the event initially occurred.
 */
abstract class CloudEvent(source: Any) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -3330199602759544854L
    }

    suspend fun post() {
        CloudEventBus.instance.post(this)
    }

    fun postAndForget() {
        CloudEventBus.instance.postAndForget(this)
    }
}