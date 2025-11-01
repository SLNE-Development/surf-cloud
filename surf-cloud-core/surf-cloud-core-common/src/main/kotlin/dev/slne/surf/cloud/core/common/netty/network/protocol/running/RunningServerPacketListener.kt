package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ServerboundCacheRegisterKeysPacket

interface RunningServerPacketListener : ServerCommonPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    suspend fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket)

    suspend fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket)

    fun handleSendResourcePacks(packet: ServerboundSendResourcePacksPacket)

    fun handleClearResourcePacks(packet: ServerboundClearResourcePacksPacket)

    fun handleRemoveResourcePacks(packet: ServerboundRemoveResourcePacksPacket)

    fun handleShowTitle(packet: ServerboundShowTitlePacket)

    fun handleSendTitlePart(packet: ServerboundSendTitlePartPacket)

    fun handleClearTitle(packet: ServerboundClearTitlePacket)

    fun handleResetTitle(packet: ServerboundResetTitlePacket)

    fun handleShowBossBar(packet: ServerboundShowBossBarPacket)

    fun handleHideBossBar(packet: ServerboundHideBossBarPacket)

    fun handleOpenBook(packet: ServerboundOpenBookPacket)

    fun handlePlaySound(packet: ServerboundPlaySoundPacket)

    fun handleStopSound(packet: ServerboundStopSoundPacket)

    fun handleSendMessage(packet: ServerboundSendMessagePacket)

    fun handleSendActionBar(packet: ServerboundSendActionBarPacket)

    fun handleSendPlayerListHeaderAndFooter(packet: ServerboundSendPlayerListHeaderAndFooterPacket)

    suspend fun handleRequestDisplayName(packet: ServerboundRequestDisplayNamePacket)

    suspend fun handleRequestOfflinePlayerDisplayName(packet: RequestOfflineDisplayNamePacket)

    suspend fun handleClientInformation(packet: ServerboundClientInformationPacket)

    suspend fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket)

    suspend fun handleRequestPlayerPersistentDataContainer(packet: ServerboundRequestPlayerPersistentDataContainer)

    suspend fun handleConnectPlayerToServer(packet: ServerboundConnectPlayerToServerPacket)

    suspend fun handleQueuePlayerToGroup(packet: ServerboundQueuePlayerToGroupPacket)

    fun handleDisconnectPlayer(packet: DisconnectPlayerPacket)

    fun handleSilentDisconnectPlayer(packet: SilentDisconnectPlayerPacket)

    suspend fun handleTeleportPlayer(packet: TeleportPlayerPacket)

    suspend fun handleTeleportPlayerToPlayer(packet: TeleportPlayerToPlayerPacket)

    suspend fun handleShutdownServer(packet: ServerboundShutdownServerPacket)

    suspend fun handleRequestPlayerData(packet: ServerboundRequestPlayerDataPacket)

    fun handleUpdateAFKState(packet: UpdateAFKStatePacket)

    fun handleGeneratePunishmentId(packet: ServerboundGeneratePunishmentIdPacket)

    fun handleCreateKick(packet: ServerboundCreateKickPacket)

    fun handleCreateWarn(packet: ServerboundCreateWarnPacket)

    fun handleCreateMute(packet: ServerboundCreateMutePacket)

    fun handleCreateBan(packet: ServerboundCreateBanPacket)

    fun handleAttachIpAddressToBan(packet: ServerboundAttachIpAddressToBanPacket)

    fun handleAttachNoteToPunishment(packet: ServerboundAttachNoteToPunishmentPacket)

    fun handleFetchNotesFromPunishment(packet: ServerboundFetchNotesFromPunishmentPacket)

    fun handleFetchMutes(packet: ServerboundFetchMutesPacket)

    fun handleFetchBans(packet: ServerboundFetchBansPacket)

    fun handleFetchKicks(packet: ServerboundFetchKicksPacket)

    fun handleFetchWarns(packet: ServerboundFetchWarnsPacket)

    fun handleGetCurrentLoginValidationPunishmentCache(packet: ServerboundGetCurrentLoginValidationPunishmentCachePacket)

    fun handleFetchIpAddressesForBan(packet: ServerboundFetchIpAddressesForBanPacket)

    fun handleFetchIpBans(packet: ServerboundFetchIpBansPacket)

    suspend fun handleRequestPlayerPermission(packet: RequestPlayerPermissionPacket)

    fun handleSyncValueChange(packet: SyncValueChangePacket)

    fun handleSyncSetDelta(packet: SyncSetDeltaPacket)

    fun handleCreateOfflineCloudPlayerIfNotExists(packet: ServerboundCreateOfflineCloudPlayerIfNotExistsPacket)

    suspend fun handleRequestWhitelistStatus(packet: ServerboundRequestWhitelistStatusPacket)

    suspend fun handleRequestWhitelist(packet: ServerboundRequestWhitelistPacket)

    suspend fun handleCreateWhitelist(packet: ServerboundCreateWhitelistPacket)

    suspend fun handleUpdateWhitelist(packet: ServerboundUpdateWhitelistPacket)

    fun handleRefreshWhitelist(packet: ServerboundRefreshWhitelistPacket)

    fun handleCacheRegisterKeys(packet: ServerboundCacheRegisterKeysPacket)
    fun handleCacheOp(packet: ServerboundCacheOpPacket)
    fun handleCacheFetch(packet: ServerboundCacheFetchPacket)
    fun handleCacheWatchPlayers(packet: ServerboundCacheWatchPlayersPacket)

    fun handleSendToast(packet: SendToastPacket)

    fun handleUpdatePlayerPersistentDataContainer(packet: UpdatePlayerPersistentDataContainerPacket)

    fun handlePacket(packet: NettyPacket)
}