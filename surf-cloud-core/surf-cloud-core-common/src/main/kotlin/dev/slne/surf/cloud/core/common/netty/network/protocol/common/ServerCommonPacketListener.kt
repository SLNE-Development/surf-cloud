package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener

interface ServerCommonPacketListener : ServerboundPacketListener {
    fun handleBundlePacket(packet: ServerboundBundlePacket)

    suspend fun handleKeepAlivePacket(packet: KeepAlivePacket)

    fun handlePingRequest(packet: ServerboundPingRequestPacket)

}