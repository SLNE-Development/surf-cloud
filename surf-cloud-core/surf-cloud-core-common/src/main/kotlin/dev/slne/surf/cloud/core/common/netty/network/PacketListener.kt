package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

interface PacketListener {
    val flow: PacketFlow
    val protocol: ConnectionProtocol

    fun onDisconnect(details: DisconnectionDetails)
    fun createDisconnectionInfo(
        reason: DisconnectReason,
        additionalInfo: String? = null
    ): DisconnectionDetails =
        DisconnectionDetails(reason, additionalInfo)

    fun onPacketError(packet: NettyPacket, exception: Throwable) {}

    fun isAcceptingMessages(): Boolean
    fun shouldHandleMessage(packet: NettyPacket): Boolean = isAcceptingMessages()

}