package dev.slne.surf.cloud.core.util

import dev.slne.surf.cloud.api.util.logger
import java.security.SecureRandom

inline fun <reified T> Iterator<T>.toArray(size: Int) = Array(size) { next() }
inline fun <reified T> Iterable<T>.toArray(): Array<T> = iterator().toArray(count())
inline fun <reified T> Sequence<T>.toArray() = iterator().toArray(count())

val random: SecureRandom by lazy {
    try {
        SecureRandom.getInstanceStrong()
    } catch (e: Exception) {
        logger().atWarning()
            .withCause(e)
            .log("Failed to get strong SecureRandom, falling back to default")
        SecureRandom()
    }
}