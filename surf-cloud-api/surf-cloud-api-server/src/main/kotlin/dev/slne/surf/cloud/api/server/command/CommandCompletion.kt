package dev.slne.surf.cloud.api.server.command

import net.kyori.adventure.text.Component

data class CommandCompletion(
    val suggestion: String,
    val tooltip: Component? = null,
)