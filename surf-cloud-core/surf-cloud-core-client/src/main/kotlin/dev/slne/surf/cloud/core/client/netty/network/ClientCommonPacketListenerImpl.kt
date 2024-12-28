package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundKeepAlivePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerboundKeepAlivePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPingPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundPongPacket
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

abstract class ClientCommonPacketListenerImpl(
    val connection: ConnectionImpl,
): ClientCommonPacketListener {
    private val log = logger()

    override fun handleKeepAlive(packet: ClientboundKeepAlivePacket) {
        send(ServerboundKeepAlivePacket(packet.keepAliveId))
    }

    override fun handlePing(packet: ClientboundPingPacket) {
        send(ServerboundPongPacket(packet.pingId))
    }

    override fun handleDisconnect(packet: ClientboundDisconnectPacket) {
        connection.disconnect(packet.details)
    }

    override fun handleBundlePacket(packet: ClientboundBundlePacket) {
        ConnectionManagementScope.launch {
            for (subPacket in packet.subPackets) {
                connection.handlePacket(subPacket)
            }
        }
    }


    override suspend fun onDisconnect(details: DisconnectionDetails) {
        log.atInfo().log("Client disconnected with reason: ${details.reason}")
        // TODO: shutdown server if not already shutting down
        exitProcess(0)
    }

    fun send(packet: NettyPacket) {
        connection.send(packet)
    }
}