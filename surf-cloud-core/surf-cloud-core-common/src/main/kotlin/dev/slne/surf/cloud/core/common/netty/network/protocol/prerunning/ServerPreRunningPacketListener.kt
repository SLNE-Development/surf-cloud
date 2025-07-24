package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.serverbound.ServerboundPreRunningAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.serverbound.ServerboundProceedToSynchronizingAcknowledgedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.serverbound.ServerboundRequestContinuation

interface ServerPreRunningPacketListener : ServerCommonPacketListener {
    override val protocol get() = ConnectionProtocol.PRE_RUNNING

    fun handleRequestContinuation(packet: ServerboundRequestContinuation)

    suspend fun handleReadyToRun(packet: ServerboundProceedToSynchronizingAcknowledgedPacket)

    suspend fun handlePreRunningAcknowledged(packet: ServerboundPreRunningAcknowledgedPacket)
}