package dev.slne.surf.cloud.core.common.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

class SyncSignal {

    private val channel = Channel<Unit>(Channel.CONFLATED)

    suspend fun awaitNext() {
        channel.receive()
    }

    suspend fun onNext(block: () -> Unit) {
        channel.consumeEach { block() }
    }

    fun fire() {
        channel.trySend(Unit)
    }
}