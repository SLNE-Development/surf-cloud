package dev.slne.surf.cloud.standalone.netty.server.connection

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.util.*
import dev.slne.surf.cloud.core.common.config.cloudConfig
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.HandlerNames
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundBundlePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import dev.slne.surf.cloud.standalone.netty.server.network.ServerHandshakePacketListenerImpl
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerDomainSocketChannel
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.unix.DomainSocketAddress
import io.netty.handler.flush.FlushConsolidationHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentLinkedQueue

class ServerConnectionListener(val server: NettyServerImpl) {

    private val logIps by lazy { cloudConfig.logging.logIps }

    @Volatile
    var running = false
        private set

    private val channels = mutableObjectListOf<ChannelFuture>().synchronize()
    private val channelsMutex = Mutex()

    val connections = mutableObjectListOf<ConnectionImpl>().synchronize()
    val connectionsMutex = Mutex()

    private val pending = ConcurrentLinkedQueue<ConnectionImpl>()

    init {
        running = true
    }


    suspend fun startTcpServerListener(address: InetAddress?, port: Int) {
        bind(InetSocketAddress(address, port))
    }

    suspend fun bind(address: SocketAddress) = withContext(Dispatchers.IO) {
        channelsMutex.withLock {
            val channelClass: Class<out ServerChannel>
            val eventloopgroup: EventLoopGroup

            if (Epoll.isAvailable() && cloudConfig.connectionConfig.nettyConfig.useEpoll) {
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
                        override fun handlerAdded(ctx: ChannelHandlerContext?) {
                            super.handlerAdded(ctx)

                            println("Handler added to channel ${ctx?.channel()?.id()?.asLongText()}")
                        }

                        override fun handlerRemoved(ctx: ChannelHandlerContext?) {
                            super.handlerRemoved(ctx)

                            println("Handler removed to channel ${ctx?.channel()?.id()?.asLongText()}")
                        }

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
                                ConnectionImpl(PacketFlow.SERVERBOUND) // TODO: rate limit

                            pending.add(connection)
                            connection.configurePacketHandler(channel, pipeline)
                            println(
                                "Configured packet handler for ${
                                    connection.getLoggableAddress(
                                        logIps
                                    )
                                }"
                            )
                            connection.setListenerForServerboundHandshake(
                                ServerHandshakePacketListenerImpl(server, connection)
                            )
                        }
                    })
                    .bind()
                    .suspend()
            )
        }
    }

    suspend fun acceptConnections() {
        channelsMutex.withLock {
            for (future in channels) {
                future.channel().config().isAutoRead = true
            }
        }
    }

    suspend fun stop() {
        this.running = false

        channelsMutex.withLock {
            for (future in channels) {
                try {
                    future.channel().close().suspend()
                } catch (e: InterruptedException) {
                    log.atSevere().withCause(e).log("Interrupted whilst closing channel")
                }
            }
        }
    }

    private suspend fun addPending() {
        var connection: ConnectionImpl
        while ((pending.poll().also { connection = it }) != null) {
            connectionsMutex.withLock {
                connections.add(connection)
                connection.isPending = false
            }
        }
    }

    suspend fun tick() {
        addPending()

        connectionsMutex.withLock {
            val iterator = connections.iterator()
            for (connection in iterator) {
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
                            val details = DisconnectionDetails("Internal server error")
                            connection.sendWithIndication(ClientboundDisconnectPacket(details))
                            connection.disconnect(details)
                        }

                        connection.setReadOnly()
                    }
                } else {
                    if (connection.preparing) {
                        continue
                    }

                    iterator.remove()
                    connection.handleDisconnection()
                }
            }
        }
    }

    fun broadcast(packet: NettyPacket, flush: Boolean = true) {
        val activeProtocols = packet.protocols
        for (connection in connections) {
            if (connection.outboundProtocolInfo.id !in activeProtocols) continue
            connection.send(packet, flush)
        }
    }

    fun broadcast(packets: List<NettyPacket>, flush: Boolean = true) {
        if (packets.isEmpty()) return
        val packet = if (packets.size == 1) packets.first() else ClientboundBundlePacket(packets)
        val activeProtocols = packet.protocols

        for (connection in connections) {
            if (connection.outboundProtocolInfo.id !in activeProtocols) continue
            connection.send(packet, flush)
        }
    }

    companion object {
        private val log = logger()

        val SERVER_EVENT_GROUP by lazy {
            NioEventLoopGroup(
                threadFactory {
                    nameFormat("Netty Server IO #%d")
                    daemon(true)
                    uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                }
            )
        }

        val SERVER_EPOLL_EVENT_GROUP by lazy {
            EpollEventLoopGroup(
                threadFactory {
                    nameFormat("Netty Epoll Server IO #%d")
                    daemon(true)
                    uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                }
            )
        }
    }
}