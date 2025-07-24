package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.bidirectional.KeepAlivePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.serverbound.ServerboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.serverbound.ServerboundPingRequestPacket

interface ServerCommonPacketListener : ServerboundPacketListener {
    fun handleBundlePacket(packet: ServerboundBundlePacket)

    suspend fun handleKeepAlivePacket(packet: KeepAlivePacket)

    fun handlePingRequest(packet: ServerboundPingRequestPacket)

}