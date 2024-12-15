package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPingPacket

interface ClientCommonPacketListener: ClientboundPacketListener {

    fun handleBundlePacket(packet: ClientboundBundlePacket)

    fun handleKeepAlive(packet: ClientboundKeepAlivePacket)

    fun handlePing(packet: ClientboundPingPacket)

    fun handleDisconnect(packet: ClientboundDisconnectPacket)
}