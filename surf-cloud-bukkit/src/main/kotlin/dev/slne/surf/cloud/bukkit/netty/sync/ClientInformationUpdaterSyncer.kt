package dev.slne.surf.cloud.bukkit.netty.sync

import com.destroystokyo.paper.event.server.WhitelistToggleEvent
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.server.state.ServerState
import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.common.util.observer.ObservableField
import dev.slne.surf.cloud.bukkit.util.ObservableFieldByEvent
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound.ClientInformation
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound.ServerboundClientInformationPacket
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import org.bukkit.Bukkit
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.milliseconds

@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class ClientInformationUpdaterSyncer : CloudLifecycleAware {
    val maxPlayerWatcher by lazy {
        ObservableField(
            { Bukkit.getMaxPlayers() },
            interval = 50.milliseconds
        )
    }
    val whitelistWatcher by lazy {
        ObservableFieldByEvent<WhitelistToggleEvent, Boolean>(
            { isEnabled },
            Bukkit.hasWhitelist()
        )
    }

    override suspend fun afterStart(timeLogger: TimeLogger) {
        timeLogger.measureStep("Sending initial client information") {
            sendUpdatedClientInformation() // initially send
        }

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