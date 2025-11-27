package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.surfapi.core.api.util.logger
import java.util.concurrent.ConcurrentLinkedQueue

interface TickablePacketListener : PacketListener {

    /**
     * Called every second
     */
    fun tickSecond()
}

abstract class CommonTickablePacketListener : TickablePacketListener {
    companion object {
        private val log = logger()
    }

    private val schedules = ConcurrentLinkedQueue<() -> Unit>()

    final override fun tickSecond() {
        while (true) {
            val task = schedules.poll() ?: break

            try {
                task()
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Error while executing scheduled task")
            }
        }

        tickSecond0()
    }

    protected abstract fun tickSecond0()

    fun schedule(function: () -> Unit) {
        schedules.add(function)
    }
}