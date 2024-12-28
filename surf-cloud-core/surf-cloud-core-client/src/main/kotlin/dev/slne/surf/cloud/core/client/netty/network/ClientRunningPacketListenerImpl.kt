package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.client.player.commonPlayerManagerImpl
import dev.slne.surf.cloud.core.client.server.serverManagerImpl
import dev.slne.surf.cloud.core.client.util.getOrLoadUser
import dev.slne.surf.cloud.core.client.util.luckperms
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.protocol.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.server.CloudServerImpl
import dev.slne.surf.cloud.core.common.server.ProxyCloudServerImpl
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.*
import java.util.concurrent.TimeUnit

class ClientRunningPacketListenerImpl(
    connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension
) : ClientCommonPacketListenerImpl(connection), RunningClientPacketListener {
    private val log = logger()

    override suspend fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket) {
        playerManagerImpl.updateOrCreatePlayer(packet.uuid, packet.serverUid, packet.proxy)
    }

    override suspend fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket) {
        playerManagerImpl.updateOrRemoveOnDisconnect(packet.uuid, packet.serverUid, packet.proxy)
    }

    override fun handleSendResourcePacks(packet: ClientboundSendResourcePacksPacket) {
        withAudience(packet.uuid) { sendResourcePacks(packet.request) }
    }

    override fun handleClearResourcePacks(packet: ClientboundClearResourcePacksPacket) {
        withAudience(packet.uuid) { clearResourcePacks() }
    }

    override fun handleRemoveResourcePacks(packet: ClientboundRemoveResourcePacksPacket) {
        withAudience(packet.uuid) { removeResourcePacks(packet.first, *packet.others) }
    }

    override fun handleShowTitle(packet: ClientboundShowTitlePacket) {
        withAudience(packet.uuid) { showTitle(packet.title) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSendTitlePart(packet: ClientboundSendTitlePartPacket) {
        withAudience(packet.uuid) {
            when (packet.titlePart) {
                TitlePart.TITLE, TitlePart.SUBTITLE -> sendTitlePart(
                    packet.titlePart as TitlePart<Component>,
                    packet.value as Component
                )

                TitlePart.TIMES -> sendTitlePart(
                    packet.titlePart as TitlePart<Title.Times>,
                    packet.value as Title.Times
                )

                else -> error("Unknown title part: ${packet.titlePart}")
            }
        }
    }

    override fun handleClearTitle(packet: ClientboundClearTitlePacket) {
        withAudience(packet.uuid) { clearTitle() }
    }

    override fun handleResetTitle(packet: ClientboundResetTitlePacket) {
        withAudience(packet.uuid) { resetTitle() }
    }

    override fun handleShowBossBar(packet: ClientboundShowBossBarPacket) {
        withAudience(packet.uuid) { showBossBar(packet.bossBar) }
    }

    override fun handleHideBossBar(packet: ClientboundHideBossBarPacket) {
        withAudience(packet.uuid) { hideBossBar(packet.bossBar) }
    }

    override fun handleOpenBook(packet: ClientboundOpenBookPacket) {
        withAudience(packet.uuid) { openBook(packet.book) }
    }

    override fun handlePlaySound(packet: ClientboundPlaySoundPacket) {
        withAudience(packet.uuid) {
            val emitter = packet.emitter
            val x = packet.x
            val y = packet.y
            val z = packet.z

            if (emitter != null) {
                playSound(packet.sound, emitter)
            } else if (x != null && y != null && z != null) {
                playSound(packet.sound, x, y, z)
            } else {
                playSound(packet.sound)
            }
        }
    }

    override fun handleStopSound(packet: ClientboundStopSoundPacket) {
        withAudience(packet.uuid) { stopSound(packet.soundStop) }
    }

    override fun handleSendMessage(packet: ClientboundSendMessagePacket) {
        withAudience(packet.uuid) { sendMessage(packet.message) }
    }

    override fun handleSendActionBar(packet: ClientboundSendActionBarPacket) {
        withAudience(packet.uuid) { sendActionBar(packet.message) }
    }

    override fun handleSendPlayerListHeaderAndFooter(packet: ClientboundSendPlayerListHeaderAndFooterPacket) {
        withAudience(packet.uuid) { sendPlayerListHeaderAndFooter(packet.header, packet.footer) }
    }

    override fun handleRequestDisplayName(packet: ClientboundRequestDisplayNamePacket) {
        withRequiredAudience(packet.uuid, {
            val displayName = pointers().get(Identity.DISPLAY_NAME).orElse(Component.empty())
            packet.respond(ResponseDisplayNamePacketRequestPacket(packet.uuid, displayName))
        }) { "Display name requested for player ${packet.uuid} who is not online. Probably send to wrong server." }
    }

    override suspend fun handleRegisterServerPacket(packet: ClientboundRegisterServerPacket) {
        val server = if (packet.proxy) {
            ProxyCloudServerImpl(
                packet.serverId,
                packet.group,
                packet.name,
            )
        } else {
            CloudServerImpl(
                packet.serverId,
                packet.group,
                packet.name,
            )
        }

        serverManagerImpl.registerServer(server)
    }

    override suspend fun handleUnregisterServerPacket(packet: ClientboundUnregisterServerPacket) {
        serverManagerImpl.unregisterServer(packet.serverId)
    }

    override suspend fun handleAddPlayerToServer(packet: ClientboundAddPlayerToServerPacket) {
        (serverManagerImpl.retrieveServerById(packet.serverUid)?.users as? UserListImpl)?.add(packet.playerUuid)
    }

    override suspend fun handleRemovePlayerFromServer(packet: ClientboundRemovePlayerFromServerPacket) {
        (serverManagerImpl.retrieveServerById(packet.serverUid)?.users as? UserListImpl)
            ?.remove(packet.playerUuid)
    }

    override fun handleUpdateServerInformation(packet: ClientboundUpdateServerInformationPacket) {
        serverManagerImpl.updateServerInformationNow(packet.serverId, packet.information)
    }

    override fun handleIsServerManagedByThisProxy(packet: ClientboundIsServerManagedByThisProxyPacket) {
        val managed = platformExtension.isServerManagedByThisProxy(packet.clientAddress)
        packet.respond(ServerboundIsServerManagedByThisProxyResponse(managed))
    }

    override suspend fun handleTransferPlayer(packet: ClientboundTransferPlayerPacket) {
        val (status, reason) = platformExtension.transferPlayerToServer(
            packet.playerUuid,
            packet.address
        )
        packet.respond(ServerboundTransferPlayerPacketResponse(status, reason))
    }

    override suspend fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket) {
        val metaValue =
            luckperms.userManager.getOrLoadUser(packet.uuid).cachedData.metaData.getMetaValue(packet.key)
        packet.respond(LuckpermsMetaDataResponsePacket(metaValue))
    }

    override fun handleDisconnectPlayer(packet: DisconnectPlayerPacket) {
        platformExtension.disconnectPlayer(packet.uuid, packet.reason)
    }

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val (proxiedSource, finalPacket) = when (packet) {
//            is ProxiedNettyPacket -> packet.source to packet.packet
            else -> null to packet
        }
        val info = NettyPacketInfo(this, proxiedSource)

        for (listener in listeners) {
            PacketHandlerScope.launch {
                try {
                    listener.handle(finalPacket, info)
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .atMostEvery(5, TimeUnit.SECONDS)
                        .log(
                            "Failed to call listener %s for packet %s",
                            listener::class.simpleName,
                            finalPacket::class.simpleName
                        )
                }
            }
        }
    }

    private fun withAudience(uuid: UUID, block: Audience.() -> Unit) {
        commonPlayerManagerImpl.getAudience(uuid)?.block()
    }

    private fun withRequiredAudience(
        uuid: UUID,
        block: Audience.() -> Unit,
        errorMessage: () -> String
    ) {
        val audience = commonPlayerManagerImpl.getAudience(uuid)
        if (audience == null) {
            log.atWarning().log(errorMessage())
            return
        }

        audience.block()
    }
}