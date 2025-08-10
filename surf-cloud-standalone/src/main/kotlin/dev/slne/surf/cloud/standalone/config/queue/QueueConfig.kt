package dev.slne.surf.cloud.standalone.config.queue

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class QueueConfig(
    val maxConnectionAttempts: Int = 3,
    val multiQueue: Boolean = true,
    val allowJoiningSuspendedQueue: Boolean = false,
    val removePlayerOnServerSwitch: Boolean = true,

    /** How many minutes a queue is held after the last access. */
    val cacheRetainMinutes: Int = 30,

    val suspendedQueueCharacter: Char = '⏸',
)