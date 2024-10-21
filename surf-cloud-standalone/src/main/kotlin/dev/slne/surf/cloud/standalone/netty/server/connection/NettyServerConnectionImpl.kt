package dev.slne.surf.cloud.standalone.netty.server.connection

import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.netty.connection.NettyServerConnection
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.api.util.suspend
import dev.slne.surf.cloud.core.coreCloudInstance
import dev.slne.surf.cloud.core.netty.client.source.NettyClientSourceImpl
import dev.slne.surf.cloud.core.netty.common.connection.AbstractNettyConnection
import dev.slne.surf.cloud.core.netty.common.source.AbstractNettySource
import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier
import dev.slne.surf.cloud.core.netty.protocol.channel.SurfNettyChannelInitializer
import dev.slne.surf.cloud.standalone.netty.server.SurfNettyServer
import dev.slne.surf.cloud.standalone.netty.server.source.tracker.ServerNettyClientTrackerImpl
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

class NettyServerConnectionImpl(server: SurfNettyServer) :
    AbstractNettyConnection<NettyServerConnectionImpl, NettyClientSource, SurfNettyServer>(server),
    NettyServerConnection {

    private val log = logger()

    private val port = server.port
    override val clientTracker = ServerNettyClientTrackerImpl(server)

    private val bossGroup = NioEventLoopGroup()
    private val workerGroup = NioEventLoopGroup()

    override lateinit var channel: ServerChannel

    override val connected: Boolean
        get() = channel.isActive

    override suspend fun tryEstablishConnection0() {
        log.atInfo()
            .log("Starting server on port %d", port)

        ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(coreCloudInstance.dataContext.getBean(SurfNettyChannelInitializer::class.java))
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .bind(port)
            .addListener(ChannelFutureListener { listener ->
                check(listener.isSuccess) { "Failed to bind to port $port because ${listener.cause()}" }
                this.channel = listener.channel() as ServerChannel

                log.atInfo()
                    .log("Server started on port %d", port)
            })
            .suspend()
    }

    override suspend fun close() {
        channel.close().suspend()
        bossGroup.shutdownGracefully().suspend()
        workerGroup.shutdownGracefully().suspend()
    }
    override fun source(channel: Channel) = clientTracker.client(channel)

    override fun broadcast(packet: NettyPacket<*>) {
        for (client in clientTracker.clients) {
            sendPacket(client, packet)
        }
    }

    override fun sendPacket0(
        source: AbstractNettySource<NettyClientSource>,
        packet: NettyPacket<*>
    ) {
        if (source !is NettyClientSourceImpl) {
            log.atWarning()
                .withStackTrace(StackSize.MEDIUM)
                .atMostEvery(10, TimeUnit.SECONDS)
                .log("Source %s is not a client", source)
            return
        }

        source.channel.writeAndFlush(packet)
    }

    @Component
    @Profile("independent")
    class NettyServerChannelInitializerModifier : ChannelInitializerModifier {
        @Throws(Exception::class)
        override fun modify(ch: SocketChannel) {
            System.err.println("MODIFYING CHANNEL")
            //      ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));
        }
    }
}
