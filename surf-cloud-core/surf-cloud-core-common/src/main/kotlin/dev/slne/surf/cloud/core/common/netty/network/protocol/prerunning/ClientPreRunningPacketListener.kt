package dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.clientbound.ClientboundPreRunningFinishedPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.clientbound.ClientboundProceedToSynchronizingPacket

interface ClientPreRunningPacketListener : ClientCommonPacketListener {
    override val protocol get() = ConnectionProtocol.PRE_RUNNING

    suspend fun handlePreRunningFinished(packet: ClientboundPreRunningFinishedPacket)

    suspend fun handleProceedToSynchronizing(packet: ClientboundProceedToSynchronizingPacket)
}