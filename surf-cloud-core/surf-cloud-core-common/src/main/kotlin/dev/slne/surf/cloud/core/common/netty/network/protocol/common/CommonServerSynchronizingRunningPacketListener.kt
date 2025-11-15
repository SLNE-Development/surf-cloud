package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundCreateOfflineCloudPlayerIfNotExistsPacket

interface CommonServerSynchronizingRunningPacketListener :
    CommonSynchronizingRunningPacketListener {

    fun handleCreateOfflineCloudPlayerIfNotExists(packet: ServerboundCreateOfflineCloudPlayerIfNotExistsPacket)
}