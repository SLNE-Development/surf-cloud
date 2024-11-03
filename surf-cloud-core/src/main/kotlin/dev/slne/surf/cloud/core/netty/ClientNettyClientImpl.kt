package dev.slne.surf.cloud.core.netty

import dev.slne.surf.cloud.api.exceptions.ExitCodes
import dev.slne.surf.cloud.api.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.config.cloudConfig
import dev.slne.surf.cloud.core.data.CloudPersistentData
import dev.slne.surf.cloud.core.netty.client.network.ClientHandshakePacketListenerImpl
import dev.slne.surf.cloud.core.netty.client.network.ClientRunningPacketListenerImpl
import dev.slne.surf.cloud.core.netty.client.network.StatusUpdate
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.netty.network.protocol.initialize.ClientInitializePacketListener
import dev.slne.surf.cloud.core.netty.network.protocol.initialize.ClientboundInitializeIdResponsePacket
import dev.slne.surf.cloud.core.netty.network.protocol.initialize.ServerboundInitializeRequestIdPacket
import dev.slne.surf.cloud.core.netty.network.protocol.login.LoginProtocols
import dev.slne.surf.cloud.core.netty.network.protocol.login.ServerboundLoginStartPacket
import dev.slne.surf.cloud.core.util.ServerAddress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import kotlin.time.Duration.Companion.seconds

class ClientNettyClientImpl(
    serverId: Long = CloudPersistentData.SERVER_ID_NOT_SET,
    serverCategory: String
) :
    CommonNettyClientImpl(serverId, serverCategory) {
    private val log = logger()

    private var _listener: ClientRunningPacketListenerImpl? = null
        set(value) {
            field = value
            if (value != null) {
                initConnection(value.connection)
            }
        }

    val listener get() = _listener ?: error("listener not yet set")


    private val statusUpdate: StatusUpdate = {
        log.atInfo().log(it)
    }

    suspend fun connectToServer(serverAddress: ServerAddress) {
        fetchAndUpdateData(InetSocketAddress(serverAddress.host, serverAddress.port))

        log.atInfo().log("Connecting to server at ${serverAddress.host}:${serverAddress.port}")
        val inetSocketAddress: InetSocketAddress

        try {
            inetSocketAddress = InetSocketAddress(serverAddress.host, serverAddress.port)
            val connection = Connection(PacketFlow.CLIENTBOUND)
            Connection.connect(
                inetSocketAddress,
                cloudConfig.connectionConfig.nettyConfig.useEpoll,
                connection
            )

            connection.initiateServerboundRunningConnection(
                inetSocketAddress.hostName,
                inetSocketAddress.port,
                LoginProtocols.SERVERBOUND,
                LoginProtocols.CLIENTBOUND,
                ClientHandshakePacketListenerImpl(this, connection, statusUpdate),
                false
            )
            connection.send(ServerboundLoginStartPacket(serverId, serverCategory))
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
            val connection = Connection.connectToServer(address, false)
            log.atInfo().log("Generating server ID...")
            val responseId = CompletableDeferred<Long>()

            val listener = object : ClientInitializePacketListener {
                override fun handleIdResponse(packet: ClientboundInitializeIdResponsePacket) {
                    responseId.complete(packet.generatedId)
                }

                override fun onDisconnect(details: DisconnectionDetails) {
                    if (!responseId.isCompleted) {
                        responseId.completeExceptionally(IllegalStateException("The connection was closed before the server ID could be fetched."))
                    }
                }
            }

            coroutineScope {
                launch {
                    while (connection.connected) {
                        connection.tick()
                        delay(1.seconds)
                    }

                    connection.handleDisconnection()
                }
            }

            try {
                connection.initiateServerboundInitializeConnection(
                    address.hostName,
                    address.port,
                    listener
                )
                connection.send(ServerboundInitializeRequestIdPacket)
                internalServerId = responseId.await()
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
}