package dev.slne.surf.cloud.standalone.config.playercache

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class PlayerCacheConfig(
    val hydrationChunkSize: Int = 50
)