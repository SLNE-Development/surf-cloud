package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.serverManager
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.mutableIntSetOf
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.util.random
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


class ServerRunningPacketListenerImpl(
    server: NettyServerImpl,
    client: ServerClientImpl,
    connection: ConnectionImpl
) : ServerCommonPacketListenerImpl(server, client, connection), RunningServerPacketListener {
    private val log = logger()

    override suspend fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket) {
        playerManagerImpl.updateOrCreatePlayer(packet.uuid, packet.serverUid, packet.proxy)
        broadcast(packet)
        serverManagerImpl.getCommonStandaloneServerByUid(packet.serverUid)
            ?.handlePlayerConnect(packet.uuid)
    }

    override suspend fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket) {
        playerManagerImpl.updateOrRemoveOnDisconnect(packet.uuid, packet.serverUid, packet.proxy)
        serverManagerImpl.getCommonStandaloneServerByUid(packet.serverUid)
            ?.handlePlayerDisconnect(packet.uuid)
        broadcast(packet)
    }

    override fun handleSendResourcePacks(packet: ServerboundSendResourcePacksPacket) {
        withPlayer(packet.uuid) { sendResourcePacks(packet.request) }
    }

    override fun handleClearResourcePacks(packet: ServerboundClearResourcePacksPacket) {
        withPlayer(packet.uuid) { clearResourcePacks() }
    }

    override fun handleRemoveResourcePacks(packet: ServerboundRemoveResourcePacksPacket) {
        withPlayer(packet.uuid) { removeResourcePacks(packet.first, *packet.others) }
    }

    override fun handleShowTitle(packet: ServerboundShowTitlePacket) {
        withPlayer(packet.uuid) { showTitle(packet.title) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleSendTitlePart(packet: ServerboundSendTitlePartPacket) {
        withPlayer(packet.uuid) {
            val part = packet.titlePart
            val value = packet.value
            when (part) {
                TitlePart.TITLE, TitlePart.SUBTITLE -> sendTitlePart(
                    part as TitlePart<Component>,
                    value as Component
                )

                TitlePart.TIMES -> sendTitlePart(
                    part as TitlePart<Title.Times>,
                    value as Title.Times
                )

                else -> error("Unknown title part: $part")
            }
        }
    }

    override fun handleClearTitle(packet: ServerboundClearTitlePacket) {
        withPlayer(packet.uuid) { clearTitle() }
    }

    override fun handleResetTitle(packet: ServerboundResetTitlePacket) {
        withPlayer(packet.uuid) { resetTitle() }
    }

    override fun handleShowBossBar(packet: ServerboundShowBossBarPacket) {
        withPlayer(packet.uuid) { showBossBar(packet.bossBar) }
    }

    override fun handleHideBossBar(packet: ServerboundHideBossBarPacket) {
        withPlayer(packet.uuid) { hideBossBar(packet.bossBar) }
    }

    override fun handleOpenBook(packet: ServerboundOpenBookPacket) {
        withPlayer(packet.uuid) { openBook(packet.book) }
    }

    override fun handlePlaySound(packet: ServerboundPlaySoundPacket) {
        withPlayer(packet.uuid) {
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

    override fun handleStopSound(packet: ServerboundStopSoundPacket) {
        withPlayer(packet.uuid) { stopSound(packet.soundStop) }
    }

    override fun handleSendMessage(packet: ServerboundSendMessagePacket) {
        withPlayer(packet.uuid) { sendMessage(packet.message) }
    }

    override fun handleSendActionBar(packet: ServerboundSendActionBarPacket) {
        withPlayer(packet.uuid) { sendActionBar(packet.message) }
    }

    override fun handleSendPlayerListHeaderAndFooter(packet: ServerboundSendPlayerListHeaderAndFooterPacket) {
        withPlayer(packet.uuid) { sendPlayerListHeaderAndFooter(packet.header, packet.footer) }
    }

    override suspend fun handleRequestDisplayName(packet: ServerboundRequestDisplayNamePacket) {
        log.atInfo()
            .log("Requesting display name for %s", packet.uuid)

        withPlayer(packet.uuid) {
            log.atInfo()
                .log("Trying to respond with display name for %s", packet.uuid)

            val displayName = displayName()

            log.atInfo()
                .log("Responding with display name for %s: %s", packet.uuid, displayName)

            packet.respond(
                ResponseDisplayNamePacketRequestPacket(
                    uuid,
                    displayName
                )
            )
        }
    }

    override suspend fun handleClientInformation(packet: ServerboundClientInformationPacket) {
        val server = serverManagerImpl.retrieveServerById(packet.serverId) ?: return

        if (server is StandaloneProxyCloudServerImpl) {
            server.information = packet.information
        } else if (server is StandaloneCloudServerImpl) {
            server.information = packet.information
            server.queue.handleServerUpdate()
        }

        broadcast(ClientboundUpdateServerInformationPacket(packet.serverId, packet.information))
    }

    override suspend fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket) {
        val player = playerManagerImpl.getPlayer(packet.uuid)
            ?: error("Received luckperms meta data request for unknown player ${packet.uuid}! Is the player online?")
        val data = player.getLuckpermsMetaData(packet.key)
        packet.respond(LuckpermsMetaDataResponsePacket(data))
    }

    private val pendingVerificationIds = mutableIntSetOf()
    override suspend fun handleRequestPlayerPersistentDataContainer(packet: ServerboundRequestPlayerPersistentDataContainer) {
        withPlayer(packet.uuid) {
            val data = getPersistentData()
            val id = random.nextInt()
            pendingVerificationIds.add(id)

            packet.respond(ClientboundPlayerPersistentDataContainerResponse(id, data))
        }
    }

    override suspend fun handlePlayerPersistentDataContainerUpdate(packet: ServerboundPlayerPersistentDataContainerUpdatePacket) {
        if (!pendingVerificationIds.remove(packet.verificationId)) {
            log.atWarning()
                .log(
                    "Received invalid persistent data container update id %s",
                    packet.verificationId
                )
            return
        }

        withPlayer(packet.uuid) {
            log.atInfo()
                .log("Updating persistent data for %s with data %s", packet.uuid, packet.nbt)
            updatePersistentData(packet.nbt)
        }
    }

    override suspend fun handleConnectPlayerToServer(packet: ServerboundConnectPlayerToServerPacket) {
        withPlayer(packet.uuid) {
            val server = serverManager.retrieveServerById(packet.serverId)

            if (server == null) {
                packet.respond(ClientboundConnectPlayerToServerResponse(ConnectionResultEnum.SERVER_NOT_FOUND to null))
                return
            }

            if (server !is CloudServer) {
                packet.respond(ClientboundConnectPlayerToServerResponse(ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY to null))
                return
            }

            val result = if (packet.queue) {
                connectToServerOrQueue(server)
            } else {
                connectToServer(server)
            }

            packet.respond(ClientboundConnectPlayerToServerResponse(result))
        }
    }

    override fun handleDisconnectPlayer(packet: DisconnectPlayerPacket) {
        withPlayer(packet.uuid) { disconnect(packet.reason) }
    }

    override suspend fun handleTeleportPlayer(packet: TeleportPlayerPacket) {
        withPlayer(packet.uuid) {
            val result = teleport(
                location = packet.location,
                teleportCause = packet.teleportCause,
                flags = packet.flags
            )

            packet.respond(TeleportPlayerResultPacket(result))
        }
    }

    override suspend fun handleShutdownServer(packet: ServerboundShutdownServerPacket) {
        val server = serverManager.retrieveServerById(packet.serverId) ?: return
        server.shutdown()
    }

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val info = NettyPacketInfo(connection)

        for (listener in listeners) {
            PacketHandlerScope.launch {
                try {
                    listener.handle(packet, info)
                } catch (e: Throwable) {
                    log.atWarning()
                        .withCause(e)
                        .log(
                            "Failed to call listener %s for packet %s",
                            listener::class.simpleName,
                            packet::class.simpleName
                        )
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun withPlayer(uuid: UUID, block: StandaloneCloudPlayerImpl.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        val player = playerManagerImpl.getPlayer(uuid) as? StandaloneCloudPlayerImpl ?: return
        player.block()
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }
}

