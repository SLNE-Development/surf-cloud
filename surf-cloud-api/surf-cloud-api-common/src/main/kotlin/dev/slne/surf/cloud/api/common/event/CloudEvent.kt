package dev.slne.surf.cloud.api.common.event

import org.springframework.context.ApplicationEvent
import java.io.Serial

/**
 * Base class for all cloud-related events.
 *
 * @param source The object on which the event initially occurred.
 */
abstract class CloudEvent(source: Any) : ApplicationEvent(source) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -3330199602759544854L
    }
}