package dev.slne.surf.cloud.core.netty.network

import com.google.common.util.concurrent.ThreadFactoryBuilder
import dev.slne.surf.cloud.api.exceptions.SkipPacketException
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.DefaultUncaughtExceptionHandlerWithName
import dev.slne.surf.cloud.api.util.lerp
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.api.util.suspend
import dev.slne.surf.cloud.core.coroutines.NettyListenerScope
import dev.slne.surf.cloud.core.netty.network.protocol.handshake.HandshakeProtocols
import dev.slne.surf.cloud.core.netty.network.protocol.handshake.ServerHandshakePacketListener
import dev.slne.surf.cloud.core.netty.network.protocol.login.ServerLoginPacketListener
import dev.slne.surf.cloud.core.netty.network.protocol.running.RunningServerPacketListener
import dev.slne.surf.cloud.core.netty.protocol.codec.*
import dev.slne.surf.cloud.core.netty.protocol.packet.handler.UnconfiguredPipelineHandler
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.ServerboundBroadcastPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.handshake.serverbound.ServerboundHandshakePacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.clientbound.ClientboundInitializeClientPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.clientbound.ClientboundLoginFinishedPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginStartPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound.ClientboundKeepAlivePacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.serverbound.ServerboundKeepAlivePacket
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.EncoderException
import io.netty.handler.flow.FlowControlHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.TimeoutException
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.ClosedChannelException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class Connection(val receiving: PacketFlow) : SimpleChannelInboundHandler<NettyPacket>() {
    val log = logger()

    private val pendingActions = ConcurrentLinkedQueue<WrappedConsumer>()

    private var _channel: Channel? = null
    val channel get() = _channel ?: throw IllegalStateException("Channel is not initialized")

    private var _address: SocketAddress? = null
    val address get() = _address ?: throw IllegalStateException("Address is not initialized")

    @Volatile
    private var _packetListener: PacketListener? = null
    val packetListener get() = _packetListener ?: error("Listener not set")

    val sending get() = receiving.getOpposite()

    var preparing = true
        private set

    private var disconnectionHandled = false

    private var receivedPackets = 0

    private var sentPackets = 0

    private var averageReceivedPackets = 0f

    private var averageSentPackets = 0f

    private var handlingFault = false

    /**
     *  The hostname of the connection (address:port)
     */
    var hostname: String = ""

    lateinit var virtualHost: InetSocketAddress

    var isPending: Boolean = true

    var stopReadingPackets = false
        private set

    var disconnectionReason: String = ""
        private set

    val connected get() = _channel?.isOpen ?: false
    val connecting get() = _channel == null


    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        _channel = ctx.channel()
        _address = channel.remoteAddress()
        preparing = false

        // delayed disconnect?
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        disconnect("End of stream")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, e: Throwable?) {
        var throwable = e

        if (throwable is EncoderException) {
            val cause = throwable.cause

            if (cause is PacketTooLargeException) {
                val packet = cause.packet
                if (packet.packetTooLarge(this)) {
                    return
                } else if (packet.skippable) {
                    log.atFine().withCause(cause).log("Skipping packet due to errors")
                    return
                } else {
                    throwable = cause
                }
            }
        }

        if (throwable is SkipPacketException) {
            log.atFine().withCause(throwable.cause).log("Skipping packet due to errors")
        } else {
            val previousHandlingFault = !handlingFault
            handlingFault = true

            if (channel.isOpen) {
                if (throwable is TimeoutException) {
                    log.atFine().withCause(throwable).log("Timeout")
                    disconnect("Timed out")
                } else {
                    if (previousHandlingFault) {
                        log.atFine().withCause(throwable).log("Failed to sent packet")
                        disconnect("Internal Exception: $throwable")
                        setReadOnly()
                    } else {
                        log.atFine().withCause(throwable).log("Double fault")
                        disconnect("Internal Exception: $throwable")
                    }
                }
            }
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: NettyPacket) {
        val packetListener = _packetListener
        check(packetListener != null) { "Received a packet before the packet listener was initialized" }

        if (!channel.isOpen) return
        if (stopReadingPackets) return

        receivedPackets++
        NettyListenerScope.launch {
            // Directly handle our packets
            when (packetListener) {
                is ServerboundPacketListener -> {
                    when (packetListener) {
                        is ServerHandshakePacketListener -> {
                            when (msg) {
                                is ServerboundHandshakePacket -> packetListener.handleHandshake(msg)
                            }
                        }

                        is ServerLoginPacketListener -> {
                            when (msg) {
                                is ServerboundLoginStartPacket -> packetListener.handleLoginStart(
                                    msg
                                )

                                is ServerboundLoginAcknowledgedPacket -> packetListener.handleLoginAcknowledgement(
                                    msg
                                )
                            }
                        }

                        is RunningServerPacketListener -> {
                            when (msg) {
                                else -> packetListener.handlePacket(msg) // handle other packets
                            }
                        }
                    }
                }

                is ClientboundPacketListener -> {

                }

                else -> error("Invalid packet listener")
            }
        }
    }

    suspend fun <T : PacketListener> setupInboundProtocol(newState: ProtocolInfo<T>, listener: T) {
        check(newState.flow == receiving) { "Invalid inbound protocol: ${newState.flow}" }
        this._packetListener = listener

        val protocol = UnconfiguredPipelineHandler.setupInboundProtocol(newState)
        syncAfterConfigurationChange(channel.writeAndFlush(protocol))
    }

    suspend fun setupOutboundProtocol(newState: ProtocolInfo<*>) {
        check(newState.flow == sending) { "Invalid outbound protocol: ${newState.flow}" }

        val protocol = UnconfiguredPipelineHandler.setupOutboundProtocol(newState)
        syncAfterConfigurationChange(channel.writeAndFlush(protocol))
    }

    fun setListenerForServerboundHandshake(packetListener: PacketListener) {
        check(this._packetListener == null) { "Listener already set" }
        check(receiving == PacketFlow.SERVERBOUND && packetListener.flow == PacketFlow.SERVERBOUND && packetListener.protocol == INITIAL_PROTOCOL.id) { "Invalid initial listener" }
        this._packetListener = packetListener
    }

    fun broadcast(packet: NettyPacket) {
        if (!connected) return
        send(ServerboundBroadcastPacket(packet))
    }

    @JvmOverloads
    fun send(packet: NettyPacket, flush: Boolean = true) {
        val connected = connected
        if (!connected && !preparing) return

        if (connected
            && (Util.canSendImmediate(this, packet)
                    || (packet.isReady() && pendingActions.isEmpty() && packet.extraPackets?.isEmpty() == true))
        ) {
            sendPacket(packet, flush)
        } else {
            // Write the packets to the queue, then flush
            val extraPackets = Util.buildExtraPackets(packet)

            if (extraPackets.isNullOrEmpty()) {
                pendingActions.add(PacketSendAction(packet, flush))
            } else {
                pendingActions.addAll(buildList {
                    add(
                        PacketSendAction(
                            packet,
                            false
                        )
                    )  // Delay the future listener until the end of the extra packets

                    extraPackets.forEachIndexed { index, extraPacket ->
                        add(PacketSendAction(extraPacket, index == extraPackets.size - 1))
                    }
                })
            }

            flushQueue()
        }
    }

    private fun sendPacket(packet: NettyPacket<*>, flush: Boolean) {
        sentPackets++
        val channel = _channel ?: return
        val eventLoop = channel.eventLoop()

        if (eventLoop.inEventLoop()) {
            doSendPacket(packet, flush)
        } else {
            eventLoop.execute { doSendPacket(packet, flush) }
        }
    }

    private fun doSendPacket(packet: NettyPacket<*>, flush: Boolean) {
        if (!connected) return

        try {
            val channelfuture = if (flush) channel.writeAndFlush(packet) else channel.write(packet)

            // TODO: callbacks

            channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        } catch (e: Exception) {
            log.atSevere().withCause(e).log("NetworkException:")
            disconnect("Internal Exception: ${e.message}")
        }
    }

    fun flushChannel() {
        if (connected) {
            flush()
        } else {
            pendingActions.add(WrappedConsumer { flush() })
        }
    }

    private fun flush() {
        val channel = _channel ?: return
        val eventLoop = channel.eventLoop()

        if (eventLoop.inEventLoop()) {
            channel.flush()
        } else {
            eventLoop.execute { channel.flush() }
        }
    }

    private fun flushQueue(): Boolean {
        if (!connected) return true

        if (this.isPending) {
            synchronized(this.pendingActions) {
                return this.processQueue()
            }
        }

        return false
    }

    private fun processQueue(): Boolean {
        if (pendingActions.isEmpty()) return true

        val iterator = pendingActions.iterator()
        while (iterator.hasNext()) {
            val queued = iterator.next() ?: return true // poll -> peek
            if (queued.isConsumed()) continue


            if (queued is PacketSendAction) {
                val packet = queued.packet
                if (!packet.isReady()) return false
            }

            iterator.remove()
            if (queued.tryMarkConsumed()) {
                queued.consumer(this)
            }
        }
        return true
    }

    fun clearPacketQueue() {
        pendingActions.clear()
    }

    fun disconnect(reason: String) {
        preparing = false
        clearPacketQueue()

        if (connected) {
            channel.close()
            this.disconnectionReason = reason
        }
    }

    fun handleDisconnection() {
        val channel = _channel ?: return
        if (channel.isOpen) return
        if (disconnectionHandled) return

        disconnectionHandled = true

        // TODO: inform listener?

        clearPacketQueue()
    }


    suspend fun tick() {
        flushQueue()

        if (!connected && !disconnectionHandled) {
            handleDisconnection()
        }

        averageSentPackets = lerp(0.75f, sentPackets.toFloat(), averageSentPackets)
        averageReceivedPackets = lerp(0.75f, receivedPackets.toFloat(), averageReceivedPackets)
        sentPackets = 0
        receivedPackets = 0
    }

    fun setReadOnly() {
        _channel?.config()?.setAutoRead(false)
    }

    fun configurePacketHandler(pipeline: ChannelPipeline) {
        pipeline.addLast("packet_handler", this)
    }

    fun getLoggableAddress(logIps: Boolean) =
        if (_address == null) "local" else (if (logIps) _address.toString() else "IP hidden")


    private object Util {
        fun canSendImmediate(connection: Connection, packet: NettyPacket): Boolean {
            return connection.isPending
                    || packet is ProxiedNettyPacket
                    || packet is ServerboundHandshakePacket
                    || packet is ClientboundInitializeClientPacket
                    || packet is ClientboundLoginFinishedPacket
                    || packet is ServerboundLoginStartPacket
                    || packet is ServerboundLoginAcknowledgedPacket
                    || packet is ClientboundKeepAlivePacket
                    || packet is ServerboundKeepAlivePacket
        }

        fun buildExtraPackets(packet: NettyPacket): List<NettyPacket>? {
            val extra = packet.extraPackets ?: return null
            if (extra.isEmpty()) return extra
            return buildList { buildExtraPackets0(extra) }
        }

        private fun MutableList<NettyPacket>.buildExtraPackets0(extraPackets: List<NettyPacket>) {
            for (extra in extraPackets) {
                add(extra)
                val extraExtra = extra.extraPackets

                if (!extraExtra.isNullOrEmpty()) buildExtraPackets0(extraExtra)
            }
        }
    }

    private open class WrappedConsumer(val consumer: (Connection) -> Unit) {
        private val consumed = AtomicBoolean(false)

        fun tryMarkConsumed() = consumed.compareAndSet(false, true)
        fun isConsumed() = consumed.get()
    }

    private class PacketSendAction(val packet: NettyPacket, flush: Boolean) :
        WrappedConsumer({ it.sendPacket(packet, flush) })

    companion object {
        private val log = logger()

        val NETWORK_WORKER_GROUP: NioEventLoopGroup by lazy {
            NioEventLoopGroup(
                ThreadFactoryBuilder()
                    .setNameFormat("Netty Client IO #%d")
                    .setDaemon(true)
                    .setUncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                    .build()
            )
        }

        val NETWORK_EPOLL_WORKER_GROUP: EpollEventLoopGroup by lazy {
            EpollEventLoopGroup(
                ThreadFactoryBuilder()
                    .setNameFormat("Netty Epoll Client IO #%d")
                    .setDaemon(true)
                    .setUncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                    .build()
            )
        }

        private val INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND

        suspend fun connectToServer(
            address: InetSocketAddress,
            useEpoll: Boolean,
        ): Connection {
            val connection = Connection(null)
            connectToServer(address, useEpoll, connection)
            return connection
        }

        suspend fun connectToServer(
            address: InetSocketAddress,
            useEpoll: Boolean,
            connection: Connection
        ) {
            val channelClass: Class<out Channel>
            val eventLoopGroup: EventLoopGroup

            if (Epoll.isAvailable() && useEpoll) {
                channelClass = EpollSocketChannel::class.java
                eventLoopGroup = NETWORK_EPOLL_WORKER_GROUP
            } else {
                channelClass = NioSocketChannel::class.java
                eventLoopGroup = NETWORK_WORKER_GROUP
            }

            Bootstrap()
                .group(eventLoopGroup)
                .channel(channelClass)
                .handler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(channel: Channel) {
                        runCatching {
                            channel.config().setOption(ChannelOption.TCP_NODELAY, true)
                        }

                        val pipeline = channel.pipeline().addLast("timeout", ReadTimeoutHandler(30))

                        configureSerialization(pipeline, PacketFlow.CLIENTBOUND, local = false)
                        connection.configurePacketHandler(pipeline)
//                        bean<SurfNettyChannelInitializer>().initChannel(channel)
                    }
                })
                .connect(address.address, address.port)
                .suspend()
        }

        fun configureSerialization(pipeline: ChannelPipeline, side: PacketFlow, local: Boolean) {
            val opposite = side.getOpposite()
            val receivingSide = side == PacketFlow.SERVERBOUND
            val sendingSide = opposite == PacketFlow.SERVERBOUND

            pipeline.addLast("splitter", createFrameDecoder(local))
                .addLast(FlowControlHandler())
                .addLast(
                    inboundHandlerName(receivingSide),
                    if (receivingSide) else UnconfiguredPipelineHandler.Inbound()
                )
                .addLast("prepender", createFrameEncoder(local))
                .addLast(
                    outboundHandlerName(sendingSide),
                    if (sendingSide) PacketEncoder(INITIAL_PROTOCOL) else UnconfiguredPipelineHandler.Outbound()
                )
        }

        private fun createFrameEncoder(local: Boolean) =
            if (local) NoOpFrameEncoder() else Varint21LengthFieldPrepender()

        private fun createFrameDecoder(local: Boolean) =
            if (!local) Varint21FrameDecoder() else NoOpFrameDecoder()


        private fun outboundHandlerName(sendingSide: Boolean) =
            if (sendingSide) "encoder" else "outbound_config"

        private fun inboundHandlerName(receivingSide: Boolean) =
            if (receivingSide) "decoder" else "inbound_config"

        private suspend fun syncAfterConfigurationChange(future: ChannelFuture) {
            try {
                future.suspend()
            } catch (exception: java.lang.Exception) {
                if (exception is ClosedChannelException) {
                    log.atInfo().log("Connection closed during protocol change")
                } else {
                    throw exception
                }
            }
        }
    }
}