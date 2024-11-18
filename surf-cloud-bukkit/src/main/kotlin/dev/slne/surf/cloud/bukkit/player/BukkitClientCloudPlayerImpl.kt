package dev.slne.surf.cloud.bukkit.player

import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import org.bukkit.Bukkit
import java.util.*

class BukkitClientCloudPlayerImpl(uuid: UUID) : ClientCloudPlayerImpl(uuid) {
    override val audience get() = Bukkit.getPlayer(uuid)
}