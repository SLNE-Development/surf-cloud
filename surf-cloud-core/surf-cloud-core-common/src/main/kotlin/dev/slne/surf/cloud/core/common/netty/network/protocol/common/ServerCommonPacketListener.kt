package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener

interface ServerCommonPacketListener : ServerboundPacketListener, CommonPacketListener {
    fun handleBundlePacket(packet: ServerboundBundlePacket)

    fun handlePingRequest(packet: ServerboundPingRequestPacket)

}