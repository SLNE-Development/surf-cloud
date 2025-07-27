package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.bidirectional.KeepAlivePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.clientbound.ClientboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound.ClientboundPingPacket

interface ClientCommonPacketListener : ClientboundPacketListener {

    fun handleBundlePacket(packet: ClientboundBundlePacket)

    fun handleKeepAlive(packet: KeepAlivePacket)

    fun handlePing(packet: ClientboundPingPacket)

    fun handleDisconnect(packet: ClientboundDisconnectPacket)
}