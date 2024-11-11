package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface TickablePacketListener : PacketListener {

    /**
     * Called every second
     */
    suspend fun tick()
}

abstract class CommonTickablePacketListener : TickablePacketListener {
    private val schedules = mutableObjectListOf<suspend () -> Unit>().synchronize()
    private val schedulesMutex = Mutex()

    override suspend fun tick() {
        schedulesMutex.withLock {
            schedules.forEach { it() }
            schedules.clear()
        }
        tick0()
    }

    protected abstract suspend fun tick0()

    suspend fun schedule(function: suspend () -> Unit) {
        schedulesMutex.withLock {
            schedules.add(function)
        }
    }
}