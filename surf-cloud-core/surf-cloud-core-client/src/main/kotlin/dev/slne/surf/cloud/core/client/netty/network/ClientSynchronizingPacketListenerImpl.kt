package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.player.commonPlayerManagerImpl
import dev.slne.surf.cloud.core.client.server.ClientCloudServerImpl
import dev.slne.surf.cloud.core.client.server.ClientProxyCloudServerImpl
import dev.slne.surf.cloud.core.client.server.serverManagerImpl
import dev.slne.surf.cloud.core.client.sync.SyncRegistryImpl
import dev.slne.surf.cloud.core.common.coroutines.BeforeStartTaskScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundSetVelocitySecretPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.*
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.plugin.task.CloudSynchronizeTaskManager
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import kotlinx.coroutines.launch
import net.kyori.adventure.nbt.BinaryTagIO
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class ClientSynchronizingPacketListenerImpl(
    override val client: ClientNettyClientImpl,
    connection: ConnectionImpl,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    val statusUpdater: AbstractStatusUpdater,
) : ClientCommonPacketListenerImpl(connection), ClientSynchronizingPacketListener {

    private val log = logger()
    private val hydratingPlayers = AtomicBoolean(false)
    private val pendingHydrationPlayers =
        mutableObjectListOf<ClientboundSyncPlayerHydrationChunkPacket.Entry>()

    private var currentLargePpdcUuid: UUID? = null
    private var currentLargePpdc: ByteArray? = null
    private val pendingLargePpdcs = mutableObjectSetOf<UUID>()

    fun startSynchronizing() {
        statusUpdater.switchState(AbstractStatusUpdater.State.SYNCHRONIZING)

        BeforeStartTaskScope.launch {
            CloudSynchronizeTaskManager.executeTasks(client)

            statusUpdater.switchState(AbstractStatusUpdater.State.SYNCHRONIZE_WAIT_FOR_SERVER)
            send(FinishSynchronizingPacket)
        }
    }

    override fun handleSynchronizeFinish(packet: ClientboundSynchronizeFinishPacket) {
        statusUpdater.switchState(AbstractStatusUpdater.State.SYNCHRONIZED)

        PacketHandlerScope.launch {
            val listener = ClientRunningPacketListenerImpl(connection, client, platformExtension)
            connection.setupInboundProtocol(RunningProtocols.CLIENTBOUND, listener)
            connection.send(ServerboundSynchronizeFinishAcknowledgedPacket)
            connection.setupOutboundProtocol(RunningProtocols.SERVERBOUND)
            client.initListener(listener)
            statusUpdater.switchState(AbstractStatusUpdater.State.CONNECTED)
            client.synchronizeCallback.complete(Unit)
        }
    }

    override fun handleSyncValueChange(packet: SyncValueChangePacket) {
        try {
            if (!packet.registered) return
            SyncRegistryImpl.instance.updateSyncValue(packet.syncId, packet.value)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to update sync value for packet $packet")
        }
    }

    override fun handleBatchSyncValue(packet: ClientboundBatchSyncValuePacket) {
        try {
            SyncRegistryImpl.instance.applyBatchSyncValue(packet.syncValues)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to apply batch sync values for packet $packet")
        }
    }

    override fun handleBatchSyncSet(packet: ClientboundBatchSyncSetPacket) {
        try {
            SyncRegistryImpl.instance.applyBatchSyncSets(packet.syncSets)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to apply batch sync sets for packet $packet")
        }
    }

    override fun handleBatchUpdateServer(packet: ClientboundBatchUpdateServer) {
        serverManagerImpl.batchUpdateServer(packet.servers.map { data ->
            if (data.proxy) {
                ClientProxyCloudServerImpl(data.group, data.name, data.playAddress)
            } else {
                ClientCloudServerImpl(
                    data.group,
                    data.name,
                    data.playAddress,
                    data.lobby
                ).also { server ->
                    platformExtension.registerCloudServerToProxy(server)
                }
            }
        })
    }

    override fun handleSyncSetDelta(packet: SyncSetDeltaPacket) {
        try {
            SyncRegistryImpl.instance.handleSyncSetDelta(packet)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle sync set delta for packet $packet")
        }
    }

    override fun handleSetVelocitySecret(packet: ClientboundSetVelocitySecretPacket) {
        try {
            client.velocitySecret = packet.secret
            platformExtension.setVelocitySecret(packet.secret)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to set velocity secret for packet $packet")
        }
    }

    override fun handlePlayerCacheHydrateStart(packet: ClientboundPlayerCacheHydrateStartPacket) {
        TODO("Not yet implemented")
    }

    override fun handlePlayerCacheHydrateChunk(packet: ClientboundPlayerCacheHydrateChunkPacket) {
        TODO("Not yet implemented")
    }

    override fun handlePlayerCacheHydrateEnd(packet: ClientboundPlayerCacheHydrateEndPacket) {
        TODO("Not yet implemented")
    }

    override fun handleSyncPlayerHydrationStart(packet: ClientboundSyncPlayerHydrationStartPacket) {
        if (!hydratingPlayers.compareAndSet(false, true)) {
            log.atWarning()
                .log("Tried to start player hydration twice")
            return
        }
    }

    override fun handleSyncPlayerHydrationChunk(packet: ClientboundSyncPlayerHydrationChunkPacket) {
        if (!hydratingPlayers.get()) {
            log.atWarning()
                .log("Received player hydration chunk before start")
            return
        }

        pendingHydrationPlayers.addAll(packet.entries)
    }

    override fun handleSyncPlayerHydrationEnd(packet: ClientboundSyncPlayerHydrationEndPacket) {
        if (!hydratingPlayers.compareAndSet(true, false)) {
            log.atWarning()
                .log("Tried to end player hydration twice")
            return
        }

        for (data in pendingHydrationPlayers) {
            val player = commonPlayerManagerImpl.createExistingPlayer(
                data.uuid,
                data.name,
                data.playerIp,
                data.serverName,
                data.proxyName
            )

            data.pdcOrCallback.ifLeft { tag ->
                player.overwritePpdc(tag)
            }.ifRight { callback ->
                pendingLargePpdcs.add(callback)
            }
        }

        pendingHydrationPlayers.clear()
    }

    override fun handleSyncLargerPlayerPersistentDataContainerStart(packet: ClientboundSyncLargePlayerPersistentDataContainerStartPacket) {
        if (currentLargePpdcUuid != null) {
            log.atWarning()
                .log("Received start of large PPD container before end of previous one (%s)", currentLargePpdcUuid)
            return
        }

        currentLargePpdcUuid = packet.playerUuid
        currentLargePpdc = null
    }

    override fun handleSyncLargerPlayerPersistentDataContainerChunk(packet: ClientboundSyncLargePlayerPersistentDataContainerChunkPacket) {
        if (currentLargePpdcUuid == null) {
            log.atWarning()
                .log("Received chunk of large PPD container before start")
            return
        }

        val existing = currentLargePpdc
        val payload = packet.payload

        currentLargePpdc = if (existing == null) {
            payload
        } else {
            existing + payload
        }
    }

    override fun handleSyncLargerPlayerPersistentDataContainerEnd(packet: ClientboundSyncLargePlayerPersistentDataContainerEndPacket) {
        val uuid = currentLargePpdcUuid
        val payload = currentLargePpdc
        if (uuid == null || payload == null) {
            log.atWarning()
                .log("Received end of large PPD container before start")
            return
        }

        currentLargePpdcUuid = null
        currentLargePpdc = null

        pendingLargePpdcs.remove(uuid)
        val player = commonPlayerManagerImpl.getPlayer(uuid)
        if (player == null) {
            log.atWarning()
                .log("Received large PPD container end for unknown player (%s)", uuid)
            return
        }

        val tag = payload.inputStream().use { stream ->
            BinaryTagIO.reader().read(stream)
        }

        player.overwritePpdc(tag)
    }

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val info = NettyPacketInfo(connection, ConnectionProtocol.SYNCHRONIZING)

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

    override fun restart() {
        platformExtension.restart()
    }

    override fun shutdown() {
        platformExtension.shutdown()
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }
}