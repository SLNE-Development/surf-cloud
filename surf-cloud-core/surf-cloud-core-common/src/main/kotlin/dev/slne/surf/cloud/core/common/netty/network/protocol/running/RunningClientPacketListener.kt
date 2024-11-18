package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener

interface RunningClientPacketListener: ClientboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handleBundlePacket(packet: ClientboundBundlePacket)

    fun handleKeepAlive(packet: ClientboundKeepAlivePacket)

    fun handlePing(packet: ClientboundPingPacket)

    fun handleDisconnect(packet: ClientboundDisconnectPacket)

    fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket)

    fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket)

    fun handlePacket(packet: NettyPacket)
}