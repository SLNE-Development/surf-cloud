package dev.slne.surf.cloud.core.client.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerConnectToServerPacket
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import org.springframework.stereotype.Component

@Component
class PlayerConnectionPacketListener {

    @SurfNettyPacketHandler
    fun onPlayerConnectToServer(packet: PlayerConnectToServerPacket) {
        playerManagerImpl.addPlayer(packet.uuid, packet.serverUid, packet.proxy)
    }
}