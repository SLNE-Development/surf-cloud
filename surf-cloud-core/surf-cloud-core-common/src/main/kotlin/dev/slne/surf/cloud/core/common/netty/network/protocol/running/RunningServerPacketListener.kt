package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener

interface RunningServerPacketListener: ServerboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handleBundlePacket(packet: ServerboundBundlePacket)

    suspend fun handleKeepAlivePacket(packet: ServerboundKeepAlivePacket)

    fun handlePingRequest(packet: ServerboundPingRequestPacket)

    fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket)

    fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket)

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

    fun handlePacket(packet: NettyPacket)
}