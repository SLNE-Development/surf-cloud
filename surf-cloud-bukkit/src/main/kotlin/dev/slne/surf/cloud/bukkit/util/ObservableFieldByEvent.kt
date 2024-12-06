package dev.slne.surf.cloud.bukkit.util

import dev.slne.surf.cloud.api.common.util.ObservableField
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.surfapi.bukkit.api.event.listen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

class ObservableFieldByEvent<E : Event, T>(
    plugin: Plugin,
    eventClass: KClass<E>,
    private val getter: E.() -> T,

    @Volatile
    private var currentValue: T,
) {
    private val channel = Channel<T>(Channel.CONFLATED)
    private val listener = mutableObjectSetOf<(T) -> Unit>()

    init {
        listen(plugin, eventClass) {
            val newValue = getter()
            if (newValue != currentValue) {
                currentValue = newValue
                ObservableField.ObservableCoroutineScope.launch {
                    channel.send(currentValue)
                }
            }
        }

        ObservableField.ObservableCoroutineScope.launch {
            for (value in channel) {
                listener.forEach { it(value) }
            }
        }
    }

    fun observe(listener: (T) -> Unit) {
        this.listener.add(listener)
    }
}