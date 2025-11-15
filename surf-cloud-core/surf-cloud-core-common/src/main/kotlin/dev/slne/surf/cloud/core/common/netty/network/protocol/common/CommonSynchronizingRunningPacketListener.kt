package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.PacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncSetDeltaPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SyncValueChangePacket

interface CommonSynchronizingRunningPacketListener : PacketListener {

    fun handleSyncValueChange(packet: SyncValueChangePacket)

    fun handleSyncSetDelta(packet: SyncSetDeltaPacket)
}