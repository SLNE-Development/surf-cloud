package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPingPacket

interface ClientCommonPacketListener : ClientboundPacketListener, CommonPacketListener {

    fun handleBundlePacket(packet: ClientboundBundlePacket)

    fun handlePing(packet: ClientboundPingPacket)

    fun handlePong(packet: ClientboundPongResponsePacket)

    fun handleDisconnect(packet: ClientboundDisconnectPacket)
}