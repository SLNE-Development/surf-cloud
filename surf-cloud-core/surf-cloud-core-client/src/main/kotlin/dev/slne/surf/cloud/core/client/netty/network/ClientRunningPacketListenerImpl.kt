package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.coroutines.NettyConnectionScope
import dev.slne.surf.cloud.core.common.coroutines.NettyListenerScope
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.protocol.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ClientRunningPacketListenerImpl(val connection: ConnectionImpl) : CommonTickablePacketListener(),
    RunningClientPacketListener {
    private val log = logger()

    override suspend fun tick0() {

    }

    override fun handleBundlePacket(packet: ClientboundBundlePacket) {
        NettyConnectionScope.launch {
            for (subPacket in packet.subPackets) {
                connection.handlePacket(subPacket)
            }
        }
    }

    override fun handleKeepAlive(packet: ClientboundKeepAlivePacket) {
        send(ServerboundKeepAlivePacket(packet.keepAliveId))
    }

    override fun handlePing(packet: ClientboundPingPacket) {
        send(ServerboundPongPacket(packet.pingId))
    }

    override fun handleDisconnect(packet: ClientboundDisconnectPacket) {
        connection.disconnect(packet.details)
    }

    override fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket) {
        playerManagerImpl.updateOrCreatePlayer(packet.uuid, packet.serverUid, packet.proxy)
        log.atInfo().log("Player ${packet.uuid} connected to server ${packet.serverUid}")
    }

    override fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket) {
        playerManagerImpl.updateOrRemoveOnDisconnect(packet.uuid, packet.serverUid, packet.proxy)
        log.atInfo().log("Player ${packet.uuid} disconnected from server ${packet.serverUid}")
    }

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val (proxiedSource, finalPacket) = when (packet) {
//            is ProxiedNettyPacket -> packet.source to packet.packet
            else -> null to packet
        }
        val info = NettyPacketInfo(this, proxiedSource)

        for (listener in listeners) {
            NettyListenerScope.launch {
                try {
                    listener.handle(finalPacket, info)
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .atMostEvery(5, TimeUnit.SECONDS)
                        .log(
                            "Failed to call listener %s for packet %s",
                            listener::class.simpleName,
                            finalPacket::class.simpleName
                        )
                }
            }
        }
    }

    override fun onDisconnect(details: DisconnectionDetails) {
        log.atInfo().log("Client disconnected with reason: ${details.reason}")
        // TODO: shutdown server if not already shutting down
    }

    fun send(packet: NettyPacket) {
        connection.send(packet)
    }
}