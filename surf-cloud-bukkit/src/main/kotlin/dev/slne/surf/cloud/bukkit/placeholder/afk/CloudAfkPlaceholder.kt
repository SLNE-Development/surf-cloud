package dev.slne.surf.cloud.bukkit.placeholder.afk

import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiPlaceholder
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.OfflinePlayer

object CloudAfkPlaceholder : PapiPlaceholder("afk") {
    private const val NOT_AFK_STRING = ""
    private val afkString = LegacyComponentSerializer.legacySection().serialize(buildText {
        spacer("[")
        text("💤")
        spacer("]")
    })

    override fun parse(
        player: OfflinePlayer,
        args: List<String>
    ): String? {
        val cloudPlayer = CloudPlayerManager.getPlayer(player.uniqueId) ?: return null
        if (!cloudPlayer.isAfk()) {
            return NOT_AFK_STRING
        }

        return afkString
    }
}