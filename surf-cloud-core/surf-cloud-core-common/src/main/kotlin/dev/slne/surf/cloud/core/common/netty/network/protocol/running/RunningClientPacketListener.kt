package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonClientSynchronizingRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ClientboundCacheRegisterAckPacket

interface RunningClientPacketListener :
    ClientCommonPacketListener,
    CommonClientSynchronizingRunningPacketListener,
    CommonRunningPacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handlePlayerConnectedToServer(packet: PlayerConnectedToServerPacket)

    fun handleSendResourcePacks(packet: ClientboundSendResourcePacksPacket)

    fun handleClearResourcePacks(packet: ClientboundClearResourcePacksPacket)

    fun handleRemoveResourcePacks(packet: ClientboundRemoveResourcePacksPacket)

    fun handleShowTitle(packet: ClientboundShowTitlePacket)

    fun handleSendTitlePart(packet: ClientboundSendTitlePartPacket)

    fun handleClearTitle(packet: ClientboundClearTitlePacket)

    fun handleResetTitle(packet: ClientboundResetTitlePacket)

    fun handleShowBossBar(packet: ClientboundShowBossBarPacket)

    fun handleHideBossBar(packet: ClientboundHideBossBarPacket)

    fun handleOpenBook(packet: ClientboundOpenBookPacket)

    fun handlePlaySound(packet: ClientboundPlaySoundPacket)

    fun handleStopSound(packet: ClientboundStopSoundPacket)

    fun handleSendMessage(packet: ClientboundSendMessagePacket)

    fun handleSendActionBar(packet: ClientboundSendActionBarPacket)

    fun handleSendPlayerListHeaderAndFooter(packet: ClientboundSendPlayerListHeaderAndFooterPacket)

    fun handleRequestDisplayName(packet: ClientboundRequestDisplayNamePacket)

    fun handleRegisterServerPacket(packet: ClientboundRegisterServerPacket)

    fun handleUnregisterServerPacket(packet: ClientboundUnregisterServerPacket)

    fun handleAddPlayerToServer(packet: ClientboundAddPlayerToServerPacket)

    fun handleRemovePlayerFromServer(packet: ClientboundRemovePlayerFromServerPacket)

    fun handleUpdateServerInformation(packet: ClientboundUpdateServerInformationPacket)

    fun handleIsServerManagedByThisProxy(packet: ClientboundIsServerManagedByThisProxyPacket)

    fun handleTransferPlayer(packet: ClientboundTransferPlayerPacket)

    fun handleRegisterCloudServersToProxy(packet: ClientboundRegisterCloudServersToProxyPacket)

    fun handleTriggerShutdown(packet: ClientboundTriggerShutdownPacket)

    fun handleRunPlayerPreJoinTasks(packet: ClientboundRunPrePlayerJoinTasksPacket)

    fun handleTriggerPunishmentUpdateEvent(packet: ClientboundTriggerPunishmentUpdateEventPacket)

    fun handleTriggerPunishmentCreatedEvent(packet: ClientboundTriggerPunishmentCreatedEventPacket)

    fun handleCacheRegisterAck(packet: ClientboundCacheRegisterAckPacket)
    fun handleCacheDelta(packet: ClientboundCacheDeltaPacket)
    fun handleCacheError(packet: ClientboundCacheErrorPacket)

    fun handlePacket(packet: NettyPacket)
}