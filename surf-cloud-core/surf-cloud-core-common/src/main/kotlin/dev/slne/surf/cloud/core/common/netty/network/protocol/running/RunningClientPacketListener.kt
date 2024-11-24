package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.TickablePacketListener

interface RunningClientPacketListener: ClientboundPacketListener, TickablePacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING

    fun handleBundlePacket(packet: ClientboundBundlePacket)

    fun handleKeepAlive(packet: ClientboundKeepAlivePacket)

    fun handlePing(packet: ClientboundPingPacket)

    fun handleDisconnect(packet: ClientboundDisconnectPacket)

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

    fun handlePacket(packet: NettyPacket)
}