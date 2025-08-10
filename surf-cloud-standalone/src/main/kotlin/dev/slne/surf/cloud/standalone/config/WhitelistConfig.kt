package dev.slne.surf.cloud.standalone.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class WhitelistConfig(
    val enforcedGroups: MutableSet<String> = mutableSetOf(),
    val enforcedServers: MutableSet<String> = mutableSetOf(),
)