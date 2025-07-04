package dev.slne.surf.cloud.api.common.util.observer

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun <T> observingFlow(getter: () -> T, interval: Duration = 1.seconds): Flow<T> = flow {
    emit(getter())
    while (currentCoroutineContext().isActive) {
        delay(interval)
        val current = getter()
        emit(current)
    }
}.distinctUntilChanged()