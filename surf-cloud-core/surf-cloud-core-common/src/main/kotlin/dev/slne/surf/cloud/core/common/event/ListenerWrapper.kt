package dev.slne.surf.cloud.core.common.event

import dev.slne.surf.cloud.api.common.event.CloudEvent
import org.springframework.core.ResolvableType
import org.springframework.expression.Expression

data class ListenerWrapper(
    val eventType: Class<out CloudEvent>,
    val genericType: ResolvableType,
    val invoker: EventListenerInvoker,
    val priority: Int,
    val ignoreCancelled: Boolean,
    val condition: Expression?
) : Comparable<ListenerWrapper> {
    override fun compareTo(other: ListenerWrapper): Int {
        return priority.compareTo(other.priority)
    }
}
