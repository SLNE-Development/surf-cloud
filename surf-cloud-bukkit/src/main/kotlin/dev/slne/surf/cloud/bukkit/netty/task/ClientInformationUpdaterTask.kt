package dev.slne.surf.cloud.bukkit.netty.task

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.server.state.ServerState
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundClientInformationPacket
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.event.Listener

object ClientInformationUpdaterTask : Listener {
    init {
        plugin.launch(plugin.asyncDispatcher) {
            while (true) {
                delay(1.ticks) // Yes, we really need to send it every tick
                sendUpdatedClientInformation()
            }
        }
    }

    private fun sendUpdatedClientInformation() {
        val info = ClientInformation(
            Bukkit.getMaxPlayers(),
            Bukkit.hasWhitelist(),
            ServerState.ONLINE
        )
        ServerboundClientInformationPacket(info).fireAndForget()
    }
}