package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.server.CloudServerConstants
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.client.netty.network.StatusUpdate
import dev.slne.surf.cloud.core.client.netty.state.*
import dev.slne.surf.cloud.core.client.server.serverManagerImpl
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.netty.CommonNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundBroadcastPacket
import dev.slne.surf.cloud.core.common.util.SyncSignal
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.net.InetSocketAddress
import kotlin.time.Duration.Companion.minutes

class ClientNettyClientImpl(
    val proxy: Boolean,
    val platformExtension: PlatformSpecificPacketListenerExtension,
    private val configHolder: AbstractSurfCloudConfigHolder<*>,
    private val manager: NettyCommonClientManager,
    private val reconnectBackoff: ReconnectBackoff
) : CommonNettyClientImpl(
    CloudProperties.SERVER_CATEGORY,
    CloudProperties.SERVER_NAME
), ConnectionStateChangeListener {
    private val log = logger()

    val connectionManager = createConnectionManager()

    val preRunningFinishedSignal = SyncSignal()
    var startSynchronizeTask: (suspend () -> Unit) = {}
    val enteredSynchronizingStateSignal = SyncSignal()
    val synchronizeFinishedSignal = SyncSignal()

    override val playAddress: InetSocketAddress
        get() = platformExtension.playAddress

    val statusUpdate: StatusUpdate = {
        log.atInfo().log(it)
    }

    override var velocitySecret = ByteArray(0)

    private fun createConnectionManager(): ConnectionManager {
        val config = configHolder.config.connectionConfig.nettyConfig
        val manager = ConnectionManager(
            address = InetSocketAddress(config.host, config.port),
            useEpoll = config.useEpoll,
            client = this,
            configHolder = configHolder,
            reconnectBackoff = reconnectBackoff
        )

        manager.addListener(this)

        return manager
    }

    /**
     * Bootstraps the client. Setup the connection protocol until the PreRunning state.
     */
    suspend fun bootstrap() {
        val start = connectionManager.start(true)
            ?: throw IllegalStateException("Failed to bootstrap the client.")

        start.syncUninterruptibly()
        preRunningFinishedSignal.awaitNext() // Wait until the connection is in the PreRunning state
    }

    suspend fun stop() {
        velocitySecret.fill(0)
        doShutdown()
    }

    // <editor-fold desc="fetchAndUpdateData">
    /* // Currently not used — kept for reference for future use
    private suspend fun fetchAndUpdateData(address: InetSocketAddress) {
        if (false) {
            suspend fun connectToServer(
                address: InetSocketAddress,
                useEpoll: Boolean,
            ): ConnectionImpl {
                val connection = ConnectionImpl(PacketFlow.CLIENTBOUND, EncryptionManager.instance)
                ConnectionImpl.connect(address, useEpoll, connection, configHolder)
                return connection
            }

            val connection = connectToServer(address, false)
            log.atInfo()
                .log("Updating data...")
            val data = CompletableDeferred<Any>()

            val listener = object : ClientInitializePacketListener {
                override suspend fun onDisconnect(details: DisconnectionDetails) {
                    if (!data.isCompleted) {
                        data.completeExceptionally(IllegalStateException("The connection was closed before the data could be fetched."))
                    }
                }

                override fun isAcceptingMessages(): Boolean {
                    return connection.connected
                }
            }

            ConnectionTickScope.launch {
                while (connection.connected) {
                    connection.tick()
                    delay(1.seconds)
                }

                connection.handleDisconnection()
            }

            try {
                connection.initiateServerboundInitializeConnection(
                    address.hostName,
                    address.port,
                    listener
                )
                connection.sendWithIndication(ServerboundInitializeRequestIdPacket)
                connection.disconnect(DisconnectionDetails(DisconnectReason.SERVER_ID_FETCHED))
            } catch (e: Throwable) {
                throw FatalSurfError {
                    simpleErrorMessage("Couldn't fetch server ID")
                    detailedErrorMessage("An error occurred while trying to fetch the server ID.")
                    cause(e)
                    exitCode(ExitCodes.CLIENT_COULD_NOT_FETCH_SERVER_ID)
                    additionalInformation("Server address: ${address.hostName}:${address.port}")
                    possibleSolution("Check if the server is online and reachable.")
                    possibleSolution("Check if the server address is correct.")
                }
            }
        }
    }
     */
    // </editor-fold>

    override fun broadcast(packets: List<NettyPacket>) {
        require(packets.none { it is RespondingNettyPacket<*> }) { "Cannot broadcast responding packets." }
        require(packets.all { it.protocols.contains(ConnectionProtocol.RUNNING) }) { "Cannot broadcast packets that are not in the Running state." }

        val finalPackets = packets.toMutableList()
        finalPackets.add(0, ServerboundBroadcastPacket)

        connectionManager.sendOrQueue(
            ServerboundBundlePacket(finalPackets),
            ConnectionProtocol.RUNNING,
            false
        )
    }

    suspend fun doShutdown() {
        val server = serverManagerImpl.retrieveServerByName(serverName)

        try {
            withTimeout(1.minutes) { server?.sendAll(CloudServerConstants.LOBBY_CATEGORY) }
        } catch (e: TimeoutCancellationException) {
            log.atWarning()
                .withCause(e)
                .log("Failed to transfer all players from server ${server?.name} to the lobby. Proceeding with shutdown anyway.")
        }

        connectionManager.shutdown()
    }

    override fun onConnectionStateChanged(event: ConnectionEvent) {
        val to = event.to
        if (to == StateMachine.State.DEGRADED || to == StateMachine.State.DISCONNECTED) {
            manager.blockPlayerConnections()
        } else if (to == StateMachine.State.CONNECTED) {
            manager.unblockPlayerConnections()
        }
    }
}