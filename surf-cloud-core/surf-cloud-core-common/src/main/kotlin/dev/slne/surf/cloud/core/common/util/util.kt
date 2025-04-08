@file:OptIn(ExperimentalContracts::class)

package dev.slne.surf.cloud.core.common.util

import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.util.logger
import org.jetbrains.annotations.ApiStatus
import org.springframework.context.ApplicationEvent
import java.security.SecureRandom
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.streams.asSequence

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

fun <T> tempChangeSystemClassLoader(classLoader: ClassLoader, run: () -> T): T {
    contract {
        callsInPlace(run, InvocationKind.EXACTLY_ONCE)
    }

    val thread = Thread.currentThread()
    val originalClassLoader = thread.contextClassLoader

    return try {
        thread.contextClassLoader = classLoader
        run()
    } finally {
        thread.contextClassLoader = originalClassLoader
    }
}

private val STACK_WALK_INSTANCE: StackWalker =
    StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

fun getCallerClass() = getCallerClass(0)
fun getCallerClass(depth: Int) =
    STACK_WALK_INSTANCE.walk { it.asSequence().drop(3 + depth).firstOrNull()?.declaringClass }

fun checkCallerClass(expected: Class<*>) {
    if (getCallerClass(1) != expected) {
        throw IllegalStateException("Caller class is not $expected")
    }
}

fun checkInstantiationByServiceLoader() {
    check(getCallerClass(1)?.name?.startsWith("java.util.ServiceLoader") == true) { "Cannot instantiate instance directly" }
}

@ApiStatus.Internal
inline fun <reified T : Any> bean(name: String? = null): T = if (name == null) {
    coreCloudInstance.dataContext.getBean(T::class.java)
} else {
    coreCloudInstance.dataContext.getBean(name, T::class.java)
}

fun ApplicationEvent.publish() {
    coreCloudInstance.dataContext.publishEvent(this)
}