package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonServerSynchronizingRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener

interface ServerSynchronizingPacketListener : ServerCommonPacketListener,
    CommonServerSynchronizingRunningPacketListener {
    override val protocol get() = ConnectionProtocol.SYNCHRONIZING

    fun handleFinishSynchronizing(packet: FinishSynchronizingPacket)

    fun handleSynchronizeFinishAcknowledged(packet: ServerboundSynchronizeFinishAcknowledgedPacket)

    fun handlePacket(packet: NettyPacket)
}