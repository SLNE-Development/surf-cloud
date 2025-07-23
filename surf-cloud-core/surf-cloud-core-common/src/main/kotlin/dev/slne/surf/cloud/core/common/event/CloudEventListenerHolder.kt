package dev.slne.surf.cloud.core.common.event

import org.springframework.core.ResolvableType
import java.util.concurrent.CopyOnWriteArrayList

class CloudEventListenerHolder {
    private val listeners = CopyOnWriteArrayList<ListenerWrapper>()

    fun isEmpty() = listeners.isEmpty()

    fun getListeners(type: ResolvableType) =
        listeners.filter { type.isAssignableFrom(it.genericType) }

    fun register(wrapper: ListenerWrapper) {
        listeners.addIfAbsent(wrapper)
        listeners.sort()
    }

    fun unregister(listener: Any) {
        listeners.removeIf { it.invoker.owner === listener }
    }
}