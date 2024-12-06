package dev.slne.surf.cloud.api.common.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A utility class that observes a value and notifies listeners of changes to that value.
 *
 * @param T The type of the observed value.
 * @property getter A function to retrieve the current value.
 * @property cachedValue The initial cached value of the observed field, defaults to the value returned by [getter].
 * @property interval The time interval between checks for changes, defaults to 1 second.
 * @property customDispatcher An optional [CoroutineDispatcher] to use for scheduling tasks.
 */
class ObservableField<T>(
    private val getter: () -> T,
    private var cachedValue: T = getter(),
    private val interval: Duration = 1.seconds,
    customDispatcher: CoroutineDispatcher? = null
) {
    private val channel = Channel<T>(Channel.CONFLATED)
    private val listener = mutableObjectSetOf<(T) -> Unit>()

    init {
        val dispatcher = customDispatcher?.let { CoroutineScope(it + SupervisorJob()) }
            ?: ObservableCoroutineScope
        startObserving(dispatcher)
        startChannelProcessing(dispatcher)
    }

    /**
     * Starts a coroutine that periodically checks the value from [getter] and sends updates
     * to the channel if the value has changed.
     *
     * @param scope The [CoroutineScope] in which to launch the observing coroutine.
     */
    private fun startObserving(scope: CoroutineScope) = scope.launch {
        while (isActive) {
            delay(interval)
            val newValue = getter()
            if (newValue != cachedValue) {
                cachedValue = newValue
                channel.send(newValue)
            }
        }
    }

    /**
     * Starts a coroutine that processes values from the channel and notifies listeners
     * of changes to the observed value.
     *
     * @param scope The [CoroutineScope] in which to launch the channel processing coroutine.
     */
    private fun startChannelProcessing(scope: CoroutineScope) = scope.launch {
        for (value in channel) {
            listener.forEach { it(value) }
        }
    }

    /**
     * Adds a listener that will be notified of changes to the observed value.
     *
     * @param listener A callback function to invoke whenever the value changes.
     */
    fun observe(listener: (T) -> Unit) {
        this.listener.add(listener)
    }

    /**
     * A singleton object that provides a shared [CoroutineScope] for [ObservableField] instances.
     * This scope is backed by a cached thread pool and includes exception handling for uncaught exceptions.
     */
    @ApiStatus.Internal
    object ObservableCoroutineScope : CoroutineScope {
        private val log = logger()

        /**
         * The [CoroutineDispatcher] used by this scope, backed by a cached thread pool.
         */
        val dispatcher = Executors.newCachedThreadPool(threadFactory {
            nameFormat("observable-field-thread-%d")
            daemon(false)
            exceptionHandler { thread, throwable ->
                log.atSevere()
                    .withCause(throwable)
                    .log("Uncaught exception in observable field thread ${thread.name}")
            }
        }).asCoroutineDispatcher()

        override val coroutineContext =
            dispatcher + CoroutineName("observable-field") + SupervisorJob()
    }
}
