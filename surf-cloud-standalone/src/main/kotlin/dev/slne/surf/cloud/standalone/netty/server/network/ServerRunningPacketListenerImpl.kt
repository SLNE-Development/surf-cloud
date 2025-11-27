package dev.slne.surf.cloud.standalone.netty.server.network

import com.google.common.flogger.LogPerBucketingStrategy
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistSettings
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerIoScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.coroutines.QueueConnectionScope
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ServerboundCacheRegisterKeysPacket
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistService
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.player.standalonePlayerManagerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import dev.slne.surf.cloud.standalone.sync.SyncRegistryImpl
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.toMutableObjectList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException


class ServerRunningPacketListenerImpl(
    server: NettyServerImpl,
    client: ServerClientImpl,
    connection: ConnectionImpl
) : ServerCommonPacketListenerImpl(server, client, connection), RunningServerPacketListener {
    private val log = logger()

    override fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket) {
        PacketHandlerScope.launch {
            val result = standalonePlayerManagerImpl.updateOrCreatePlayer(
                packet.uuid,
                packet.name,
                packet.proxy,
                packet.playerIp,
                packet.serverName,
                true
            )

            packet.respond(PlayerConnectToServerResponsePacket(result.preJoinResult))

            if (result.preJoinAllowed) {
                broadcast(
                    PlayerConnectedToServerPacket(
                        packet.uuid,
                        packet.name,
                        packet.serverName,
                        packet.proxy,
                        packet.playerIp,
                        result.player!!.ppdcTagSnapshot()
                    )
                )

                serverManagerImpl.getCommonStandaloneServerByName(packet.serverName)
                    ?.handlePlayerConnect(packet.uuid)
            }
        }
    }

    override fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket) {
        PacketHandlerScope.launch {
            playerManagerImpl.updateOrRemoveOnDisconnect(
                packet.uuid,
                packet.serverName,
                packet.proxy
            )
            serverManagerImpl.getCommonStandaloneServerByName(packet.serverName)
                ?.handlePlayerDisconnect(packet.uuid)
            broadcast(packet)
        }
    }

    override fun handleSendResourcePacks(packet: ServerboundSendResourcePacksPacket) {
        withPlayer(packet.uuid) { sendResourcePacks(packet.request) }
    }

    override fun handleClearResourcePacks(packet: ServerboundClearResourcePacksPacket) {
        withPlayer(packet.uuid) { clearResourcePacks() }
    }

    override fun handleRemoveResourcePacks(packet: ServerboundRemoveResourcePacksPacket) {
        withPlayer(packet.uuid) { removeResourcePacks(packet.packIds) }
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
            val permission = packet.permission

            if (emitter != null) {
                if (permission == null) {
                    playSound(packet.sound, emitter)
                } else {
                    playSound(packet.sound, emitter, permission)
                }
            } else if (x != null && y != null && z != null) {
                require(permission == null) { "Permission not implemented for this sound play action" }
                playSound(packet.sound, x, y, z)
            } else {
                require(permission == null) { "Permission not implemented for this sound play action" }
                playSound(packet.sound)
            }
        }
    }

    override fun handleStopSound(packet: ServerboundStopSoundPacket) {
        withPlayer(packet.uuid) { stopSound(packet.soundStop) }
    }

    override fun handleSendMessage(packet: ServerboundSendMessagePacket) {
        withPlayer(packet.uuid) {
            val message = packet.message
            val permission = packet.permission

            if (permission == null) {
                sendMessage(message)
            } else {
                sendMessage(message, permission)
            }
        }
    }

    override fun handleSendActionBar(packet: ServerboundSendActionBarPacket) {
        withPlayer(packet.uuid) { sendActionBar(packet.message) }
    }

    override fun handleSendPlayerListHeaderAndFooter(packet: ServerboundSendPlayerListHeaderAndFooterPacket) {
        withPlayer(packet.uuid) { sendPlayerListHeaderAndFooter(packet.header, packet.footer) }
    }

    override fun handleRequestDisplayName(packet: ServerboundRequestDisplayNamePacket) {
        PacketHandlerScope.launch {
            withPlayer(packet.uuid) {
                val displayName = try {
                    displayName()
                } catch (e: Throwable) {
                    log.atWarning()
                        .per(packet.uuid, LogPerBucketingStrategy.byHashCode(250))
                        .atMostEvery(15, TimeUnit.SECONDS)
                        .withCause(e)
                        .log("Failed to retrieve display name for player ${packet.uuid}! Using fallback name.")

                    text(name)
                }

                packet.respond(
                    ResponseDisplayNamePacketRequestPacket(
                        uuid,
                        displayName
                    )
                )
            }
        }

    }

    override fun handleRequestOfflinePlayerDisplayName(packet: RequestOfflineDisplayNamePacket) {
        PacketHandlerScope.launch {
            val name = serverManagerImpl.requestOfflineDisplayName(packet.uuid)
            packet.respond(name)
        }
    }

    override fun handleClientInformation(packet: ServerboundClientInformationPacket) {
        val server = serverManagerImpl.retrieveServerByName(packet.serverName) ?: return

        if (server is StandaloneProxyCloudServerImpl) {
            server.information = packet.information
        } else if (server is StandaloneCloudServerImpl) {
            server.information = packet.information
        }

        broadcast(ClientboundUpdateServerInformationPacket(packet.serverName, packet.information))
    }

    override fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket) {
        withPlayer(packet.uuid, {
            log.atWarning()
                .per(packet.uuid, LogPerBucketingStrategy.byHashCode(25))
                .atMostEvery(15, TimeUnit.SECONDS)
                .log("Failed to retrieve luckperms meta data for player ${packet.uuid}! Player not found.")
        }) {
            PacketHandlerScope.launch {
                packet.respond(LuckpermsMetaDataResponsePacket(getLuckpermsMetaData(packet.key)))
            }
        }
    }

    override fun handleConnectPlayerToServer(packet: ServerboundConnectPlayerToServerPacket) {
        val (uuid, serverName, queue, sendQueuedMessage) = packet
        withPlayer(uuid, {
            packet.respond(ClientboundConnectPlayerToServerResponse(ConnectionResultEnum.DISCONNECTED))
        }) {
            PacketHandlerScope.launch {
                try {
                    val result =
                        when (val server = CloudServerManager.retrieveServerByName(serverName)) {
                            null -> ConnectionResultEnum.SERVER_NOT_FOUND(serverName)
                            !is CloudServer -> ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY
                            else -> withContext(QueueConnectionScope.context) {
                                if (queue) connectToServerOrQueue(server, sendQueuedMessage)
                                else connectToServer(server)
                            }
                        }

                    packet.respond(ClientboundConnectPlayerToServerResponse(result))
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    log.atWarning()
                        .per(serverName, LogPerBucketingStrategy.byHashCode(25))
                        .atMostEvery(15, TimeUnit.SECONDS)
                        .withCause(e)
                        .log("Failed to connect player ${packet.uuid} to server $serverName!")

                    packet.respond(ClientboundConnectPlayerToServerResponse(ConnectionResultEnum.SERVER_DISCONNECTED))
                }
            }
        }
    }

    override fun handleQueuePlayerToGroup(packet: ServerboundQueuePlayerToGroupPacket) {
        val (uuid, group, sendQueuedMessage) = packet
        withPlayer(uuid, {
            packet.respond(ClientboundConnectPlayerToServerResponse(ConnectionResultEnum.DISCONNECTED))
        }) {
            QueueConnectionScope.launch {
                try {
                    val result = connectToServerOrQueue(group, sendQueuedMessage)
                    packet.respond(ClientboundConnectPlayerToServerResponse(result))
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    log.atWarning()
                        .per(group, LogPerBucketingStrategy.byHashCode(25))
                        .atMostEvery(15, TimeUnit.SECONDS)
                        .withCause(e)
                        .log("Failed to connect player ${packet.uuid} to group $group!")

                    packet.respond(ClientboundConnectPlayerToServerResponse(ConnectionResultEnum.SERVER_DISCONNECTED))
                }
            }
        }
    }

    override fun handleDisconnectPlayer(packet: DisconnectPlayerPacket) {
        withPlayer(packet.uuid) { disconnect(packet.reason) }
    }

    override fun handleSilentDisconnectPlayer(packet: SilentDisconnectPlayerPacket) {
        withPlayer(packet.uuid) { disconnectSilent() }
    }

    override fun handleTeleportPlayer(packet: TeleportPlayerPacket) {
        withPlayer(packet.uuid) {
            PacketHandlerScope.launch {
                try {
                    val result = teleport(
                        location = packet.location,
                        teleportCause = packet.teleportCause,
                        flags = packet.flags.toTypedArray()
                    )

                    packet.respond(result)
                } catch (e: Throwable) {
                    log.atWarning()
                        .log("Failed to teleport player ${packet.uuid} to location ${packet.location}!")
                    packet.respond(false)
                }
            }
        }
    }

    override fun handleTeleportPlayerToPlayer(packet: TeleportPlayerToPlayerPacket) {
        val player = CloudPlayerManager.getPlayer(packet.uuid)
        val targetPlayer = CloudPlayerManager.getPlayer(packet.target)

        if (player == null || targetPlayer == null) {
            packet.respond(false)
            log.atWarning()
                .log("Failed to teleport player ${packet.uuid} to player ${packet.target}! One of the players is not online!")
            return
        }

        PacketHandlerScope.launch {
            try {
                packet.respond(player.teleport(targetPlayer))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to teleport player ${packet.uuid} to player ${packet.target}!")
                packet.respond(false)
            }
        }
    }

    override fun handleShutdownServer(packet: ServerboundShutdownServerPacket) {
        val server = CloudServerManager.retrieveServerByName(packet.serverName) ?: return
        server.shutdown()
    }

    override fun handleRequestPlayerData(packet: ServerboundRequestPlayerDataPacket) {
        PacketHandlerScope.launch {
            try {
                packet.respond(
                    ServerboundRequestPlayerDataResponse(
                        packet.type.readData(
                            playerManagerImpl.getOfflinePlayer(
                                packet.uuid
                            )
                        )
                    )
                )
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to retrieve player data for player ${packet.uuid}!")
            }
        }
    }

    override fun handleUpdateAFKState(packet: UpdateAFKStatePacket) {
        withPlayer(packet.uuid) { updateAfkStatus(packet.isAfk) }
    }

    override fun handleGeneratePunishmentId(packet: ServerboundGeneratePunishmentIdPacket) {
        PunishmentHandlerScope.launch {
            try {
                packet.respond(bean<PunishmentManager>().generatePunishmentId())
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to generate punishment id!")
            }
        }
    }

    override fun handleCreateKick(packet: ServerboundCreateKickPacket) {
        PunishmentHandlerScope.launch {
            try {
                val punishment = bean<PunishmentManager>().createKick(
                    punishedUuid = packet.punishedUuid,
                    issuerUuid = packet.issuerUuid,
                    reason = packet.reason,
                    initialNotes = packet.initialNotes,
                    parentId = packet.parentId
                )
                packet.respond(ClientboundCreatedPunishmentResponsePacket(punishment))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to create kick punishment!")
            }
        }
    }

    override fun handleCreateWarn(packet: ServerboundCreateWarnPacket) {
        PunishmentHandlerScope.launch {
            try {
                val punishment = bean<PunishmentManager>().createWarn(
                    punishedUuid = packet.punishedUuid,
                    issuerUuid = packet.issuerUuid,
                    reason = packet.reason,
                    initialNotes = packet.initialNotes,
                    parentId = packet.parentId
                )
                packet.respond(ClientboundCreatedPunishmentResponsePacket(punishment))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to create warn punishment!")
            }
        }
    }

    override fun handleCreateMute(packet: ServerboundCreateMutePacket) {
        PunishmentHandlerScope.launch {
            try {
                val punishment = bean<PunishmentManager>().createMute(
                    punishedUuid = packet.punishedUuid,
                    issuerUuid = packet.issuerUuid,
                    reason = packet.reason,
                    permanent = packet.permanent,
                    expirationDate = packet.expirationDate,
                    initialNotes = packet.initialNotes,
                    parentId = packet.parentId
                )
                packet.respond(ClientboundCreatedPunishmentResponsePacket(punishment))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to create mute punishment!")
            }
        }
    }

    override fun handleCreateBan(packet: ServerboundCreateBanPacket) {
        PunishmentHandlerScope.launch {
            try {
                val punishment = bean<PunishmentManager>().createBan(
                    punishedUuid = packet.punishedUuid,
                    issuerUuid = packet.issuerUuid,
                    reason = packet.reason,
                    permanent = packet.permanent,
                    expirationDate = packet.expirationDate,
                    securityBan = packet.securityBan,
                    raw = packet.raw,
                    initialNotes = packet.initialNotes,
                    initialIpAddresses = packet.initialIpAddresses,
                    parentId = packet.parentId
                )

                packet.respond(ClientboundCreatedPunishmentResponsePacket(punishment))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to create ban punishment!")
            }
        }
    }

    override fun handleAttachIpAddressToBan(packet: ServerboundAttachIpAddressToBanPacket) {
        PunishmentHandlerScope.launch {
            try {
                packet.respond(
                    bean<PunishmentManager>().attachIpAddressToBan(
                        packet.banId,
                        packet.rawIp
                    )
                )
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to attach ip address to ban!")
            }
        }
    }

    override fun handleAttachNoteToPunishment(packet: ServerboundAttachNoteToPunishmentPacket) {
        PunishmentHandlerScope.launch {
            try {
                val (id, rawNote, type) = packet
                val manager = bean<PunishmentManager>()
                val note = when (type) {
                    PunishmentType.BAN -> manager.attachNoteToBan(id, rawNote)
                    PunishmentType.MUTE -> manager.attachNoteToMute(id, rawNote)
                    PunishmentType.KICK -> manager.attachNoteToKick(id, rawNote)
                    PunishmentType.WARN -> manager.attachNoteToWarn(id, rawNote)
                }
                packet.respond(ClientboundAttachedNoteToPunishmentResponse(note))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to attach note to punishment!")
            }
        }
    }

    override fun handleFetchNotesFromPunishment(packet: ServerboundFetchNotesFromPunishmentPacket) {
        PunishmentHandlerScope.launch {
            try {
                val (id, type) = packet
                val manager = bean<PunishmentManager>()
                val notes = when (type) {
                    PunishmentType.BAN -> manager.fetchNotesForBan(id)
                    PunishmentType.MUTE -> manager.fetchNotesForMute(id)
                    PunishmentType.KICK -> manager.fetchNotesForKick(id)
                    PunishmentType.WARN -> manager.fetchNotesForWarn(id)
                }.toMutableObjectList()
                packet.respond(ClientboundFetchNotesFromPunishmentResponse(notes))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch notes from punishment!")
            }
        }
    }

    override fun handleFetchMutes(packet: ServerboundFetchMutesPacket) {
        PunishmentHandlerScope.launch {
            try {
                val mutes = bean<PunishmentManager>()
                    .fetchMutes(packet.punishedUuid, packet.onlyActive)
                packet.respond(ClientboundFetchedPunishmentsResponsePacket(mutes))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch mutes!")
            }
        }
    }

    override fun handleFetchBans(packet: ServerboundFetchBansPacket) {
        PunishmentHandlerScope.launch {
            try {
                val bans =
                    bean<PunishmentManager>().fetchBans(packet.punishedUuid, packet.onlyActive)
                packet.respond(ClientboundFetchedPunishmentsResponsePacket(bans))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch bans!")
            }
        }
    }

    override fun handleFetchKicks(packet: ServerboundFetchKicksPacket) {
        PunishmentHandlerScope.launch {
            try {
                val kicks = bean<PunishmentManager>().fetchKicks(packet.punishedUuid)
                packet.respond(ClientboundFetchedPunishmentsResponsePacket(kicks))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch kicks!")
            }
        }
    }

    override fun handleFetchWarns(packet: ServerboundFetchWarnsPacket) {
        PunishmentHandlerScope.launch {
            try {
                val warns = bean<PunishmentManager>().fetchWarnings(packet.punishedUuid)
                packet.respond(ClientboundFetchedPunishmentsResponsePacket(warns))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch warns!")
            }
        }
    }

    override fun handleGetCurrentLoginValidationPunishmentCache(packet: ServerboundGetCurrentLoginValidationPunishmentCachePacket) {
        PunishmentHandlerScope.launch {
            try {
                val cache = bean<PunishmentManager>()
                    .getCurrentLoginValidationPunishmentCache(packet.uuid)
                val response =
                    ClientboundGetCurrentLoginValidationPunishmentCacheResponsePacket(cache)
                packet.respond(response)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch current login validation punishment cache!")
            }
        }
    }

    override fun handleFetchIpAddressesForBan(packet: ServerboundFetchIpAddressesForBanPacket) {
        PunishmentHandlerScope.launch {
            try {
                val ipAddresses = bean<PunishmentManager>()
                    .fetchIpAddressesForBan(packet.banId)
                    .toMutableObjectList()

                packet.respond(ClientboundFetchIpAddressesResponsePacket(ipAddresses))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch ip addresses for ban!")
            }
        }
    }

    override fun handleFetchIpBans(packet: ServerboundFetchIpBansPacket) {
        PunishmentHandlerScope.launch {
            try {
                val bans = bean<PunishmentManager>().fetchIpBans(packet.ip, packet.onlyActive)
                packet.respond(ClientboundFetchedPunishmentsResponsePacket(bans))
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fetch ip bans!")
            }
        }
    }

    override fun handleRequestPlayerPermission(packet: RequestPlayerPermissionPacket) {
        withPlayer(packet.uuid) {
            PacketHandlerScope.launch {
                try {
                    packet.respond(hasPermission(packet.permission))
                } catch (e: Throwable) {
                    packet.respond(false)
                    if (e is CancellationException) throw e
                    log.atWarning()
                        .withCause(e)
                        .log("Failed to check if player ${packet.uuid} has permission ${packet.permission}!")
                }
            }
        }
    }

    override fun handleSyncValueChange(packet: SyncValueChangePacket) {
        try {
            SyncRegistryImpl.instance.handleChangePacket(packet, connection)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle sync value change packet: %s", packet.syncId)
        }
    }

    override fun handleSyncSetDelta(packet: SyncSetDeltaPacket) {
        try {
            SyncRegistryImpl.instance.handleSyncSetDeltaPacket(packet, connection)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle sync set delta packet: %s", packet.setId)
        }
    }

    override fun handleCreateOfflineCloudPlayerIfNotExists(packet: ServerboundCreateOfflineCloudPlayerIfNotExistsPacket) {
        CloudPlayerManager.getOfflinePlayer(packet.uuid, true)
    }

    override fun handleRequestWhitelistStatus(packet: ServerboundRequestWhitelistStatusPacket) {
        PacketHandlerIoScope.launch {
            try {
                val status =
                    bean<WhitelistService>().whitelistStatus(packet.uuid, packet.groupOrServer)
                packet.respond(WhitelistStatusResponsePacket(status))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to handle whitelist status request for %s", packet.uuid)
                packet.respond(WhitelistStatusResponsePacket(WhitelistStatus.UNKNOWN))
            }
        }
    }

    override fun handleRequestWhitelist(packet: ServerboundRequestWhitelistPacket) {
        PacketHandlerIoScope.launch {
            try {
                val whitelist =
                    bean<WhitelistService>().getWhitelist(packet.uuid, packet.groupOrServer)
                packet.respond(WhitelistResponsePacket(whitelist))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to handle whitelist request for %s", packet.uuid)
                packet.respond(WhitelistResponsePacket(null))
            }
        }
    }

    override fun handleCreateWhitelist(packet: ServerboundCreateWhitelistPacket) {
        PacketHandlerIoScope.launch {
            try {
                val whitelist = bean<WhitelistService>().createWhitelist(packet.entry)
                packet.respond(WhitelistResponsePacket(whitelist))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to handle create whitelist request for %s", packet.entry)
                packet.respond(WhitelistResponsePacket(null))
            }
        }
    }

    override fun handleUpdateWhitelist(packet: ServerboundUpdateWhitelistPacket) {
        PacketHandlerIoScope.launch {
            try {
                val changed = bean<WhitelistService>().updateWhitelist(packet.updated)
                packet.respond(changed)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to handle update whitelist request for %s", packet.updated)
                packet.respond(false)
            }
        }
    }

    override fun handleRefreshWhitelist(packet: ServerboundRefreshWhitelistPacket) {
        try {
            WhitelistSettings.refresh()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle refresh whitelist request")
        }
    }

    override fun handlePullPlayersToGroup(packet: ServerboundPullPlayersToGroupPacket) {
        PacketHandlerScope.launch {
            try {
                val players = packet.players.mapNotNull(CloudPlayer::get)
                val result = CloudServerManager.pullPlayersToGroup(packet.group, players)
                val networkResults = result
                    .mapTo(mutableObjectListOf(result.size)) { (player, result) -> player.uuid to result }

                packet.respond(PullPlayersToGroupResponsePacket(Either.left(networkResults)))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to handle pull players to group request for %s", packet.players)

                packet.respond(PullPlayersToGroupResponsePacket(Either.right(e.message)))
            }
        }
    }

    override fun handleCacheRegisterKeys(packet: ServerboundCacheRegisterKeysPacket) {
        TODO("Not yet implemented")
    }

    override fun handleCacheOp(packet: ServerboundCacheOpPacket) {
        TODO("Not yet implemented")
    }

    override fun handleCacheFetch(packet: ServerboundCacheFetchPacket) {
        TODO("Not yet implemented")
    }

    override fun handleCacheWatchPlayers(packet: ServerboundCacheWatchPlayersPacket) {
        TODO("Not yet implemented")
    }

    override fun handleSendToast(packet: SendToastPacket) {
        withPlayer(packet.uuid) { sendToast(packet.toast) }
    }

    override fun handleUpdatePlayerPersistentDataContainer(packet: UpdatePlayerPersistentDataContainerPacket) {
        withPlayer(packet.uuid) {
            try {
                applyPpdcPatch(packet.patch)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to apply ppdc patch for %s", packet.uuid)
            }
        }
    }

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val info = NettyPacketInfo(connection, ConnectionProtocol.RUNNING)

        for (listener in listeners) {
            PacketHandlerScope.launch {
                try {
                    listener.handle(packet, info)
                } catch (e: Throwable) {
                    log.atWarning()
                        .withCause(e)
                        .log(
                            "Failed to call listener %s for packet %s",
                            listener.owner::class.simpleName,
                            packet::class.simpleName
                        )
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun withPlayer(
        uuid: UUID,
        orElse: () -> Unit = {},
        block: StandaloneCloudPlayerImpl.() -> Unit
    ) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
            callsInPlace(orElse, InvocationKind.AT_MOST_ONCE)
        }

        val player =
            playerManagerImpl.getPlayer(uuid) as? StandaloneCloudPlayerImpl ?: return orElse()
        player.block()
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }
}

