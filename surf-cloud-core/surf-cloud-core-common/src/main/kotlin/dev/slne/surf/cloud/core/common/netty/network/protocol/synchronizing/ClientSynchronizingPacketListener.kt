package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonClientSynchronizingRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundBatchUpdateServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPlayerCacheHydrateChunkPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPlayerCacheHydrateEndPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPlayerCacheHydrateStartPacket

interface ClientSynchronizingPacketListener : ClientCommonPacketListener,
    CommonClientSynchronizingRunningPacketListener {
    override val protocol get() = ConnectionProtocol.SYNCHRONIZING

    fun handleSynchronizeFinish(packet: ClientboundSynchronizeFinishPacket)

    fun handleBatchSyncValue(packet: ClientboundBatchSyncValuePacket)

    fun handleBatchSyncSet(packet: ClientboundBatchSyncSetPacket)

    fun handleBatchUpdateServer(packet: ClientboundBatchUpdateServer)

    fun handlePlayerCacheHydrateStart(packet: ClientboundPlayerCacheHydrateStartPacket)
    fun handlePlayerCacheHydrateChunk(packet: ClientboundPlayerCacheHydrateChunkPacket)
    fun handlePlayerCacheHydrateEnd(packet: ClientboundPlayerCacheHydrateEndPacket)

    fun handleSyncPlayerHydrationStart(packet: ClientboundSyncPlayerHydrationStartPacket)
    fun handleSyncPlayerHydrationChunk(packet: ClientboundSyncPlayerHydrationChunkPacket)
    fun handleSyncPlayerHydrationEnd(packet: ClientboundSyncPlayerHydrationEndPacket)

    fun handleSyncLargerPlayerPersistentDataContainerStart(packet: ClientboundSyncLargePlayerPersistentDataContainerStartPacket)
    fun handleSyncLargerPlayerPersistentDataContainerChunk(packet: ClientboundSyncLargePlayerPersistentDataContainerChunkPacket)
    fun handleSyncLargerPlayerPersistentDataContainerEnd(packet: ClientboundSyncLargePlayerPersistentDataContainerEndPacket)

    fun handleSynchronizePlayerMutes(packet: ClientboundSynchronizePlayerMutes)

    fun handlePacket(packet: NettyPacket)
}