package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundAddPlayerToServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundRemovePlayerFromServerPacket
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import java.util.*

class CommonStandaloneServerImpl : CommonStandaloneServer {
    override lateinit var wrapper: ServerCommonCloudServer

    override fun handlePlayerConnect(playerUUID: UUID) {
        (wrapper.users as UserListImpl).add(playerUUID)
        broadcast(ClientboundAddPlayerToServerPacket(wrapper.uid, playerUUID))
    }

    override fun handlePlayerDisconnect(playerUUID: UUID) {
        (wrapper.users as UserListImpl).remove(playerUUID)
        broadcast(ClientboundRemovePlayerFromServerPacket(wrapper.uid, playerUUID))
    }

    private fun send(packet: NettyPacket) {
        wrapper.connection.send(packet)
    }

    companion object {
        private val server by lazy { bean<NettyServerImpl>() }
        private fun broadcast(packet: NettyPacket) {
            server.connection.broadcast(packet)
        }
    }
}