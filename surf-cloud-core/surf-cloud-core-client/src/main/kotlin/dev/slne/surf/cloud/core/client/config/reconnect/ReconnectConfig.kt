package dev.slne.surf.cloud.core.client.config.reconnect

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class ReconnectConfig(

    @Setting("min-delay-ms")
    val minDelay: Long = 200,

    @Setting("max-delay-ms")
    val maxDelay: Long = 15_000,

    @Setting("max-offline-time-ms")
    val maxOfflineTime: Long = 900_000L
)