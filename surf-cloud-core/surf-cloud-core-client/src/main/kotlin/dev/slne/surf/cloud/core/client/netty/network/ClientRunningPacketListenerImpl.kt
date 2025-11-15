package dev.slne.surf.cloud.core.client.netty.network

import com.google.common.flogger.LogPerBucketingStrategy
import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent
import dev.slne.surf.cloud.api.common.event.player.afk.AfkStateChangeEvent
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.core.client.player.commonPlayerManagerImpl
import dev.slne.surf.cloud.core.client.server.ClientCloudServerImpl
import dev.slne.surf.cloud.core.client.server.ClientProxyCloudServerImpl
import dev.slne.surf.cloud.core.client.server.serverManagerImpl
import dev.slne.surf.cloud.core.client.sync.SyncRegistryImpl
import dev.slne.surf.cloud.core.client.util.getOrLoadUser
import dev.slne.surf.cloud.core.client.util.luckperms
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundSetVelocitySecretPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ClientboundCacheRegisterAckPacket
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.player.task.PrePlayerJoinTaskManager
import dev.slne.surf.cloud.core.common.util.hasPermissionPlattform
import dev.slne.surf.surfapi.core.api.messages.adventure.getPointer
import dev.slne.surf.surfapi.core.api.util.logger
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
    val client: ClientNettyClientImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension
) : ClientCommonPacketListenerImpl(connection), RunningClientPacketListener {
    private val log = logger()

    override fun handlePlayerConnectedToServer(packet: PlayerConnectedToServerPacket) {
        PacketHandlerScope.launch {
            commonPlayerManagerImpl.updateOrCreatePlayer(
                packet.uuid,
                packet.name,
                packet.proxy,
                packet.playerIp,
                packet.serverName,
                false
            ) {
                overwritePpdc(packet.pdc)
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
        }
    }

    override fun handleSendResourcePacks(packet: ClientboundSendResourcePacksPacket) {
        withAudience(packet.uuid) { sendResourcePacks(packet.request) }
    }

    override fun handleClearResourcePacks(packet: ClientboundClearResourcePacksPacket) {
        withAudience(packet.uuid) { clearResourcePacks() }
    }

    override fun handleRemoveResourcePacks(packet: ClientboundRemoveResourcePacksPacket) {
        withAudience(packet.uuid) { removeResourcePacks(packet.packIds) }
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
            val permission = packet.permission

            if (emitter != null) {
                if (permission == null) {
                    playSound(packet.sound, emitter)
                } else {
                    if (hasPermissionPlattform(permission)) {
                        playSound(packet.sound, emitter)
                    }
                }
            } else if (x != null && y != null && z != null) {
                require(permission == null) { "Permission is not supported for this play sound type" }
                playSound(packet.sound, x, y, z)
            } else {
                require(permission == null) { "Permission is not supported for this play sound type" }
                playSound(packet.sound)
            }
        }
    }

    override fun handleStopSound(packet: ClientboundStopSoundPacket) {
        withAudience(packet.uuid) { stopSound(packet.soundStop) }
    }

    override fun handleSendMessage(packet: ClientboundSendMessagePacket) {
        withAudience(packet.uuid) {
            val message = packet.message
            val permission = packet.permission

            if (permission == null) {
                sendMessage(message)
            } else {
                if (hasPermissionPlattform(permission)) {
                    sendMessage(message)
                }
            }
        }
    }

    override fun handleSendActionBar(packet: ClientboundSendActionBarPacket) {
        withAudience(packet.uuid) { sendActionBar(packet.message) }
    }

    override fun handleSendPlayerListHeaderAndFooter(packet: ClientboundSendPlayerListHeaderAndFooterPacket) {
        withAudience(packet.uuid) { sendPlayerListHeaderAndFooter(packet.header, packet.footer) }
    }

    override fun handleRequestDisplayName(packet: ClientboundRequestDisplayNamePacket) {
        withRequiredAudience(packet.uuid, {
            val displayName = getPointer(Identity.DISPLAY_NAME) ?: Component.empty()
            packet.respond(ResponseDisplayNamePacketRequestPacket(packet.uuid, displayName))
        }) { "Display name requested for player ${packet.uuid} who is not online. Probably send to wrong server." }
    }

    override fun handleRequestOfflinePlayerDisplayName(packet: RequestOfflineDisplayNamePacket) {
        PacketHandlerScope.launch {
            try {
                val lpUser = luckperms.userManager.getOrLoadUser(packet.uuid)
                val lpName = lpUser.username
                packet.respond(lpName?.let(Component::text))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to get offline player display name for packet $packet")
                packet.respond(null)
            }
        }
    }

    override fun handleRegisterServerPacket(packet: ClientboundRegisterServerPacket) {
        val server = if (packet.proxy) {
            ClientProxyCloudServerImpl(
                packet.group,
                packet.serverName,
                packet.playAddress,
            )
        } else {
            ClientCloudServerImpl(
                packet.group,
                packet.serverName,
                packet.playAddress,
                packet.lobby
            ).also { client ->
                try {
                    platformExtension.registerCloudServerToProxy(client)
                } catch (e: Throwable) {
                    log.atWarning()
                        .withCause(e)
                        .log("Failed to register server ${packet.serverName} to proxy")
                }
            }
        }

        serverManagerImpl.registerServer(server)
    }

    override fun handleUnregisterServerPacket(packet: ClientboundUnregisterServerPacket) {
        val removed = serverManagerImpl.unregisterServer(packet.serverName)

        if (removed is ClientCloudServerImpl) {
            platformExtension.unregisterCloudServerFromProxy(removed)
        }
    }

    override fun handleAddPlayerToServer(packet: ClientboundAddPlayerToServerPacket) {
        val server = serverManagerImpl.retrieveServerByName(packet.serverName) ?: return
        val users = server.users
        check(users is UserListImpl) { "Server ${server.name} has unsupported user list implementation: ${users::class.simpleName}" }
        users.add(packet.playerUuid)
    }

    override fun handleRemovePlayerFromServer(packet: ClientboundRemovePlayerFromServerPacket) {
        val server = serverManagerImpl.retrieveServerByName(packet.serverName) ?: return
        val users = server.users
        check(users is UserListImpl) { "Server ${server.name} has unsupported user list implementation: ${users::class.simpleName}" }
        users.remove(packet.playerUuid)
    }

    override fun handleUpdateServerInformation(packet: ClientboundUpdateServerInformationPacket) {
        serverManagerImpl.updateServerInformationNow(packet.serverName, packet.information)
    }

    override fun handleIsServerManagedByThisProxy(packet: ClientboundIsServerManagedByThisProxyPacket) {
        val managed = platformExtension.isServerManagedByThisProxy(packet.clientAddress)
        packet.respond(ServerboundIsServerManagedByThisProxyResponse(managed))
    }

    override fun handleTransferPlayer(packet: ClientboundTransferPlayerPacket) {
        PacketHandlerScope.launch {
            try {
                val (status, reason) = platformExtension.transferPlayerToServer(
                    packet.playerUuid,
                    packet.address
                )
                packet.respond(ServerboundTransferPlayerPacketResponse(status, reason))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to transfer player ${packet.playerUuid} to server at ${packet.address}")
            }
        }
    }

    override fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket) {
        PacketHandlerScope.launch {
            try {
                val lpUser = luckperms.userManager.getOrLoadUser(packet.uuid)
                val metaData = lpUser.cachedData.metaData.getMetaValue(packet.key)
                packet.respond(LuckpermsMetaDataResponsePacket(metaData))
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to get luckperms meta data for packet $packet")
                packet.respond(LuckpermsMetaDataResponsePacket(null))
            }
        }
    }

    override fun handleDisconnectPlayer(packet: DisconnectPlayerPacket) {
        try {
            platformExtension.disconnectPlayer(packet.uuid, packet.reason)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to disconnect player ${packet.uuid} with reason ${packet.reason}")
        }
    }

    override fun handleSilentDisconnectPlayer(packet: SilentDisconnectPlayerPacket) {
        try {
            platformExtension.silentDisconnectPlayer(packet.uuid)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to silent disconnect player ${packet.uuid}")
        }
    }

    override fun handleTeleportPlayer(packet: TeleportPlayerPacket) {
        PacketHandlerScope.launch {
            try {
                val result = platformExtension.teleportPlayer(
                    packet.uuid,
                    packet.location,
                    packet.teleportCause,
                    packet.flags
                )
                packet.respond(result)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to teleport player ${packet.uuid} to location ${packet.location}")
                packet.respond(false)
            }
        }
    }

    override fun handleTeleportPlayerToPlayer(packet: TeleportPlayerToPlayerPacket) {
        PacketHandlerScope.launch {
            try {
                val result = platformExtension.teleportPlayerToPlayer(packet.uuid, packet.target)
                packet.respond(result)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to teleport player ${packet.uuid} to player ${packet.target}")
                packet.respond(false)
            }
        }
    }

    override fun handleRegisterCloudServersToProxy(packet: ClientboundRegisterCloudServersToProxyPacket) {
        for (info in packet.servers) {
            try {
                platformExtension.registerCloudServerToProxy(info)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to register server ${info.first} to proxy")
            }
        }
    }

    override fun handleTriggerShutdown(packet: ClientboundTriggerShutdownPacket) {
        platformExtension.triggerShutdown()
    }

    override fun handleUpdateAFKState(packet: UpdateAFKStatePacket) {
        playerManagerImpl.getPlayer(packet.uuid)?.let { player ->
            require(player is ClientCloudPlayerImpl<*>) { "Player $player is not a client player" }
            player.afk = packet.isAfk
            AfkStateChangeEvent(packet.isAfk, this, player).postAndForget()
        }
    }

    override fun handleRunPlayerPreJoinTasks(packet: ClientboundRunPrePlayerJoinTasksPacket) {
        PacketHandlerScope.launch {
            try {
                val player = commonPlayerManagerImpl.getOfflinePlayer(packet.uuid)
                val result = PrePlayerJoinTaskManager.runTasks(player)
                packet.respond(RunPrePlayerJoinTasksResultPacket(result))
            } catch (e: Throwable) {
                log.atWarning()
                    .per(e.javaClass, LogPerBucketingStrategy.byClass())
                    .atMostEvery(5, TimeUnit.SECONDS)
                    .withCause(e)
                    .log("Failed to run pre player join tasks for player ${packet.uuid}")
                packet.respond(RunPrePlayerJoinTasksResultPacket(PrePlayerJoinTask.Result.ERROR))
            }
        }
    }

    override fun handleTriggerPunishmentUpdateEvent(packet: ClientboundTriggerPunishmentUpdateEventPacket) {
        val (updatedPunishment, operation) = packet
        try {
            CloudPlayerPunishmentUpdatedEvent(
                this,
                updatedPunishment.punishedPlayer(),
                updatedPunishment,
                operation
            ).postAndForget()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish punishment update event for punishment $updatedPunishment from packet")
        }
    }

    override fun handleTriggerPunishmentCreatedEvent(packet: ClientboundTriggerPunishmentCreatedEventPacket) {
        val punishment = packet.createdPunishment
        try {
            CloudPlayerPunishEvent(
                this,
                punishment.punishedPlayer(),
                punishment,
            ).postAndForget()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish punishment created event for punishment $punishment from packet")
        }
    }

    override fun handleRequestPlayerPermission(packet: RequestPlayerPermissionPacket) {
        withAudience(packet.uuid) {
            val permission = hasPermissionPlattform(packet.permission)
            packet.respond(permission)
        }
    }

    override fun handleSyncValueChange(packet: SyncValueChangePacket) {
        try {
            if (!packet.registered) return
            SyncRegistryImpl.instance.updateSyncValue(packet.syncId, packet.value)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to update sync value for packet $packet")
        }
    }

    override fun handleSyncSetDelta(packet: SyncSetDeltaPacket) {
        try {
            SyncRegistryImpl.instance.handleSyncSetDelta(packet)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle sync set delta for packet $packet")
        }
    }

    override fun handleSetVelocitySecret(packet: ClientboundSetVelocitySecretPacket) {
        try {
            client.velocitySecret = packet.secret
            platformExtension.setVelocitySecret(packet.secret)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to set velocity secret for packet $packet")
        }
    }

    override fun handleCacheRegisterAck(packet: ClientboundCacheRegisterAckPacket) {
        TODO("Not yet implemented")
    }

    override fun handleCacheDelta(packet: ClientboundCacheDeltaPacket) {
        TODO("Not yet implemented")
    }

    override fun handleCacheError(packet: ClientboundCacheErrorPacket) {
        TODO("Not yet implemented")
    }

    override fun handleSendToast(packet: SendToastPacket) {
        try {
            platformExtension.sendToast(packet.uuid, packet.toast)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to send toast to player ${packet.uuid}")
        }
    }

    override fun handleUpdatePlayerPersistentDataContainer(packet: UpdatePlayerPersistentDataContainerPacket) {
        val player = commonPlayerManagerImpl.getPlayer(packet.uuid) ?: return
        try {
            player.applyPpdcPatch(packet.patch)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to apply patch to player ${packet.uuid}'s persistent data container")
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
            log.atWarning()
                .withStackTrace(StackSize.SMALL)
                .log(errorMessage())
            return
        }

        audience.block()
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected && !closed
    }

    override fun restart() {
        platformExtension.restart()
    }

    override fun shutdown() {
        platformExtension.shutdown()
    }
}