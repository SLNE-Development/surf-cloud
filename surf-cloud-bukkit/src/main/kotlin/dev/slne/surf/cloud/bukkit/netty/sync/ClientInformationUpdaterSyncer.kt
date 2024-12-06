package dev.slne.surf.cloud.bukkit.netty.sync

import com.destroystokyo.paper.event.server.WhitelistToggleEvent
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.server.state.ServerState
import dev.slne.surf.cloud.api.common.util.ObservableField
import dev.slne.surf.cloud.bukkit.util.ObservableFieldByEvent
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundClientInformationPacket
import org.bukkit.Bukkit
import kotlin.time.Duration.Companion.milliseconds

object ClientInformationUpdaterSyncer {
    val maxPlayerWatcher = ObservableField({ Bukkit.getMaxPlayers() }, interval = 50.milliseconds)
    val whitelistWatcher =
        ObservableFieldByEvent<WhitelistToggleEvent, Boolean>({ isEnabled }, Bukkit.hasWhitelist())

    init {
        sendUpdatedClientInformation() // initially send

        maxPlayerWatcher.observe { sendUpdatedClientInformation() }
        whitelistWatcher.observe { sendUpdatedClientInformation() }
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