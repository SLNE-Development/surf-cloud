package dev.slne.surf.cloud.api.common.player.punishment.type

import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.Component

enum class PunishmentType(val punishDisplay: Component, val unpunishDisplay: Component = punishDisplay) {
    WARN(buildText { error("Verwarnung") }),
    KICK(buildText { error("Kick") }),
    MUTE(buildText { error("Mute") }, buildText { success("Unmute") }),
    BAN(buildText { error("Bann") }, buildText { success("Entbannung") }),
}