package dev.slne.surf.cloud.api.util

import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.Future
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <V> Future<V>.suspend(): V = suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel(true) }
    addListener { future ->
        if (it.isActive) {
            if (future.isSuccess) {
                @Suppress("UNCHECKED_CAST") // Future is generic, but we know it's the same type as the one we're suspending
                it.resume(future.now as V)
            } else {
                it.resumeWithException(future.cause())
            }
        }
    }
}

suspend fun <V : ChannelFuture> V.suspend(): V = suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel(true) }
    addListener { future ->
        if (it.isActive) {
            if (future.isSuccess) {
                it.resume(future.now as V)
            } else {
                it.resumeWithException(future.cause())
            }
        }
    }
}