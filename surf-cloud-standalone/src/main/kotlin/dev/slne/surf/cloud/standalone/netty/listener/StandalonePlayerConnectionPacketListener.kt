package dev.slne.surf.cloud.standalone.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerConnectToServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerDisconnectFromServerPacket
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import org.springframework.stereotype.Component

@Component
class StandalonePlayerConnectionPacketListener(private val server: NettyServerImpl) {

    @SurfNettyPacketHandler
    fun onPlayerConnectToServer(packet: PlayerConnectToServerPacket) {
        playerManagerImpl.updateOrCreatePlayer(packet.uuid, packet.serverUid, packet.proxy)
        server.connection.broadcast(packet)
    }

    @SurfNettyPacketHandler
    fun onPlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket) {
        playerManagerImpl.updateOrRemoveOnDisconnect(packet.uuid, packet.serverUid, packet.proxy)
        server.connection.broadcast(packet)
    }
}