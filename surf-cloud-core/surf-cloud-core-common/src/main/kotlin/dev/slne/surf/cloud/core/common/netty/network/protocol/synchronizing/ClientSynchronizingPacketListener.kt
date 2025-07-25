package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundSetVelocitySecretPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundBatchUpdateServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket

interface ClientSynchronizingPacketListener : ClientCommonPacketListener {
    override val protocol get() = ConnectionProtocol.SYNCHRONIZING

    suspend fun handleSynchronizeFinish(packet: ClientboundSynchronizeFinishPacket)

    fun handleSyncValueChange(packet: SyncValueChangePacket)

    fun handleBatchSyncValue(packet: ClientboundBatchSyncValuePacket)

    fun handleBatchSyncSet(packet: ClientboundBatchSyncSetPacket)

    suspend fun handleBatchUpdateServer(packet: ClientboundBatchUpdateServer)

    fun handleSyncSetDelta(packet: SyncSetDeltaPacket)

    fun handleSetVelocitySecret(packet: ClientboundSetVelocitySecretPacket)

    fun handlePacket(packet: NettyPacket)
}