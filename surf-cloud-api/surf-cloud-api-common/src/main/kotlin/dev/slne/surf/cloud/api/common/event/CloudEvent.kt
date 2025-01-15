package dev.slne.surf.cloud.api.common.event

import org.springframework.context.ApplicationEvent

/**
 * Base class for all cloud-related events.
 *
 * @param source The object on which the event initially occurred.
 */
abstract class CloudEvent(source: Any) : ApplicationEvent(source)