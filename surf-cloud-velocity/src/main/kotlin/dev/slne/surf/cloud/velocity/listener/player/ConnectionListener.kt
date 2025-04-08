package dev.slne.surf.cloud.velocity.listener.player

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerConnectToServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerDisconnectFromServerPacket
import org.springframework.stereotype.Component
import java.net.Inet4Address

@Component
class ConnectionListener {

    @Subscribe(order = PostOrder.CUSTOM)
    fun onLogin(event: LoginEvent) {
        if (!event.result.isAllowed) return

        val player = event.player
        PlayerConnectToServerPacket(
            player.uniqueId,
            player.username,
            true,
            player.remoteAddress.address as? Inet4Address
                ?: error("Player address is not an Inet4Address"),
            CloudPersistentData.SERVER_ID
        ).fireAndForget()
    }

    @Subscribe(order = PostOrder.CUSTOM)
    fun onDisconnect(event: DisconnectEvent) {
        PlayerDisconnectFromServerPacket(
            event.player.uniqueId,
            CloudPersistentData.SERVER_ID,
            true
        ).fireAndForget()
    }
}