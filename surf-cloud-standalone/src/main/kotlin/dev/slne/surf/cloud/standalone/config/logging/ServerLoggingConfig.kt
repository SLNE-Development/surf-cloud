package dev.slne.surf.cloud.standalone.config.logging

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ServerLoggingConfig(
    val logPlayerConnections: Boolean = true,
)