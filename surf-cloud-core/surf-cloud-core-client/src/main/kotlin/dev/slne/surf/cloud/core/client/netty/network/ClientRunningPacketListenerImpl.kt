package dev.slne.surf.cloud.core.client.netty.network

import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishmentUpdatedEvent
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
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
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.player.task.PrePlayerJoinTaskManager
import dev.slne.surf.cloud.core.common.util.hasPermissionPlattform
import dev.slne.surf.surfapi.core.api.messages.adventure.getPointer
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.*

class ClientRunningPacketListenerImpl(
    connection: ConnectionImpl,
    val client: ClientNettyClientImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension
) : ClientCommonPacketListenerImpl(connection), RunningClientPacketListener {
    private val log = logger()

    override suspend fun handlePlayerConnectedToServer(packet: PlayerConnectedToServerPacket) {
        playerManagerImpl.updateOrCreatePlayer(
            packet.uuid,
            packet.name,
            packet.proxy,
            packet.playerIp,
            packet.serverUid,
            false
        )
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

    override suspend fun handleRequestOfflinePlayerDisplayName(packet: RequestOfflineDisplayNamePacket) {
        packet.respond(luckperms.userManager.getOrLoadUser(packet.uuid).username?.let { text(it) })
    }

    override suspend fun handleRegisterServerPacket(packet: ClientboundRegisterServerPacket) {
        val server = if (packet.proxy) {
            ClientProxyCloudServerImpl(
                packet.serverId,
                packet.group,
                packet.name,
                packet.playAddress,
            )
        } else {
            ClientCloudServerImpl(
                packet.serverId,
                packet.group,
                packet.name,
                packet.playAddress,
                packet.lobby
            ).also { client ->
                platformExtension.registerCloudServerToProxy(client)
            }
        }

        serverManagerImpl.registerServer(server)
    }

    override suspend fun handleUnregisterServerPacket(packet: ClientboundUnregisterServerPacket) {
        val removed = serverManagerImpl.unregisterServer(packet.serverId)
        if (removed is ClientCloudServerImpl) {
            platformExtension.unregisterCloudServerFromProxy(removed)
        }
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

    override fun handleSilentDisconnectPlayer(packet: SilentDisconnectPlayerPacket) {
        platformExtension.silentDisconnectPlayer(packet.uuid)
    }

    override suspend fun handleTeleportPlayer(packet: TeleportPlayerPacket) {
        val result = platformExtension.teleportPlayer(
            packet.uuid,
            packet.location,
            packet.teleportCause,
            packet.flags
        )

        packet.respond(TeleportPlayerResultPacket(result))
    }

    override suspend fun handleTeleportPlayerToPlayer(packet: TeleportPlayerToPlayerPacket) {
        packet.respond(platformExtension.teleportPlayerToPlayer(packet.uuid, packet.target))
    }

    override fun handleRegisterCloudServersToProxy(packet: ClientboundRegisterCloudServersToProxyPacket) {
        platformExtension.registerCloudServersToProxy(packet.servers)
    }

    override fun handleTriggerShutdown(packet: ClientboundTriggerShutdownPacket) {
        platformExtension.triggerShutdown()
    }

    override fun handleUpdateAFKState(packet: UpdateAFKStatePacket) {
        playerManagerImpl.getPlayer(packet.uuid)?.let { player ->
            require(player is ClientCloudPlayerImpl<*>) { "Player $player is not a client player" }
            player.afk = packet.isAfk
        }
    }

    override suspend fun handleRunPlayerPreJoinTasks(packet: ClientboundRunPrePlayerJoinTasksPacket) {
        val player = commonPlayerManagerImpl.getOfflinePlayer(packet.uuid)
        val result = PrePlayerJoinTaskManager.runTasks(player)
        packet.respond(RunPrePlayerJoinTasksResultPacket(result))
    }

    override suspend fun handleTriggerPunishmentUpdateEvent(packet: ClientboundTriggerPunishmentUpdateEventPacket) {
        val (updatedPunishment, operation) = packet
        try {
            CloudPlayerPunishmentUpdatedEvent(
                this,
                updatedPunishment.punishedPlayer(),
                updatedPunishment,
                operation
            ).post()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish punishment update event for punishment $updatedPunishment from packet")
        }
    }

    override suspend fun handleTriggerPunishmentCreatedEvent(packet: ClientboundTriggerPunishmentCreatedEventPacket) {
        val punishment = packet.createdPunishment
        try {
            CloudPlayerPunishEvent(
                this,
                punishment.punishedPlayer(),
                punishment,
            ).post()
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to publish punishment created event for punishment $punishment from packet")
        }
    }

    override suspend fun handleRequestPlayerPermission(packet: RequestPlayerPermissionPacket) {
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