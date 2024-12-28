package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerCommonPacketListener

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

    suspend fun handleClientInformation(packet: ServerboundClientInformationPacket)

    suspend fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket)

    suspend fun handleRequestPlayerPersistentDataContainer(packet: ServerboundRequestPlayerPersistentDataContainer)

    suspend fun handlePlayerPersistentDataContainerUpdate(packet: ServerboundPlayerPersistentDataContainerUpdatePacket)

    suspend fun handleConnectPlayerToServer(packet: ServerboundConnectPlayerToServerPacket)

    fun handleDisconnectPlayer(packet: DisconnectPlayerPacket)

    fun handlePacket(packet: NettyPacket)
}