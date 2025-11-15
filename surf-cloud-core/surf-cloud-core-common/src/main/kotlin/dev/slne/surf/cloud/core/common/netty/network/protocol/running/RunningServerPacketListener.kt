package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonServerSynchronizingRunningPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ServerboundCacheRegisterKeysPacket

interface RunningServerPacketListener :
    ServerCommonPacketListener,
    TickablePacketListener,
    CommonServerSynchronizingRunningPacketListener,
    CommonRunningPacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket)

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

    fun handleRequestDisplayName(packet: ServerboundRequestDisplayNamePacket)

    fun handleClientInformation(packet: ServerboundClientInformationPacket)

    fun handleConnectPlayerToServer(packet: ServerboundConnectPlayerToServerPacket)

    fun handleQueuePlayerToGroup(packet: ServerboundQueuePlayerToGroupPacket)

    fun handleShutdownServer(packet: ServerboundShutdownServerPacket)

    fun handleRequestPlayerData(packet: ServerboundRequestPlayerDataPacket)

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

    fun handleRequestWhitelistStatus(packet: ServerboundRequestWhitelistStatusPacket)

    fun handleRequestWhitelist(packet: ServerboundRequestWhitelistPacket)

    fun handleCreateWhitelist(packet: ServerboundCreateWhitelistPacket)

    fun handleUpdateWhitelist(packet: ServerboundUpdateWhitelistPacket)

    fun handleRefreshWhitelist(packet: ServerboundRefreshWhitelistPacket)

    fun handleCacheRegisterKeys(packet: ServerboundCacheRegisterKeysPacket)
    fun handleCacheOp(packet: ServerboundCacheOpPacket)
    fun handleCacheFetch(packet: ServerboundCacheFetchPacket)
    fun handleCacheWatchPlayers(packet: ServerboundCacheWatchPlayersPacket)

    fun handlePacket(packet: NettyPacket)
}