package dev.slne.surf.cloud.standalone.netty.server.connection

import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.util.DefaultUncaughtExceptionHandlerWithName
import dev.slne.surf.cloud.api.common.util.netty.suspend
import dev.slne.surf.cloud.api.common.util.threadFactory
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.network.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.network.ServerHandshakePacketListenerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollServerDomainSocketChannel
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.unix.DomainSocketAddress
import io.netty.handler.flush.FlushConsolidationHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

class ServerConnectionListener(
    val server: NettyServerImpl,
    private val configHolder: AbstractSurfCloudConfigHolder<*>
) {

    private val logIps get() = configHolder.config.logging.logIps

    @Volatile
    var running = false
        private set

    private val channels = CopyOnWriteArrayList<ChannelFuture>()
    val connections = CopyOnWriteArrayList<ConnectionImpl>()
    private val pending = ConcurrentLinkedQueue<ConnectionImpl>()

    init {
        running = true
    }

    suspend fun startTcpServerListener(address: InetAddress?, port: Int) {
        bind(InetSocketAddress(address, port))
    }

    suspend fun bind(address: SocketAddress) = withContext(Dispatchers.IO) {
        val channelClass: Class<out ServerChannel>
        val eventloopgroup: EventLoopGroup

        if (Epoll.isAvailable() && configHolder.config.connectionConfig.nettyConfig.useEpoll) {
            channelClass =
                if (address is DomainSocketAddress) EpollServerDomainSocketChannel::class.java else EpollServerSocketChannel::class.java
            eventloopgroup = SERVER_EPOLL_EVENT_GROUP
            log.atInfo().log("Using epoll channel type")
        } else {
            channelClass = NioServerSocketChannel::class.java
            eventloopgroup = SERVER_EVENT_GROUP
            log.atInfo().log("Using default channel type")
        }

        channels.add(
            ServerBootstrap()
                .channel(channelClass)
                .group(eventloopgroup)
                .localAddress(address)
                .option(ChannelOption.AUTO_READ, false)
                .childHandler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(channel: Channel) {
                        runCatching {
                            channel.config().setOption(ChannelOption.TCP_NODELAY, true)
                        }

                        val pipeline = channel.pipeline()
                            .addFirst(FlushConsolidationHandler())
                            .addLast(HandlerNames.TIMEOUT, ReadTimeoutHandler(30))

                        ConnectionImpl.configureSerialization(
                            pipeline,
                            PacketFlow.SERVERBOUND,
                            false
                        )

                        val connection =
                            ConnectionImpl(PacketFlow.SERVERBOUND, EncryptionManager.instance)

                        pending.add(connection)
                        connection.configurePacketHandler(channel, pipeline)

                        val loggableAddress = connection.getLoggableAddress(logIps)
                        log.atInfo()
                            .log("Configured packet handler for $loggableAddress")

                        connection.setListenerForServerboundHandshake(
                            ServerHandshakePacketListenerImpl(server, connection)
                        )
                    }
                })
                .bind()
                .suspend()
        )
    }

    fun acceptConnections() {
        for (future in channels) {
            future.channel().config().isAutoRead = true
        }
    }

    suspend fun stop() {
        this.running = false

        suspend fun disconnect(connection: ConnectionImpl) {
            try {
                val details = DisconnectionDetails(DisconnectReason.SERVER_SHUTDOWN)
                connection.sendWithIndication(ClientboundDisconnectPacket(details))
                connection.disconnect(details)?.suspend()
            } catch (_: Throwable) {
                try {
                    connection.channel.close().suspend()
                } catch (e: InterruptedException) {
                    log.atSevere().withCause(e).log("Interrupted whilst closing channel")
                } catch (e: Exception) {
                    log.atSevere().withCause(e).log("Failed to close channel")
                }
            }
        }


        for (connection in connections) {
            disconnect(connection)
        }
        connections.clear()

        for (pending in pending) {
            disconnect(pending)
        }
        pending.clear()

        for (future in channels) {
            try {
                future.channel().close().suspend()
            } catch (e: InterruptedException) {
                log.atSevere().withCause(e).log("Interrupted whilst closing channel")
            }
        }

        ConnectionImpl.NETWORK_EPOLL_WORKER_GROUP.shutdownGracefully().suspend()
        ConnectionImpl.NETWORK_WORKER_GROUP.shutdownGracefully().suspend()
    }

    private fun addPending() {
        while (true) {
            val connection = pending.poll() ?: break
            connections.add(connection)
            connection.isPending = false
        }
    }

    suspend fun tick() {
        addPending()

        for (connection in connections) {
            if (connection.connecting) {
                continue
            }

            if (connection.connected) {
                try {
                    connection.tick()
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .log("Failed to handle packet for ${connection.getLoggableAddress(logIps)}")

                    ConnectionManagementScope.launch {
                        val details =
                            DisconnectionDetails(DisconnectReason.INTERNAL_EXCEPTION, e.message)
                        connection.sendWithIndication(ClientboundDisconnectPacket(details))
                        connection.disconnect(details)
                    }

                    connection.setReadOnly()
                }
            } else {
                if (connection.preparing) {
                    continue
                }

                connections.remove(connection)
                connection.handleDisconnection()
            }
        }
    }

    fun broadcast(packet: NettyPacket, flush: Boolean = true, except: (ConnectionImpl) -> Boolean = { false }) {
        require(packet !is RespondingNettyPacket<*>) { "Cannot broadcast responding packets" }

        val activeProtocols = packet.protocols
        for (connection in connections) {
            if (connection.outboundProtocolInfo.id !in activeProtocols) continue
            if (except(connection)) continue

            try {
                connection.send(packet, flush)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log(
                        "Failed to broadcast packet ${packet::class.simpleName} to ${
                            connection.getLoggableAddress(
                                logIps
                            )
                        }"
                    )
            }
        }
    }

    fun broadcast(packets: List<NettyPacket>, flush: Boolean = true) {
        if (packets.isEmpty()) return
        require(packets.none { it is RespondingNettyPacket<*> }) { "Cannot broadcast responding packets" }

        val packet = if (packets.size == 1) packets.first() else ClientboundBundlePacket(packets)
        val activeProtocols = packet.protocols

        for (connection in connections) {
            if (connection.outboundProtocolInfo.id !in activeProtocols) continue

            try {
                connection.send(packet, flush)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log(
                        "Failed to broadcast packet ${packet::class.simpleName} to ${
                            connection.getLoggableAddress(
                                logIps
                            )
                        }"
                    )
            }
        }
    }

    companion object {
        private val log = logger()

        val SERVER_EVENT_GROUP by lazy {
            MultiThreadIoEventLoopGroup(threadFactory {
                nameFormat("Netty Server IO #%d")
                daemon(true)
                uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
            }, NioIoHandler.newFactory())
        }

        val SERVER_EPOLL_EVENT_GROUP by lazy {
            MultiThreadIoEventLoopGroup(
                threadFactory {
                    nameFormat("Netty Epoll Server IO #%d")
                    daemon(true)
                    uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                }, EpollIoHandler.newFactory()
            )
        }
    }
}