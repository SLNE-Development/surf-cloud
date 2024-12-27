package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener

interface ServerPreRunningPacketListener : ServerCommonPacketListener {
    override val protocol get() = ConnectionProtocol.PRE_RUNNING

    fun handleRequestContinuation(packet: ServerboundRequestContinuation)

    suspend fun handleReadyToRun(packet: ServerboundReadyToRunPacket)

    suspend fun handlePreRunningAcknowledged(packet: ServerboundPreRunningAcknowledgedPacket)
}