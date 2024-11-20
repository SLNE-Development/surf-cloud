package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.coroutines.NettyConnectionScope
import dev.slne.surf.cloud.core.common.coroutines.NettyListenerScope
import dev.slne.surf.cloud.core.common.netty.network.CommonTickablePacketListener
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.protocol.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


private val KEEP_ALIVE_TIME = KeepAliveTime(15.seconds)
private val KEEP_ALIVE_TIMEOUT = KeepAliveTime(30.seconds)

class ServerRunningPacketListenerImpl(
    val server: NettyServerImpl,
    val client: ServerClientImpl,
    val connection: ConnectionImpl
) :
    CommonTickablePacketListener(), RunningServerPacketListener {
    private val log = logger()

    private var keepAliveTime = KeepAliveTime.now()
    private var keepAlivePending = false
    private var keepAliveChallenge: KeepAliveTime = KeepAliveTime(0)
    private var processedDisconnect = false
    private var closedListenerTime: Long = 0
    var latency = 0
        private set

    @Volatile
    private var suspendFlushingOnServerThread = false

    private var closed = false

    override fun handleBundlePacket(packet: ServerboundBundlePacket) {
        val broadcastIdentifier = packet.subPackets.firstOrNull() as? ServerboundBroadcastPacket

        if (broadcastIdentifier != null) {
            handleBroadcastPacket(packet.subPackets.drop(1))
            return
        }

        NettyConnectionScope.launch {
            for (subPacket in packet.subPackets) {
                connection.handlePacket(subPacket)
            }
        }
    }

    private fun handleBroadcastPacket(packets: List<NettyPacket>) {
        if (packets.isEmpty()) return
        val packet = if (packets.size == 1) packets.first() else ClientboundBundlePacket(packets)
        broadcast(packet)
    }

    override suspend fun handleKeepAlivePacket(packet: ServerboundKeepAlivePacket) {
        if (keepAlivePending && packet.keepAliveId == keepAliveChallenge.time) {
            val elapsedTime = KeepAliveTime.now() - keepAliveTime

            this.latency = ((latency * 3 + elapsedTime) / 4).toInt()
            this.keepAlivePending = false
        } else {
            disconnect("Invalid keep alive")
        }
    }

    fun handlePongPacket(server: ServerboundPongPacket) {
        TODO("Not yet implemented")
    }

    override fun handlePingRequest(packet: ServerboundPingRequestPacket) {
        send(ClientboundPongResponsePacket(packet.time))
    }

    override fun handlePlayerConnectToServer(packet: PlayerConnectToServerPacket) {
        playerManagerImpl.updateOrCreatePlayer(packet.uuid, packet.serverUid, packet.proxy)
        broadcast(PlayerConnectToServerPacket(packet.uuid, packet.serverUid, packet.proxy))
    }

    override fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket) {
        playerManagerImpl.updateOrRemoveOnDisconnect(packet.uuid, packet.serverUid, packet.proxy)
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

    override fun handleSendTitlePart(packet: ServerboundSendTitlePartPacket) {
        withPlayer(packet.uuid) {
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

    override fun handleRequestDisplayName(packet: ServerboundRequestDisplayNamePacket) {

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
            NettyListenerScope.launch {
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

    override fun onDisconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return
        processedDisconnect = true

        log.atInfo()
            .log("${client.serverCategory}/${client.serverId} (${client.connection.hostname}) lost connection: ${details.reason}")
    }

    private fun close() {
        if (!closed) {
            closedListenerTime = System.currentTimeMillis()
            closed = true
        }
    }

    private suspend fun keepConnectionAlive() {
        val currentTime = KeepAliveTime.now()
        val elapsedTime = currentTime - keepAliveTime

        if (KEEP_ALIVE_TIME.isExpired(elapsedTime)) {
            if (keepAlivePending && KEEP_ALIVE_TIMEOUT.isExpired(elapsedTime)) {
                disconnect("Timed out")
            } else if (checkIfClosed(currentTime)) {
                keepAlivePending = true
                keepAliveTime = currentTime
                keepAliveChallenge = currentTime
                send(ClientboundKeepAlivePacket(keepAliveChallenge.time))
            }
        }
    }

    private suspend fun checkIfClosed(time: KeepAliveTime): Boolean {
        if (closed) {
            if (KEEP_ALIVE_TIME.isExpired(time - closedListenerTime)) {
                disconnect("Timed out")
            }

            return false
        }

        return true
    }

    fun suspendFlushing() {
        this.suspendFlushingOnServerThread = true
    }

    fun resumeFlushing() {
        this.suspendFlushingOnServerThread = false
        connection.flushChannel()
    }

    fun send(packet: NettyPacket) {
        if (processedDisconnect) return

        if (packet.terminal) {
            close()
        }

        val flush = !suspendFlushingOnServerThread
        connection.send(packet, flush)
    }

    fun broadcast(packet: NettyPacket) {
        if (processedDisconnect) return

        if (packet.terminal) {
            close()
        }

        val flush = !suspendFlushingOnServerThread
        server.connection.broadcast(packet, flush)
    }

    suspend fun disconnect(reason: String) {
        disconnect(DisconnectionDetails(reason))
    }

    suspend fun disconnect(details: DisconnectionDetails) {
        if (processedDisconnect) return

        NettyConnectionScope.launch {
            connection.sendWithIndication(ClientboundDisconnectPacket(details))
            connection.disconnect(details)
        }

        onDisconnect(details)
        connection.setReadOnly()

        schedule { connection.handleDisconnection() }
    }

    override suspend fun tick0() {
        keepConnectionAlive()
    }

    private fun withPlayer(uuid: UUID, block: CloudPlayer.() -> Unit) {
        val player = playerManagerImpl.getPlayer(uuid) ?: return
        player.block()
    }
}

@JvmInline
value class KeepAliveTime(val time: Long) {

    fun isExpired(elapsedTime: KeepAliveTime) = elapsedTime >= this

    operator fun compareTo(other: KeepAliveTime) = time.compareTo(other.time)
    operator fun minus(other: KeepAliveTime) = KeepAliveTime(time - other.time)
    operator fun minus(other: Long) = KeepAliveTime(time - other)

    companion object {
        fun now() = KeepAliveTime(System.currentTimeMillis())
    }
}

fun KeepAliveTime(duration: Duration) = KeepAliveTime(duration.inWholeMilliseconds)

operator fun Int.plus(time: KeepAliveTime) = this + time.time