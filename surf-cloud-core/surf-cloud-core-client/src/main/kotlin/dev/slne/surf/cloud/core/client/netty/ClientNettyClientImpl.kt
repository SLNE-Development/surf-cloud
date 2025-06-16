package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.server.CloudServerConstants
import dev.slne.surf.cloud.core.client.config.clientConfig
import dev.slne.surf.cloud.core.client.netty.network.ClientHandshakePacketListenerImpl
import dev.slne.surf.cloud.core.client.netty.network.ClientRunningPacketListenerImpl
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.client.netty.network.StatusUpdate
import dev.slne.surf.cloud.core.client.server.serverManagerImpl
import dev.slne.surf.cloud.core.common.config.cloudConfig
import dev.slne.surf.cloud.core.common.coroutines.ConnectionTickScope
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.netty.CommonNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.EncryptionManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ClientInitializePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ClientboundInitializeIdResponsePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ServerboundInitializeRequestIdPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.LoginProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginStartPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundBroadcastPacket
import dev.slne.surf.cloud.core.common.util.InetSocketAddress
import dev.slne.surf.cloud.core.common.util.ServerAddress
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ClientNettyClientImpl(
    val proxy: Boolean,
    val platformExtension: PlatformSpecificPacketListenerExtension,
) : CommonNettyClientImpl(
    CloudPersistentData.SERVER_ID,
    CloudProperties.SERVER_CATEGORY,
    CloudProperties.SERVER_NAME
) {
    private val log = logger()

    private var _listener: ClientRunningPacketListenerImpl? = null
    val listener get() = _listener ?: error("listener not yet set")
    val connected get() = _listener?.connection?.connected ?: false

    val preRunningCallback = CompletableDeferred<Unit>()
    val synchronizeCallback = CompletableDeferred<Unit>()
    lateinit var startSynchronizeTask: suspend () -> Unit

    override val playAddress: InetSocketAddress
        get() = platformExtension.playAddress

    private val statusUpdate: StatusUpdate = {
        log.atInfo().log(it)
    }

    /**
     * Bootstraps the client. Setup the connection protocol until the PreRunning state.
     */
    suspend fun bootstrap() {
        val config = cloudConfig.connectionConfig.nettyConfig
        connectToServer(ServerAddress(config.host, config.port))
        preRunningCallback.await() // Wait until the connection is in the PreRunning state
    }


    suspend fun stop() {
        doShutdown()
    }

    suspend fun connectToServer(serverAddress: ServerAddress) {
        fetchAndUpdateData(InetSocketAddress(serverAddress.host, serverAddress.port))

        log.atInfo()
            .log("Connecting to server at ${serverAddress.host}:${serverAddress.port}")

        try {
            val inetSocketAddress = InetSocketAddress(serverAddress)
            val connection = ConnectionImpl(PacketFlow.CLIENTBOUND, EncryptionManager.instance)
            ConnectionImpl.connect(
                inetSocketAddress,
                cloudConfig.connectionConfig.nettyConfig.useEpoll,
                connection
            )

            ConnectionTickScope.launch {
                while (connection.connected && isActive) {
                    connection.tick()
                    delay(1.seconds)
                }

                connection.handleDisconnection()
            }

            connection.initiateServerboundRunningConnection(
                inetSocketAddress.hostName,
                inetSocketAddress.port,
                LoginProtocols.SERVERBOUND,
                LoginProtocols.CLIENTBOUND,
                ClientHandshakePacketListenerImpl(
                    this,
                    connection,
                    platformExtension,
                    statusUpdate,
                ),
                false
            )
            CloudProperties
            connection.send(
                ServerboundLoginStartPacket(
                    serverId,
                    serverCategory,
                    serverName,
                    proxy,
                    clientConfig.isLobby,
                    playAddress
                )
            )
        } catch (e: Exception) {
            val cause = e.cause as? Exception ?: e

            throw FatalSurfError {
                simpleErrorMessage("Couldn't connect to server")
                detailedErrorMessage("An error occurred while trying to connect to the server.")
                cause(cause)
                exitCode(ExitCodes.CLIENT_COULD_NOT_CONNECT_TO_SERVER)
                additionalInformation("Server address: ${serverAddress.host}:${serverAddress.port}")
                additionalInformation("Server ID: $serverId")
                additionalInformation("Server category: $serverCategory")
                possibleSolution("Check if the server is online and reachable.")
                possibleSolution("Check if the server address is correct.")
            }
        }
    }

    private suspend fun fetchAndUpdateData(address: InetSocketAddress) {
        if (serverId == CloudPersistentData.SERVER_ID_NOT_SET) {
            val connection = connectToServer(address, false)
            log.atInfo()
                .log("Generating server ID...")
            val responseId = CompletableDeferred<Long>()

            val listener = object : ClientInitializePacketListener {
                override fun handleIdResponse(packet: ClientboundInitializeIdResponsePacket) {
                    responseId.complete(packet.generatedId)
                }

                override suspend fun onDisconnect(details: DisconnectionDetails) {
                    if (!responseId.isCompleted) {
                        responseId.completeExceptionally(IllegalStateException("The connection was closed before the server ID could be fetched."))
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
                internalServerId = responseId.await()
                CloudPersistentData.SERVER_ID = (internalServerId)
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

    fun initListener(listener: ClientRunningPacketListenerImpl) {
        _listener = listener
    }

    override fun broadcast(packets: List<NettyPacket>) {
        require(packets.none { it is RespondingNettyPacket<*> }) { "Cannot broadcast responding packets." }
        val finalPackets = packets.toMutableList()
        finalPackets.add(0, ServerboundBroadcastPacket)

        listener.connection.send(ServerboundBundlePacket(finalPackets))
    }

    private suspend fun connectToServer(
        address: InetSocketAddress,
        useEpoll: Boolean,
    ): ConnectionImpl {
        val connection = ConnectionImpl(PacketFlow.CLIENTBOUND, EncryptionManager.instance)
        ConnectionImpl.connect(address, useEpoll, connection)
        return connection
    }

    suspend fun doShutdown() {
        val server = serverManagerImpl.retrieveServerById(serverId)

        try {
            withTimeout(1.minutes) { server?.sendAll(CloudServerConstants.LOBBY_CATEGORY) }
        } catch (e: TimeoutCancellationException) {
            log.atWarning()
                .withCause(e)
                .log("Failed to transfer all players from server ${server?.name} to the lobby. Proceeding with shutdown anyway.")
        }

        listener.close()
        connection.disconnect(DisconnectReason.CLIENT_SHUTDOWN)
    }
}