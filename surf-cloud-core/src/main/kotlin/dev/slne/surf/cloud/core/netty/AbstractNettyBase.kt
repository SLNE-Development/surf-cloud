package dev.slne.surf.cloud.core.netty

import dev.slne.surf.cloud.api.exceptions.ExitCodes
import dev.slne.surf.cloud.api.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.netty.NettyBase
import dev.slne.surf.cloud.api.netty.exception.SurfNettyPacketNotRegisteredException
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.source.NettySource
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.config.cloudConfig
import dev.slne.surf.cloud.core.coroutines.NettyListenerScope
import dev.slne.surf.cloud.core.netty.common.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry
import dev.slne.surf.cloud.core.netty.protocol.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.netty.temp.AbstractNettyConnection
import dev.slne.surf.cloud.core.spring.event.RootSpringContextInitialized
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.context.event.EventListener
import java.util.concurrent.TimeUnit


abstract class AbstractNettyBase<SELF : AbstractNettyBase<SELF, Connection, Client>, Connection : AbstractNettyConnection<Connection, Client, SELF>, Client : ProxiedNettySource<Client>>(
    override val name: String
) : NettyBase<Client> {
    private val log = logger()

    override val connection by lazy { createConnectionInstance() }
    override val host get() = cloudConfig.connectionConfig.nettyConfig.host
    override val port get() = cloudConfig.connectionConfig.nettyConfig.port

    protected abstract fun createConnectionInstance(): Connection

    @EventListener
    suspend fun handleContextRefreshedEvent(ignored: RootSpringContextInitialized?) {
        try {
            runBlocking {
                connection.tryEstablishConnection()
            }
        } catch (e: Exception) {
            throw FatalSurfError {
                simpleErrorMessage("Failed to establish connection to the server.")
                detailedErrorMessage("An error occurred while trying to connect to $host:$port.")
                cause(e)
                additionalInformation("Check if the server is online and reachable.")
                additionalInformation("Ensure the network configuration is correct.")
                possibleSolution("Verify the host and port settings.")
                possibleSolution("Check firewall settings or network issues.")
                exitCode(ExitCodes.CONNECTION_FAILURE)
            }
        }
    }

    @PreDestroy
    fun close() = runBlocking {
        connection.close()
    }

    fun checkPacket(packet: NettyPacket<*>) {
        if (!NettyPacketRegistry.isRegistered(packet)) {
            throw SurfNettyPacketNotRegisteredException("Packet ${packet::class.simpleName} is not registered")
        }
    }

    //    @Deprecated(
//        "Use NettyPacketRegistry.createPacket instead",
//        ReplaceWith("NettyPacketRegistry.createPacket(packetId)")
//    )
    fun <T : NettyPacket<T>> createPacket(packetId: Int): T? =
        NettyPacketRegistry.createPacket<T>(packetId)

    fun onPacketReceived(
        packet: NettyPacket<*>,
        source: NettySource<*>,
        proxiedSource: ProxiedNettySource<*>? = null
    ) {
        try {
            val listeners = NettyListenerRegistry.getListeners(packet::class.java) ?: return
            val info = NettyPacketInfo(source, proxiedSource)

            for (listener in listeners) {
                NettyListenerScope.launch {
                    try {
                        listener.handle(packet, info)
                    } catch (e: Exception) {
                        log.atWarning()
                            .withCause(e)
                            .atMostEvery(5, TimeUnit.SECONDS)
                            .log(
                                "Failed to call listener %s for packet %s",
                                listener::class.simpleName,
                                packet::class.simpleName
                            )
                    }
                }
            }
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .atMostEvery(5, TimeUnit.SECONDS)
                .log("Failed to call listeners for packet %s", packet::class.simpleName)
        }
    }
}
