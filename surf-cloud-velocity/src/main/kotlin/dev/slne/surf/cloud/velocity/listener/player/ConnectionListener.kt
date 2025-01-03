package dev.slne.surf.cloud.velocity.listener.player

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerConnectToServerPacket
import org.springframework.stereotype.Component

@Component
class ConnectionListener {

    @Subscribe(order = PostOrder.CUSTOM)
    fun onLogin(event: LoginEvent) {
        if (!event.result.isAllowed) return

        PlayerConnectToServerPacket(
            event.player.uniqueId,
            CloudPersistentData.SERVER_ID,
            true
        ).fireAndForget()
    }

    @Subscribe(order = PostOrder.CUSTOM)
    fun onDisconnect(event: DisconnectEvent) {
        PlayerConnectToServerPacket(
            event.player.uniqueId,
            CloudPersistentData.SERVER_ID,
            true
        ).fireAndForget()
    }
}