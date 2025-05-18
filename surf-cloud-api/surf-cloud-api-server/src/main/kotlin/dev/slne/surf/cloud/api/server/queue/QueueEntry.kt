package dev.slne.surf.cloud.api.server.queue

import java.util.*

interface QueueEntry : Comparable<QueueEntry> {
    val uuid: UUID
    val priority: Int
    val bypassFull: Boolean
    val bypassQueue: Boolean
    val awaitingConnection: Boolean
    val connectionAttempts: Int
    val preferredServerUid: Long?

    fun hasConnectionAttemptsLeft(): Boolean
}