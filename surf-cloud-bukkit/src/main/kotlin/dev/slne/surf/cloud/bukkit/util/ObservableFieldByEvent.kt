package dev.slne.surf.cloud.bukkit.util

import dev.slne.surf.cloud.api.common.util.observer.ObservableField.ObservableCoroutineScope
import dev.slne.surf.surfapi.bukkit.api.event.listen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

class ObservableFieldByEvent<E : Event, T>(
    plugin: Plugin,
    eventClass: KClass<E>,
    private val getter: E.() -> T,

    @Volatile
    private var currentValue: T,
    customDispatcher: CoroutineDispatcher? = null
) {
    private val channel = Channel<T>(Channel.CONFLATED)
    private val listener = CopyOnWriteArrayList<(T) -> Unit>()

    init {
        val dispatcher = customDispatcher?.let { CoroutineScope(it + SupervisorJob()) }
            ?: ObservableCoroutineScope

        listen(plugin, eventClass) {
            val newValue = getter()
            if (newValue != currentValue) {
                currentValue = newValue
                dispatcher.launch {
                    channel.send(currentValue)
                }
            }
        }

        dispatcher.launch {
            for (value in channel) {
                listener.forEach { it(value) }
            }
        }
    }

    fun observe(listener: (T) -> Unit) {
        this.listener.add(listener)
    }
}

inline fun <reified E : Event, T> Any.ObservableFieldByEvent(
    noinline getter: E.() -> T,
    currentValue: T,
    customDispatcher: CoroutineDispatcher? = null
) = ObservableFieldByEvent(
    JavaPlugin.getProvidingPlugin(this::class.java),
    E::class,
    getter,
    currentValue,
    customDispatcher
)