package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional.SyncValueChangePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound.ServerboundCreateOfflineCloudPlayerIfNotExistsPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.bidirectional.FinishSynchronizingPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.serverbound.ServerboundSynchronizeFinishAcknowledgedPacket

interface ServerSynchronizingPacketListener : ServerCommonPacketListener {
    override val protocol get() = ConnectionProtocol.SYNCHRONIZING

    suspend fun handleFinishSynchronizing(packet: FinishSynchronizingPacket)

    suspend fun handleSynchronizeFinishAcknowledged(packet: ServerboundSynchronizeFinishAcknowledgedPacket)

    fun handleSyncValueChange(packet: SyncValueChangePacket)

    fun handleSyncSetDelta(packet: SyncSetDeltaPacket)

    fun handleCreateOfflineCloudPlayerIfNotExists(packet: ServerboundCreateOfflineCloudPlayerIfNotExistsPacket)

    fun handlePacket(packet: NettyPacket)
}