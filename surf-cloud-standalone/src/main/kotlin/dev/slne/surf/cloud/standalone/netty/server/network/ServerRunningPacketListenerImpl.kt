package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistSettings
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.mutableIntSetOf
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.coroutines.QueueConnectionScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
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
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import dev.slne.surf.cloud.standalone.sync.SyncRegistryImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.random
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val result = playerManagerImpl.updateOrCreatePlayer(
            packet.uuid,
            packet.name,
            packet.proxy,
            packet.playerIp,
            packet.serverName,
            true
        )

        packet.respond(PlayerConnectToServerResponsePacket(result))

        broadcast(
            PlayerConnectedToServerPacket(
                packet.uuid,
                packet.name,
                packet.serverName,
                packet.proxy,
                packet.playerIp
            )
        )
        serverManagerImpl.getCommonStandaloneServerByName(packet.serverName)
            ?.handlePlayerConnect(packet.uuid)
    }

    override suspend fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket) {
        playerManagerImpl.updateOrRemoveOnDisconnect(packet.uuid, packet.serverName, packet.proxy)
        serverManagerImpl.getCommonStandaloneServerByName(packet.serverName)
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

    override suspend fun handleRequestOfflinePlayerDisplayName(packet: RequestOfflineDisplayNamePacket) {
        val name = serverManagerImpl.requestOfflineDisplayName(packet.uuid)
        packet.respond(name)
    }

    override suspend fun handleClientInformation(packet: ServerboundClientInformationPacket) {
        val server = serverManagerImpl.retrieveServerByName(packet.serverName) ?: return

        if (server is StandaloneProxyCloudServerImpl) {
            server.information = packet.information
        } else if (server is StandaloneCloudServerImpl) {
            server.information = packet.information
        }

        broadcast(ClientboundUpdateServerInformationPacket(packet.serverName, packet.information))
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
        val (uuid, serverName, queue, sendQueuedMessage) = packet
        withPlayer(uuid) {
            val result = when (val server = CloudServerManager.retrieveServerByName(serverName)) {
                null -> ConnectionResultEnum.SERVER_NOT_FOUND(serverName.toString())
                !is CloudServer -> ConnectionResultEnum.CANNOT_CONNECT_TO_PROXY
                else -> withContext(QueueConnectionScope.context) {
                    if (queue) connectToServerOrQueue(server, sendQueuedMessage)
                    else connectToServer(server)
                }
            }

            packet.respond(ClientboundConnectPlayerToServerResponse(result))
        }
    }

    override suspend fun handleQueuePlayerToGroup(packet: ServerboundQueuePlayerToGroupPacket) {
        val (uuid, group, sendQueuedMessage) = packet
        withPlayer(uuid) {
            val result = withContext(QueueConnectionScope.context) {
                connectToServerOrQueue(group, sendQueuedMessage)
            }

            packet.respond(ClientboundConnectPlayerToServerResponse(result))
        }
    }

    override fun handleDisconnectPlayer(packet: DisconnectPlayerPacket) {
        withPlayer(packet.uuid) { disconnect(packet.reason) }
    }

    override fun handleSilentDisconnectPlayer(packet: SilentDisconnectPlayerPacket) {
        withPlayer(packet.uuid) { disconnectSilent() }
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

    override suspend fun handleTeleportPlayerToPlayer(packet: TeleportPlayerToPlayerPacket) {
        val player = CloudPlayerManager.getPlayer(packet.uuid)
        val targetPlayer = CloudPlayerManager.getPlayer(packet.target)

        if (player == null || targetPlayer == null) {
            packet.respond(false)
            return
        }

        packet.respond(player.teleport(targetPlayer))
    }

    override suspend fun handleShutdownServer(packet: ServerboundShutdownServerPacket) {
        val server = CloudServerManager.retrieveServerByName(packet.serverName) ?: return
        server.shutdown()
    }

    override suspend fun handleRequestPlayerData(packet: ServerboundRequestPlayerDataPacket) {
        packet.respond(
            ServerboundRequestPlayerDataResponse(
                packet.type.readData(
                    playerManagerImpl.getOfflinePlayer(
                        packet.uuid
                    )
                )
            )
        )
    }

    override fun handleUpdateAFKState(packet: UpdateAFKStatePacket) {
        withPlayer(packet.uuid) { updateAfkStatus(packet.isAfk) }
    }

    override fun handleGeneratePunishmentId(packet: ServerboundGeneratePunishmentIdPacket) {
        PunishmentHandlerScope.launch {
            packet.respond(bean<PunishmentManager>().generatePunishmentId())
        }
    }

    override fun handleCreateKick(packet: ServerboundCreateKickPacket) {
        PunishmentHandlerScope.launch {
            val punishment = bean<PunishmentManager>().createKick(
                punishedUuid = packet.punishedUuid,
                issuerUuid = packet.issuerUuid,
                reason = packet.reason,
                initialNotes = packet.initialNotes,
                parentId = packet.parentId
            )

            packet.respond(ClientboundCreatedPunishmentResponsePacket(punishment))
        }
    }

    override fun handleCreateWarn(packet: ServerboundCreateWarnPacket) {
        PunishmentHandlerScope.launch {
            val punishment = bean<PunishmentManager>().createWarn(
                punishedUuid = packet.punishedUuid,
                issuerUuid = packet.issuerUuid,
                reason = packet.reason,
                initialNotes = packet.initialNotes,
                parentId = packet.parentId
            )

            packet.respond(ClientboundCreatedPunishmentResponsePacket(punishment))
        }
    }

    override fun handleCreateMute(packet: ServerboundCreateMutePacket) {
        PunishmentHandlerScope.launch {
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
        }
    }

    override fun handleCreateBan(packet: ServerboundCreateBanPacket) {
        PunishmentHandlerScope.launch {
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
        }
    }

    override fun handleAttachIpAddressToBan(packet: ServerboundAttachIpAddressToBanPacket) {
        PunishmentHandlerScope.launch {
            packet.respond(
                bean<PunishmentManager>().attachIpAddressToBan(
                    packet.banId,
                    packet.rawIp
                )
            )
        }
    }

    override fun handleAttachNoteToPunishment(packet: ServerboundAttachNoteToPunishmentPacket) {
        PunishmentHandlerScope.launch {
            val (id, rawNote, type) = packet
            val manager = bean<PunishmentManager>()
            val note = when (type) {
                PunishmentType.BAN -> manager.attachNoteToBan(id, rawNote)
                PunishmentType.MUTE -> manager.attachNoteToMute(id, rawNote)
                PunishmentType.KICK -> manager.attachNoteToKick(id, rawNote)
                PunishmentType.WARN -> manager.attachNoteToWarn(id, rawNote)
            }

            packet.respond(ClientboundAttachedNoteToPunishmentResponse(note))
        }
    }

    override fun handleFetchNotesFromPunishment(packet: ServerboundFetchNotesFromPunishmentPacket) {
        PunishmentHandlerScope.launch {
            val (id, type) = packet
            val manager = bean<PunishmentManager>()
            val notes = when (type) {
                PunishmentType.BAN -> manager.fetchNotesForBan(id)
                PunishmentType.MUTE -> manager.fetchNotesForMute(id)
                PunishmentType.KICK -> manager.fetchNotesForKick(id)
                PunishmentType.WARN -> manager.fetchNotesForWarn(id)
            }
            packet.respond(ClientboundFetchNotesFromPunishmentResponse(notes))
        }
    }

    override fun handleFetchMutes(packet: ServerboundFetchMutesPacket) {
        PunishmentHandlerScope.launch {
            val mutes = bean<PunishmentManager>().fetchMutes(packet.punishedUuid, packet.onlyActive)
            packet.respond(ClientboundFetchedPunishmentsResponsePacket(mutes))
        }
    }

    override fun handleFetchBans(packet: ServerboundFetchBansPacket) {
        PunishmentHandlerScope.launch {
            val bans = bean<PunishmentManager>().fetchBans(packet.punishedUuid, packet.onlyActive)
            packet.respond(ClientboundFetchedPunishmentsResponsePacket(bans))
        }
    }

    override fun handleFetchKicks(packet: ServerboundFetchKicksPacket) {
        PunishmentHandlerScope.launch {
            val kicks = bean<PunishmentManager>().fetchKicks(packet.punishedUuid)
            packet.respond(ClientboundFetchedPunishmentsResponsePacket(kicks))
        }
    }

    override fun handleFetchWarns(packet: ServerboundFetchWarnsPacket) {
        PunishmentHandlerScope.launch {
            val warns = bean<PunishmentManager>().fetchWarnings(packet.punishedUuid)
            packet.respond(ClientboundFetchedPunishmentsResponsePacket(warns))
        }
    }

    override fun handleGetCurrentLoginValidationPunishmentCache(packet: ServerboundGetCurrentLoginValidationPunishmentCachePacket) {
        PunishmentHandlerScope.launch {
            val cache =
                bean<PunishmentManager>().getCurrentLoginValidationPunishmentCache(packet.uuid)
            packet.respond(ClientboundGetCurrentLoginValidationPunishmentCacheResponsePacket(cache))
        }
    }

    override fun handleFetchIpAddressesForBan(packet: ServerboundFetchIpAddressesForBanPacket) {
        PunishmentHandlerScope.launch {
            val ipAddresses = bean<PunishmentManager>().fetchIpAddressesForBan(packet.banId)
            packet.respond(ClientboundFetchIpAddressesResponsePacket(ipAddresses))
        }
    }

    override fun handleFetchIpBans(packet: ServerboundFetchIpBansPacket) {
        PunishmentHandlerScope.launch {
            val bans = bean<PunishmentManager>().fetchIpBans(packet.ip, packet.onlyActive)
            packet.respond(ClientboundFetchedPunishmentsResponsePacket(bans))
        }
    }

    override suspend fun handleRequestPlayerPermission(packet: RequestPlayerPermissionPacket) {
        withPlayer(packet.uuid) {
            packet.respond(hasPermission(packet.permission))
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

    override suspend fun handleRequestWhitelistStatus(packet: ServerboundRequestWhitelistStatusPacket) {
        try {
            val status = bean<WhitelistService>().whitelistStatus(packet.uuid, packet.groupOrServer)
            packet.respond(WhitelistStatusResponsePacket(status))
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle whitelist status request for %s", packet.uuid)
            packet.respond(WhitelistStatusResponsePacket(WhitelistStatus.UNKNOWN))
        }
    }

    override suspend fun handleRequestWhitelist(packet: ServerboundRequestWhitelistPacket) {
        try {
            val whitelist = bean<WhitelistService>().getWhitelist(packet.uuid, packet.groupOrServer)
            packet.respond(WhitelistResponsePacket(whitelist))
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle whitelist request for %s", packet.uuid)
            packet.respond(WhitelistResponsePacket(null))
        }
    }

    override suspend fun handleCreateWhitelist(packet: ServerboundCreateWhitelistPacket) {
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

    override suspend fun handleUpdateWhitelist(packet: ServerboundUpdateWhitelistPacket) {
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

    override fun handleRefreshWhitelist(packet: ServerboundRefreshWhitelistPacket) {
        try {
            WhitelistSettings.refresh()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle refresh whitelist request")
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

