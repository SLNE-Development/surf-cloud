package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.core.common.coroutines.QueueDisplayScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import kotlin.time.Duration.Companion.seconds

class ServerQueueDisplay(
    private val queue: SingleServerQueue
) {

    init {
        QueueDisplayScope.launch {
            display()
        }
    }

    private suspend fun display() {
        while (true) {
            delay(1.seconds)
            displayQueue()
        }
    }

    private suspend fun displayQueue() {
        val queueSnapshot = queue.snapshot()

        queueSnapshot.forEachIndexed { index, player ->
            player.sendActionBar(Component.text("Position in queue: ${index + 1}"))
        }
    }
}