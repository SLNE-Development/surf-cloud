package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.client.netty.network.ClientHandshakePacketListenerImpl
import dev.slne.surf.cloud.core.client.netty.network.ClientRunningPacketListenerImpl
import dev.slne.surf.cloud.core.client.netty.network.StatusUpdate
import dev.slne.surf.cloud.core.common.config.cloudConfig
import dev.slne.surf.cloud.core.common.coroutines.NettyConnectionScope
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.netty.CommonNettyClientImpl
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ClientInitializePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ClientboundInitializeIdResponsePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ServerboundInitializeRequestIdPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.LoginProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginStartPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundBroadcastPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundBundlePacket
import dev.slne.surf.cloud.core.common.util.ServerAddress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import kotlin.time.Duration.Companion.seconds

class ClientNettyClientImpl(val proxy: Boolean) : CommonNettyClientImpl(
    CloudPersistentData.SERVER_ID.value(),
    CloudProperties.SERVER_CATEGORY.value() ?: CloudProperties.SERVER_CATEGORY_NOT_SET,
    CloudProperties.SERVER_NAME.value()
) {
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

    suspend fun start() {
        val config = cloudConfig.connectionConfig.nettyConfig
        connectToServer(ServerAddress(config.host, config.port))
    }

    fun stop() {
        connection.disconnect(DisconnectionDetails("Client stopped"))
    }

    suspend fun connectToServer(serverAddress: ServerAddress) {
        fetchAndUpdateData(InetSocketAddress(serverAddress.host, serverAddress.port))

        log.atInfo().log("Connecting to server at ${serverAddress.host}:${serverAddress.port}")
        val inetSocketAddress: InetSocketAddress

        try {
            inetSocketAddress = InetSocketAddress(serverAddress.host, serverAddress.port)
            val connection = ConnectionImpl(PacketFlow.CLIENTBOUND)
            ConnectionImpl.connect(
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
            connection.send(ServerboundLoginStartPacket(serverId, serverCategory, serverName, proxy))
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
            val connection = ConnectionImpl.connectToServer(address, false)
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

            NettyConnectionScope.launch {
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
                CloudPersistentData.SERVER_ID.setValue(internalServerId)
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
        val finalPackets = packets.toMutableList()
        finalPackets.add(0, ServerboundBroadcastPacket)

        listener.connection.send(ServerboundBundlePacket(finalPackets))
    }
}