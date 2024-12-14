package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerboundPingRequestPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundBundlePacket

interface ServerCommonPacketListener : ServerboundPacketListener {
    fun handleBundlePacket(packet: ServerboundBundlePacket)

    suspend fun handleKeepAlivePacket(packet: ServerboundKeepAlivePacket)

    fun handlePingRequest(packet: ServerboundPingRequestPacket)

}