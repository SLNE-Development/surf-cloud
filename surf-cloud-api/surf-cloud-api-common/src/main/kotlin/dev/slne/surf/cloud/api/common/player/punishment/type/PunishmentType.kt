package dev.slne.surf.cloud.api.common.player.punishment.type

import dev.slne.surf.cloud.api.common.util.ByIdMap.OutOfBoundsStrategy
import dev.slne.surf.cloud.api.common.util.IdRepresentable
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.Component

enum class PunishmentType(
    override val id: Int,
    val punishDisplay: Component,
    val unpunishDisplay: Component = punishDisplay
) : IdRepresentable {
    WARN(0, buildText { error("Verwarnung") }),
    KICK(1, buildText { error("Kick") }),
    MUTE(2, buildText { error("Mute") }, buildText { success("Unmute") }),
    BAN(3, buildText { error("Bann") }, buildText { success("Entbannung") });

    companion object {
        val BY_ID = IdRepresentable.enumIdMap<PunishmentType>(OutOfBoundsStrategy.DECODE_ERROR)
        val STREAM_CODEC = IdRepresentable.codec(BY_ID)
    }
}