package dev.slne.surf.cloud.bukkit.listener.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerConnectToServerPacket
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

object ConnectionListener: Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun AsyncPlayerPreLoginEvent.onAsyncPlayerPreLogin() {
        if (loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return
        }

        PlayerConnectToServerPacket(
            uniqueId,
            CloudPersistentData.SERVER_ID.value(),
            false
        ).fireAndForget()
    }
}