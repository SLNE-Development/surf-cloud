package dev.slne.surf.cloud.core.netty.client.connection

import dev.slne.surf.cloud.api.config.properties.CloudProperties
import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.api.netty.connection.NettyClientConnection
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.source.NettyServerSource
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.api.util.suspend
import dev.slne.surf.cloud.core.coreCloudInstance
import dev.slne.surf.cloud.core.data.CloudPersistentData
import dev.slne.surf.cloud.core.netty.client.SurfNettyClient
import dev.slne.surf.cloud.core.netty.common.source.AbstractNettySource
import dev.slne.surf.cloud.core.netty.common.source.NettyServerSourceImpl
import dev.slne.surf.cloud.core.netty.common.source.tracker.NettyClientTrackerImpl
import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier
import dev.slne.surf.cloud.core.netty.protocol.channel.SurfNettyChannelInitializer
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudRegisterServerPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration.CloudServerRegistrationData
import dev.slne.surf.cloud.core.netty.temp.AbstractNettyConnection
import dev.slne.surf.cloud.core.netty.temp.NettyConnectionRecoveryHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

class NettyClientConnectionImpl(client: SurfNettyClient) :
    AbstractNettyConnection<NettyClientConnectionImpl, NettyServerSource, SurfNettyClient>(client),
    NettyClientConnection {
    private val log = logger()

    private val port = client.port
    private val host = client.host

    private val group = NioEventLoopGroup(1)
    private val recoveryHandler = NettyConnectionRecoveryHandler(this, 5.seconds)
    private val context get() = coreCloudInstance.dataContext

    override val clientTracker = NettyClientTrackerImpl(base)
    override fun source(channel: Channel) = source

    override lateinit var source: NettyServerSource
    override lateinit var channel: Channel

    override val connected: Boolean
        get() = channel.isActive

    override suspend fun tryEstablishConnection0() {
        log.atInfo()
            .log("Connecting to '%s:%d' ...", this.host, this.port)

        recoveryHandler.stopReconnection()

        val bootstrap = Bootstrap()
            .group(group)
            .channel(NioSocketChannel::class.java)
            .handler(context.getBean(SurfNettyChannelInitializer::class.java))
            .option(ChannelOption.SO_KEEPALIVE, true)

            //        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            .connect(this.host, this.port)
            .addListener((ChannelFutureListener { future ->
                if (!future.isSuccess) {
                    log.atSevere()
                        .log(
                            "Failed to connect to %s:%d - %s",
                            host,
                            port,
                            future.cause()?.message ?: "Unknown"
                        )

                    recoveryHandler.attemptReconnect(group.next())
                    return@ChannelFutureListener
                }

                log.atInfo()
                    .log("Successfully connected to %s:%d", host, port)

                this.source = NettyServerSourceImpl(base)
                this.channel = future.channel()
                channel.closeFuture().addListener(recoveryHandler)

                //          this.recoveryHandler.resumeReconnect();
                log.atInfo()
                    .log("Connected to %s!", channel.remoteAddress())
            }))
            .suspend()
    }

    private suspend fun sendFetchPreload() {
        val registered = registerServer(
            CloudServerRegistrationData(
                CloudPersistentData.SERVER_ID.value()!!,
                CloudProperties.SERVER_CATEGORY.value(),
                port,
                host
            )
        )

        CloudPersistentData.SERVER_ID.setValue(registered.serverGuid)
    }

    override suspend fun close() {
        recoveryHandler.stopReconnection()

        channel.close().suspend()
        group.shutdownGracefully().suspend()
    }

    // TODO: 14.09.2024 18:35 - https://git.slne.dev/silence/creepcore/-/blob/master/CreepBungee/src/main/java/me/tomicake/capi/bungee/client/ContainerClient.java?ref_type=heads#L129
    suspend fun registerServer(data: CloudServerRegistrationData) = suspendCoroutine {
        val packet = CloudRegisterServerPacket(
            CloudRegisterServerPacket.Type.FETCH_PRELOAD,
            data
        )
        this.sendPacket(packet)

        it.resume(clientTracker.findByServerGuid(data.serverId)!!)
        //    sourceTracker().findByServerGuid()
    }

    /**
     * Sends a packet with the local source
     *
     * @param packet the packet to send
     */
    fun sendPacket(packet: NettyPacket<*>) {
        this.sendPacket(source, packet)
    }

    override fun sendPacket0(
        source: AbstractNettySource<NettyServerSource>,
        packet: NettyPacket<*>
    ) {
        if (source is NettyServerSourceImpl) {
            val proxied = ProxiedNettyPacket(packet, source, base)
            channel.writeAndFlush(proxied)
        } else {
            channel.writeAndFlush(packet)
        }
    }

    override fun broadcast(packet: NettyPacket<*>) {
        sendPacket(packet)
    }

    @Component
    @Profile("client")
    class InternalPacketListener {
        @SurfNettyPacketHandler
        fun onCloudRegisterServer(packet: CloudRegisterServerPacket) {
            val serverId = packet.data.serverId
        }
    }

    @Component
    @Profile("client")
    class NettyCLientChannelInitializerModifier : ChannelInitializerModifier {
        override fun modify(channel: Channel) {
            System.err.println("MODIFYING CHANNEL")
            channel.pipeline().addFirst("idleStateHandler", IdleStateHandler(15, 15, 0))
                .addLast("keepAliveHandler", NettyKeepAliveHandler())
        }

        private class NettyKeepAliveHandler : ChannelDuplexHandler() {
            @Throws(Exception::class)
            override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
                if (evt is IdleStateEvent) {
                    if (evt.state() == IdleState.WRITER_IDLE) {
                        println("Sending Keepalive packet...")
                    }
                } else {
                    super.userEventTriggered(ctx, evt)
                }
            }
        }
    }
}
