@file:OptIn(ExperimentalContracts::class)

package dev.slne.surf.cloud.api.common.util

import org.jetbrains.annotations.Range
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@DslMarker
annotation class ThreadFactoryBuilderDsl

fun threadFactory(@ThreadFactoryBuilderDsl block: FactoryBuilder.() -> Unit): ThreadFactory {
    contract {
        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }

    return FactoryBuilder().apply(block).build()
}

@ThreadFactoryBuilderDsl
class FactoryBuilder {
    private var _nameFormat: String? = null
    private var _daemon: Boolean? = null
    private var _priority: Int? = null
    private var _uncaughtExceptionHandler: UncaughtExceptionHandler? = null
    private var _backingThreadFactory: ThreadFactory = Executors.defaultThreadFactory()

    fun nameFormat(format: String) {
        runCatching { String.format(format, 0) }.onFailure { error("Invalid name format: $format") }
        _nameFormat = format
    }

    fun daemon(daemon: Boolean) {
        _daemon = daemon
    }

    fun priority(
        priority: @Range(
            from = Thread.MIN_PRIORITY.toLong(),
            to = Thread.MAX_PRIORITY.toLong()
        ) Int
    ) {
        require(priority in Thread.MIN_PRIORITY..Thread.MAX_PRIORITY) { "Priority must be between ${Thread.MIN_PRIORITY} and ${Thread.MAX_PRIORITY}" }
        _priority = priority
    }

    fun factory(factory: ThreadFactory) {
        _backingThreadFactory = factory
    }

    fun uncaughtExceptionHandler(handler: UncaughtExceptionHandler) {
        _uncaughtExceptionHandler = handler
    }

    fun exceptionHandler(handler: (Thread, Throwable) -> Unit) {
        uncaughtExceptionHandler(handler)
    }

    internal fun build(): ThreadFactory {
        val counter = AtomicInteger()

        return ThreadFactory { run ->
            _backingThreadFactory.newThread(run)?.apply {
                _nameFormat?.let { name = String.format(it, counter.getAndIncrement()) }
                _daemon?.let { isDaemon = it }
                _priority?.let { priority = it }
                _uncaughtExceptionHandler?.let { uncaughtExceptionHandler = it }
            }
        }
    }
}