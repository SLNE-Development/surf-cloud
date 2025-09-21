package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundAddPlayerToServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundRemovePlayerFromServerPacket
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import java.util.*

class CommonStandaloneServerImpl : CommonStandaloneServer {
    override lateinit var wrapper: ServerCommonCloudServer

    override fun handlePlayerConnect(playerUUID: UUID) {
        (wrapper.users as UserListImpl).add(playerUUID)
        broadcast(ClientboundAddPlayerToServerPacket(wrapper.name, playerUUID))
    }

    override fun handlePlayerDisconnect(playerUUID: UUID) {
        (wrapper.users as UserListImpl).remove(playerUUID)
        broadcast(ClientboundRemovePlayerFromServerPacket(wrapper.name, playerUUID))
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

//abstract class CommonStandaloneServerImpl(
//    uid: Long,
//    group: String,
//    name: String,
//    users: UserListImpl,
//    information: ClientInformation,
//    override val connection: Connection,
//) : CommonCloudServerImpl(uid, group, name, users, information ), ServerCommonCloudServer {
//    private val connectionThrottlerer = Mutex()
//
//    fun addPlayer(player: CloudPlayer) {
//        users.add(player)
//    }
//
//    fun removePlayer(player: CloudPlayer) {
//        users.remove(player)
//    }
//
//    suspend fun connectPlayer(player: StandaloneCloudPlayerImpl): ConnectionResult {
//        connectionThrottlerer.withLock {
//            if (player.server == this || player.proxyServer == this) {
//                return ConnectionResult.ALREADY_CONNECTED
//            }
//
//            if (emptySlots <= 0) {
//                return ConnectionResult.SERVER_FULL
//            }
//        }
//    }
//}