package dev.slne.surf.cloud.api.common.event

import org.intellij.lang.annotations.Language

/**
 * Marks a subscriber method. The method must have exactly one parameter that subclasses [CloudEvent].
 * Suspend functions are allowed and will run on the [CloudEventBus]' coroutineScope.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CloudEventHandler(
    val priority: Int = NO_PRIORITY,
    val ignoreCancelled: Boolean = false,
    @Language("SpEL") val condition: String = "true"
) {
    companion object {
        const val NO_PRIORITY = 0
        const val LOWEST_PRIORITY = Int.MIN_VALUE
        const val LOW_PRIORITY = -100
        const val NORMAL_PRIORITY = 0
        const val HIGH_PRIORITY = 100
        const val HIGHEST_PRIORITY = Int.MAX_VALUE
    }
}
