package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.surfapi.core.api.util.logger
import java.util.concurrent.ConcurrentLinkedQueue

interface TickablePacketListener : PacketListener {

    /**
     * Called every second
     */
    suspend fun tick()
}

abstract class CommonTickablePacketListener : TickablePacketListener {
    companion object {
        private val log = logger()
    }

    private val schedules = ConcurrentLinkedQueue<suspend () -> Unit>()

    override suspend fun tick() {
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

        tick0()
    }

    protected abstract suspend fun tick0()

    fun schedule(function: suspend () -> Unit) {
        schedules.add(function)
    }
}