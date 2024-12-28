package dev.slne.surf.cloud.api.common.event

import org.springframework.context.ApplicationEvent

abstract class CloudEvent(source: Any) : ApplicationEvent(source)