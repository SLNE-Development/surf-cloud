package dev.slne.surf.cloud.standalone.temp

import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import dev.slne.surf.cloud.api.server.CloudServer
import dev.slne.surf.cloud.api.server.state.ServerState
import dev.slne.surf.cloud.core.data.CloudPersistentData
import dev.slne.surf.cloud.core.netty.client.source.NettyClientSourceImpl
import dev.slne.surf.cloud.core.netty.protocol.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoAction
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoBatchPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudServerRegistrationData
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientJoinNettyPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientQuitPacket
import dev.slne.surf.cloud.core.server.CloudServerImpl
import dev.slne.surf.cloud.core.util.random
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("independent")
class SurfNettyServerPacketHandler(private val nettyServerImpl: NettyServerImpl) {
    @SurfNettyPacketHandler
    fun onCloudServerRegistration(packet: CloudRegisterServerPacket, info: NettyPacketInfo) {
        if (packet.type != CloudRegisterServerPacket.Type.FETCH_PRELOAD) {
            return
        }

        val data = packet.data

        val serverId = if (data.serverId == CloudPersistentData.SERVER_ID_NOT_SET) {
            random.nextLong(0xffffff)
        } else {
            data.serverId
        }

        val server = CloudServerImpl(
            port = data.port,
            host = data.host,
            category = data.category,
            serverGuid = serverId,
            state = ServerState.RESTARTING
        )

        val clientSource = info.asClientSource() as NettyClientSourceImpl
        clientSource.cloudServer = server
        clientSource.broadcastServerInfo(CloudServerInfoAction.ADD_SERVER_INFO)

        val preload = CloudRegisterServerPacket(
            type = CloudRegisterServerPacket.Type.PRELOAD,
            data = CloudServerRegistrationData(
                serverId = server.serverGuid,
                category = server.category,
                port = server.port,
                host = server.host,
            )
        )

        clientSource.sendPacket(preload)
    }

    @SurfNettyPacketHandler
    fun onCloudServerInfo(packet: CloudServerInfoPacket, info: NettyPacketInfo) {
        handleServerInfo(packet.server, packet.action)
        broadcastPacket(info.asClientSource(), packet)
    }

    @SurfNettyPacketHandler
    fun onCloudServerInfoBatch(packet: CloudServerInfoBatchPacket, info: NettyPacketInfo) {
        for (server in packet.servers) {
            handleServerInfo(server, packet.action)
        }
        broadcastPacket(info.asClientSource(), packet)
    }

    @SurfNettyPacketHandler
    fun onClientQuit(packet: ClientQuitPacket?, info: NettyPacketInfo) {
        val client = info.asClientSource() as NettyClientSourceImpl
        val server = client.cloudServer ?: return

        server.state = ServerState.OFFLINE
        client.broadcastServerInfo(CloudServerInfoAction.REMOVE_SERVER_INFO)
        client.cloudServer = null
    }

    @SurfNettyPacketHandler
    fun onClientJoin(packet: ClientJoinNettyPacket?, info: NettyPacketInfo) {
        val server = nettyServerImpl.connection.clientTracker.clients.asSequence()
            .filter { it.hasServerGuid() }
            .mapNotNull { it.cloudServer }
            .toSet()

        when (server.size) {
            0 -> {
            }

            1 -> info.source.sendPacket(
                CloudServerInfoPacket(
                    CloudServerInfoAction.ADD_SERVER_INFO,
                    server.iterator().next()
                )
            )

            else -> info.source.sendPacket(

                CloudServerInfoBatchPacket(
                    CloudServerInfoAction.ADD_SERVER_INFO,
                    server
                )
            )
        }
    }

    private fun handleServerInfo(server: CloudServer, action: CloudServerInfoAction) {
        when (action) {
            CloudServerInfoAction.UPDATE_SERVER_INFO -> {
                val list = nettyServerImpl.connection.clientTracker
                val clientSource =
                    list.findByServerGuid(server.serverGuid) as? NettyClientSourceImpl
                        ?: error("Client not found")

                clientSource.updateServerInfo(server)
            }

            else -> {
            }
        }
    }

    private fun broadcastPacket(sender: NettyClientSource, packet: NettyPacket<*>) {
        nettyServerImpl.connection.clientTracker.clients
            .filter { it != sender }
            .forEach { it.sendPacket(packet) }
    }
}
