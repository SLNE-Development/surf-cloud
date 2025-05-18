package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener

interface RunningClientPacketListener : ClientCommonPacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    suspend fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket)

    suspend fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket)

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

    suspend fun handleRequestOfflinePlayerDisplayName(packet: RequestOfflineDisplayNamePacket)

    suspend fun handleRegisterServerPacket(packet: ClientboundRegisterServerPacket)

    suspend fun handleUnregisterServerPacket(packet: ClientboundUnregisterServerPacket)

    suspend fun handleAddPlayerToServer(packet: ClientboundAddPlayerToServerPacket)

    suspend fun handleRemovePlayerFromServer(packet: ClientboundRemovePlayerFromServerPacket)

    fun handleUpdateServerInformation(packet: ClientboundUpdateServerInformationPacket)

    fun handleIsServerManagedByThisProxy(packet: ClientboundIsServerManagedByThisProxyPacket)

    suspend fun handleTransferPlayer(packet: ClientboundTransferPlayerPacket)

    suspend fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket)

    fun handleDisconnectPlayer(packet: DisconnectPlayerPacket)

    fun handleSilentDisconnectPlayer(packet: SilentDisconnectPlayerPacket)

    suspend fun handleTeleportPlayer(packet: TeleportPlayerPacket)

    suspend fun handleTeleportPlayerToPlayer(packet: TeleportPlayerToPlayerPacket)

    fun handleRegisterCloudServersToProxy(packet: ClientboundRegisterCloudServersToProxyPacket)

    fun handleTriggerShutdown(packet: ClientboundTriggerShutdownPacket)

    suspend fun handleBatchUpdateServer(packet: ClientboundBatchUpdateServer)

    fun handleUpdateAFKState(packet: UpdateAFKStatePacket)

    suspend fun handleRunPlayerPreJoinTasks(packet: ClientboundRunPrePlayerJoinTasksPacket)

    suspend fun handleTriggerPunishmentUpdateEvent(packet: ClientboundTriggerPunishmentUpdateEventPacket)

    suspend fun handleTriggerPunishmentCreatedEvent(packet: ClientboundTriggerPunishmentCreatedEventPacket)

    suspend fun handleRequestPlayerPermission(packet: RequestPlayerPermissionPacket)

    fun handlePacket(packet: NettyPacket)
}