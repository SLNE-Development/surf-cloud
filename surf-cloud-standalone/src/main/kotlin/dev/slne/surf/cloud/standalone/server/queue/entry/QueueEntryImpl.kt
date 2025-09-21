package dev.slne.surf.cloud.standalone.server.queue.entry

import dev.slne.surf.cloud.api.common.util.getValue
import dev.slne.surf.cloud.api.common.util.setValue
import dev.slne.surf.cloud.api.server.queue.QueueEntry
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

data class QueueEntryImpl(
    val handle: PlayerQueueHandle,
    override var priority: Int,
    override val bypassFull: Boolean,
    override val bypassQueue: Boolean,
    override val preferredServerName: String? = null,
) : QueueEntry {
    override var awaitingConnection by AtomicBoolean()
    override var connectionAttempts = 0

    override fun hasConnectionAttemptsLeft() =
        connectionAttempts < bean<StandaloneConfigHolder>().config.queue.maxConnectionAttempts

    override val uuid: UUID
        get() = handle.uuid

    override fun compareTo(other: QueueEntry): Int = when {
        priority != other.priority -> other.priority.compareTo(priority)
        preferredServerName != null && other.preferredServerName == null -> -1
        preferredServerName == null && other.preferredServerName != null -> 1
        else -> 0
    }
}
