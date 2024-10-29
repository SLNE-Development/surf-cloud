package dev.slne.surf.cloud.standalone.netty.server.connection

import com.google.common.util.concurrent.ThreadFactoryBuilder
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.util.*
import dev.slne.surf.cloud.core.config.cloudConfig
import dev.slne.surf.cloud.core.netty.network.Connection
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
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.Volatile

class ServerConnectionListener(val server: NettyServerImpl) {

    private val logIps by lazy { cloudConfig.logging.logIps }

    @Volatile
    var running = false
        private set

    private val channels = mutableObjectListOf<ChannelFuture>().synchronize()
    val connections = mutableObjectListOf<Connection>().synchronize()
    private val pending = ConcurrentLinkedQueue<Connection>()

    init {
        running = true
    }


    suspend fun startTcpServerListener(address: InetAddress?, port: Int) {
        bind(InetSocketAddress(address, port))
    }

    suspend fun bind(address: SocketAddress) = withContext(Dispatchers.IO) {
        val channelClass: Class<out ServerChannel>
        val eventloopgroup: EventLoopGroup

        if (Epoll.isAvailable()) {
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

                        val pipeline = channel.pipeline().addFirst(FlushConsolidationHandler())
                            .addLast("timeout", ReadTimeoutHandler(30))

                        Connection.configureSerialization(
                            pipeline,
                            PacketFlow.SERVERBOUND,
                            false
                        )

                        val connection = Connection(PacketFlow.SERVERBOUND) // TODO: rate limit

                        pending.add(connection)
                        connection.configurePacketHandler(pipeline)
                        connection.setListenerForServerboundHandshake(
                            ServerHandshakePacketListenerImpl(connection)
                        )
                    }
                })
                .bind()
                .suspend()
        )
    }

    fun acceptConnections() {
        synchronized(channels) {
            for (future in channels) {
                future.channel().config().setAutoRead(true)
            }
        }
    }

    suspend fun stop() {
        this.running = false

        for (future in channels) {
            try {
                future.channel().close().suspend()
            } catch (e: InterruptedException) {
                log.atSevere().withCause(e).log("Interrupted whilst closing channel")
            }
        }
    }

    private fun addPending() {
        var connection: Connection
        while ((pending.poll().also { connection = it }) != null) {
            connections.add(connection)
            connection.isPending = false
        }
    }

    suspend fun tick() {
        addPending()

        val iterator = connections.iterator()

        for (connection in iterator) {
            if (connection.connecting) continue

            if (connection.connected) {
                try {
                    connection.tick()
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .log("Failed to handle packet for ${connection.getLoggableAddress(logIps)}")
                    connection.disconnect("Internal server error")
                    connection.setReadOnly()
                }
            } else {
                if (connection.preparing) continue

                iterator.remove()
                connection.handleDisconnection()
            }
        }
    }

    companion object {
        private val log = logger()

        val SERVER_EVENT_GROUP by lazy {
            NioEventLoopGroup(
                ThreadFactoryBuilder()
                    .setNameFormat("Netty Server IO #%d")
                    .setDaemon(true)
                    .setUncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                    .build()
            )
        }

        val SERVER_EPOLL_EVENT_GROUP by lazy {
            EpollEventLoopGroup(
                ThreadFactoryBuilder()
                    .setNameFormat("Netty Epoll Server IO #%d")
                    .setDaemon(true)
                    .setUncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                    .build()
            )
        }
    }
}