package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.exceptions.SkipPacketException
import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.*
import dev.slne.surf.cloud.core.coroutines.NettyConnectionScope
import dev.slne.surf.cloud.core.coroutines.NettyListenerScope
import dev.slne.surf.cloud.core.netty.network.protocol.handshake.ClientIntent
import dev.slne.surf.cloud.core.netty.network.protocol.handshake.HandshakeProtocols
import dev.slne.surf.cloud.core.netty.network.protocol.handshake.ServerHandshakePacketListener
import dev.slne.surf.cloud.core.netty.network.protocol.handshake.ServerboundHandshakePacket
import dev.slne.surf.cloud.core.netty.network.protocol.initialize.*
import dev.slne.surf.cloud.core.netty.network.protocol.login.*
import dev.slne.surf.cloud.core.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.netty.protocol.packets.ServerboundBroadcastPacket
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.EncoderException
import io.netty.handler.flow.FlowControlHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.TimeoutException
import kotlinx.coroutines.CompletableDeferred
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

    @Volatile
    private var sendLoginDisconnect = true

    var preparing = true
        private set

    private var disconnectionHandled = false

    var receivedPackets = 0
        private set

    var sentPackets = 0
        private set

    var averageReceivedPackets = 0f
        private set

    var averageSentPackets = 0f
        private set

    private var handlingFault = false

    /**
     *  The hostname of the connection (address:port)
     */
    var hostname: String = ""

    lateinit var virtualHost: InetSocketAddress

    var isPending: Boolean = true

    var stopReadingPackets = false
        private set

    var disconnectionDetails: DisconnectionDetails? = null
        private set

    @Volatile
    private var delayedDisconnect: DisconnectionDetails? = null

    val connected get() = _channel != null && _channel!!.isOpen
    val connecting get() = _channel == null


    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        _channel = ctx.channel()
        _address = channel.remoteAddress()
        preparing = false

        delayedDisconnect?.let { disconnect(it) }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        disconnect("End of stream")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, e: Throwable?) {
        log.atInfo().withCause(e).log("Exception caught") // TODO: remove this debug line
        var throwable = e

        if (throwable is EncoderException) {
            val cause = throwable.cause

            if (cause is PacketTooLargeException) {
                val packet = cause.packet
                if (packet.packetTooLarge(this)) {
                    ProtocolSwapHandler.handleOutboundTerminalPacket(ctx, packet)
                    return
                } else if (packet.skippable) {
                    log.atFine().withCause(cause).log("Skipping packet due to errors")
                    ProtocolSwapHandler.handleOutboundTerminalPacket(ctx, packet)
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
                    val reason = "Internal Exception: ${throwable?.message}"
                    val disconnectionDetails =
                        _packetListener?.createDisconnectionInfo(reason, throwable)
                            ?: DisconnectionDetails("Internal Exception: ${throwable?.message}")

                    if (previousHandlingFault) {
                        log.atFine().withCause(throwable).log("Failed to sent packet")

                        val doesDisconnectExist =
                            _packetListener?.protocol != ConnectionProtocol.INITIALIZE && _packetListener?.protocol != ConnectionProtocol.HANDSHAKING

                        if (sending == PacketFlow.CLIENTBOUND && doesDisconnectExist) {
                            val packet = if (sendLoginDisconnect) ClientboundLoginDisconnectPacket(
                                disconnectionDetails
                            ) else ClientboundDisconnectPacket(disconnectionDetails)

                            NettyConnectionScope.launch {
                                sendWithIndication(packet)
                                disconnect(disconnectionDetails)
                            }
                        } else {
                            disconnect(disconnectionDetails)
                        }

                        setReadOnly()
                    } else {
                        log.atFine().withCause(throwable).log("Double fault")
                        disconnect(disconnectionDetails)
                    }
                }
            }
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: NettyPacket) {
        if (!channel.isOpen) return

        val packetListener = _packetListener
        check(packetListener != null) { "Received a packet before the packet listener was initialized" }

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
                                else -> error("Unexpected packet $msg")
                            }
                        }

                        is ServerInitializePacketListener -> {
                            when (msg) {
                                is ServerboundInitializeRequestIdPacket -> packetListener.handleIdRequest(
                                    msg
                                )

                                else -> error("Unexpected packet $msg")
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

                                else -> error("Unexpected packet $msg")
                            }
                        }

                        is RunningServerPacketListener -> {
                            when (msg) {
                                is ServerboundBroadcastPacket -> packetListener.handleBroadcastPacket(
                                    msg
                                )

                                else -> packetListener.handlePacket(msg) // handle other packets
                            }
                        }
                    }
                }

                is ClientboundPacketListener -> {
                    when (packetListener) {
                        is ClientInitializePacketListener -> {
                            when (msg) {
                                is ClientboundInitializeIdResponsePacket -> packetListener.handleIdResponse(
                                    msg
                                )

                                else -> error("Unexpected packet $msg")
                            }
                        }

                        is ClientLoginPacketListener -> {
                            when (msg) {
                                is ClientboundLoginFinishedPacket -> packetListener.handleLoginFinished(
                                    msg
                                )

                                else -> error("Unexpected packet $msg")
                            }
                        }

                        is RunningClientPacketListener -> {
                            when (msg) {
                                is ClientboundKeepAlivePacket -> packetListener.handleKeepAlive(msg)
                                is ClientboundPingPacket -> packetListener.handlePing(msg)
                                is ClientboundDisconnectPacket -> packetListener.handleDisconnect(
                                    msg
                                )

                                else -> packetListener.handlePacket(msg)
                            }
                        }
                    }
                }

                else -> error("Invalid packet listener")
            }
        }
    }

    private fun validateListener(protocolInfo: ProtocolInfo<*>, listener: PacketListener) {
        check(listener.flow == this.receiving) { "Trying to set listener for wrong side: connection is $receiving, but listener is ${listener.flow}" }
        check(protocolInfo.id == listener.protocol) { "Listener protocol (${listener.protocol}) does not match requested one (${protocolInfo})" }
    }

    suspend fun <T : PacketListener> setupInboundProtocol(newState: ProtocolInfo<T>, listener: T) {
        validateListener(newState, listener)
        check(newState.flow == receiving) { "Invalid inbound protocol: ${newState.flow}" }

        this._packetListener = listener

        val protocol = UnconfiguredPipelineHandler.setupInboundProtocol(newState)
        syncAfterConfigurationChange(channel.writeAndFlush(protocol))
    }

    suspend fun setupOutboundProtocol(newState: ProtocolInfo<*>) {
        check(newState.flow == sending) { "Invalid outbound protocol: ${newState.flow}" }

        val protocol = UnconfiguredPipelineHandler.setupOutboundProtocol(newState)

        val login = newState.id == ConnectionProtocol.LOGIN
        syncAfterConfigurationChange(channel.writeAndFlush(protocol.andThen {
            this.sendLoginDisconnect = login
        }))
    }

    fun setListenerForServerboundHandshake(packetListener: PacketListener) {
        check(this._packetListener == null) { "Listener already set" }
        check(receiving == PacketFlow.SERVERBOUND && packetListener.flow == PacketFlow.SERVERBOUND && packetListener.protocol == INITIAL_PROTOCOL.id) { "Invalid initial listener" }
        this._packetListener = packetListener
    }

    suspend fun initiateServerboundInitializeConnection(
        hostname: String,
        port: Int,
        listener: ClientInitializePacketListener
    ) {
        initiateServerboundConnection(
            hostname,
            port,
            InitializeProtocols.SERVERBOUND,
            InitializeProtocols.CLIENTBOUND,
            listener,
            ClientIntent.INITIALIZE
        )
    }

    suspend fun initiateServerboundRunningConnection(
        hostname: String,
        port: Int,
        listener: ClientLoginPacketListener
    ) {
        initiateServerboundConnection(
            hostname,
            port,
            LoginProtocols.SERVERBOUND,
            LoginProtocols.CLIENTBOUND,
            listener,
            ClientIntent.LOGIN
        )
    }

    suspend fun <S : ServerboundPacketListener, C : ClientboundPacketListener> initiateServerboundRunningConnection(
        hostname: String,
        port: Int,
        serverboundProtocolInfo: ProtocolInfo<S>,
        clientboundProtocolInfo: ProtocolInfo<C>,
        clientboundListener: C,
        initialze: Boolean
    ) {
        initiateServerboundConnection(
            hostname,
            port,
            serverboundProtocolInfo,
            clientboundProtocolInfo,
            clientboundListener,
            if (initialze) ClientIntent.INITIALIZE else ClientIntent.LOGIN
        )
    }

    private suspend fun <S : ServerboundPacketListener, C : ClientboundPacketListener> initiateServerboundConnection(
        hostname: String,
        port: Int,
        serverboundProtocolInfo: ProtocolInfo<S>,
        clientboundProtocolInfo: ProtocolInfo<C>,
        clientboundListener: C,
        intention: ClientIntent
    ) {
        check(serverboundProtocolInfo.id == clientboundProtocolInfo.id) { "Mismatched initial protocols" }
        runOnceConnectedSuspend {
            setupInboundProtocol(clientboundProtocolInfo, clientboundListener)
            sendSuspendPacket(ServerboundHandshakePacket(hostname, port, intention), flush = true)
            setupOutboundProtocol(serverboundProtocolInfo)
        }
    }

    fun broadcast(packet: NettyPacket) {
        if (!connected) return
//        send(ServerboundBroadcastPacket(packet))
        TODO("not implemented")
    }

    fun send(packet: NettyPacket, flush: Boolean = true) {
        internalSend(packet, flush)
    }

    suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean = true,
        convertExceptions: Boolean = true
    ): Boolean {
        val result = runCatching {
            val deferred = CompletableDeferred<Boolean>()
            internalSend(packet, flush, deferred)
            deferred.await()
        }

        if (convertExceptions) {
            return result.getOrDefault(false)
        }

        return result.getOrThrow()
    }

    fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean = true,
        deferred: CompletableDeferred<Boolean>
    ) {
        internalSend(packet, flush, deferred)
    }

    private fun internalSend(
        packet: NettyPacket,
        flush: Boolean = true,
        deferred: CompletableDeferred<Boolean>? = null
    ) {
        val connected = connected
        if (!connected && !preparing) {
            deferred?.complete(false)
            return
        }

        if (connected
            && (Util.canSendImmediate(this, packet)
                    || (packet.isReady() && pendingActions.isEmpty() && packet.extraPackets?.isEmpty() == true))
        ) {
            sendPacket(packet, flush, deferred)
        } else {
            // Write the packets to the queue, then flush
            val extraPackets = Util.buildExtraPackets(packet)

            if (extraPackets.isNullOrEmpty()) {
                pendingActions.add(PacketSendAction(packet, flush, deferred))
            } else {
                pendingActions.addAll(buildList {
                    // Delay the future listener until the end of the extra packets
                    add(PacketSendAction(packet, false, deferred))
                    extraPackets.forEachIndexed { index, extraPacket ->
                        add(PacketSendAction(extraPacket, index == extraPackets.size - 1))
                    }
                })
            }

            flushQueue()
        }
    }

    private suspend fun sendSuspendPacket(
        packet: NettyPacket,
        flush: Boolean,
    ): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        sendPacket(packet, flush, deferred)
        return deferred.await()
    }

    private fun sendPacket(
        packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>? = null
    ) {
        sentPackets++
        val channel = _channel ?: return
        val eventLoop = channel.eventLoop()

        if (eventLoop.inEventLoop()) {
            doSendPacket(packet, flush, deferred)
        } else {
            eventLoop.execute { doSendPacket(packet, flush, deferred) }
        }
    }

    private fun doSendPacket(
        packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>? = null
    ) {
        if (!connected) {
            deferred?.complete(false)
            return
        }

        try {
            val channelFuture = if (flush) channel.writeAndFlush(packet) else channel.write(packet)

            if (deferred != null) {
                channelFuture.addListener { future ->
                    if (future.isSuccess) {
                        deferred.complete(true)
                    } else {
                        deferred.completeExceptionally(future.cause())
                    }
                }
            }

            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        } catch (e: Exception) {
            log.atSevere().withCause(e).log("NetworkException: ")
            deferred?.completeExceptionally(e)
            disconnect("Internal Exception: ${e.message}")
        }
    }

    fun runOnceConnected(block: Connection.() -> Unit) {
        if (connected) {
            flushQueue()
            block(this)
        } else {
            pendingActions.add(WrappedConsumer(block))
        }
    }

    suspend fun runOnceConnectedSuspend(block: suspend Connection.() -> Unit) {
        if (connected) {
            flushQueue()
            block(this)
        } else {
            val deferred = CompletableDeferred<Unit>()
            pendingActions.add(WrappedConsumer {
                NettyConnectionScope.launch {
                    deferred.complete(block(it))
                }
            })
            deferred.await()
        }
    }

    fun flushChannel() = runOnceConnected {
        flush()
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

    suspend fun tick() {
        flushQueue()

        val packetListener = _packetListener
        if (packetListener is TickablePacketListener) {
            packetListener.tick()
        }

        if (!connected && !disconnectionHandled) {
            handleDisconnection()
        }


        averageSentPackets = lerp(0.75f, sentPackets.toFloat(), averageSentPackets)
        averageReceivedPackets = lerp(0.75f, receivedPackets.toFloat(), averageReceivedPackets)
        sentPackets = 0
        receivedPackets = 0
    }

    fun disconnect(reason: String) {
        disconnect(DisconnectionDetails(reason))
    }

    fun disconnect(reason: DisconnectionDetails) {
        preparing = false
        clearPacketQueue()

        val channel = _channel

        if (channel == null) {
            this.delayedDisconnect = reason
            return
        }

        if (connected) {
            channel.close()
            this.disconnectionDetails = reason
        }
    }

    fun clearPacketQueue() {
        pendingActions.clear()
    }

    fun configurePacketHandler(pipeline: ChannelPipeline) {
        pipeline
            .addLast("hackfix", object : ChannelOutboundHandlerAdapter() {
                override fun write(
                    ctx: ChannelHandlerContext?,
                    msg: Any?,
                    promise: ChannelPromise?
                ) {
                    super.write(ctx, msg, promise)
                }
            })
            .addLast(HandlerNames.PACKET_HANDLER, this)
    }

    fun setReadOnly() {
        _channel?.config()?.setAutoRead(false)
    }

    fun enableAutoRead() {
        _channel?.config()?.setAutoRead(true)
    }

    fun handleDisconnection() {
        val channel = _channel ?: return
        if (channel.isOpen) return
        if (disconnectionHandled) return

        disconnectionHandled = true
        _packetListener?.onDisconnect(disconnectionDetails ?: DisconnectionDetails("Disconnected"))

        clearPacketQueue()
    }

    fun getLoggableAddress(logIps: Boolean) =
        if (_address == null) "local" else (if (logIps) _address.toString() else "IP hidden")


    private object Util {
        fun canSendImmediate(connection: Connection, packet: NettyPacket): Boolean {
            return connection.isPending || connection._packetListener?.protocol != ConnectionProtocol.RUNNING
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

    private class PacketSendAction(
        val packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>? = null
    ) : WrappedConsumer({ it.sendPacket(packet, flush, deferred) })

    companion object {
        private val log = logger()

        val NETWORK_WORKER_GROUP: NioEventLoopGroup by lazy {
            NioEventLoopGroup(
                threadFactory {
                    nameFormat("Netty Client IO #%d")
                    daemon(true)
                    uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                }
            )
        }

        val NETWORK_EPOLL_WORKER_GROUP: EpollEventLoopGroup by lazy {
            EpollEventLoopGroup(
                threadFactory {
                    nameFormat("Netty Epoll Client IO #%d")
                    daemon(true)
                    uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                }
            )
        }

        private val INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND

        suspend fun connectToServer(
            address: InetSocketAddress,
            useEpoll: Boolean,
        ): Connection {
            val connection = Connection(PacketFlow.CLIENTBOUND)
            connect(address, useEpoll, connection)
            return connection
        }

        suspend fun connect(
            address: InetSocketAddress,
            useEpoll: Boolean,
            connection: Connection
        ) {
            val channelClass: Class<out SocketChannel>
            val eventLoopGroup: EventLoopGroup

            if (Epoll.isAvailable() && useEpoll) {
                channelClass = EpollSocketChannel::class.java
                eventLoopGroup = NETWORK_EPOLL_WORKER_GROUP
                log.atInfo().log("Using Epoll")
            } else {
                channelClass = NioSocketChannel::class.java
                eventLoopGroup = NETWORK_WORKER_GROUP
                log.atInfo().log("Using NIO")
            }

            Bootstrap()
                .group(eventLoopGroup)
                .channel(channelClass)
                .handler(object : ChannelInitializer<Channel>() {
                    override fun initChannel(channel: Channel) {
                        runCatching {
                            channel.config().setOption(ChannelOption.TCP_NODELAY, true)
                        }

                        val pipeline = channel.pipeline()
                            .addLast(HandlerNames.TIMEOUT, ReadTimeoutHandler(30))

                        configureSerialization(pipeline, PacketFlow.CLIENTBOUND, local = false)
                        connection.configurePacketHandler(pipeline)
                    }
                })
                .connect(address.address, address.port)
                .suspend()
        }

        fun configureSerialization(pipeline: ChannelPipeline, side: PacketFlow, local: Boolean) {
            val opposite = side.getOpposite()
            val receivingSide = side == PacketFlow.SERVERBOUND
            val sendingSide = opposite == PacketFlow.SERVERBOUND

            pipeline.addLast(HandlerNames.SPLITTER, createFrameDecoder(local))
                .addLast(FlowControlHandler())
                .addLast(
                    inboundHandlerName(receivingSide),
                    if (receivingSide) PacketDecoder(INITIAL_PROTOCOL) else UnconfiguredPipelineHandler.Inbound()
                )
                .addLast(HandlerNames.PREPENDER, createFrameEncoder(local))
                .addLast(
                    outboundHandlerName(sendingSide),
                    if (sendingSide) PacketEncoder(INITIAL_PROTOCOL) else UnconfiguredPipelineHandler.Outbound()
                )
        }

        private fun createFrameEncoder(local: Boolean) =
            if (local) NoOpFrameEncoder() else Varint21LengthFieldPrepender()

        private fun createFrameDecoder(local: Boolean) =
            if (local)  NoOpFrameDecoder() else Varint21FrameDecoder()

        private fun outboundHandlerName(sendingSide: Boolean) =
            if (sendingSide) HandlerNames.ENCODER else HandlerNames.OUTBOUND_CONFIG

        private fun inboundHandlerName(receivingSide: Boolean) =
            if (receivingSide) HandlerNames.DECODER else HandlerNames.INBOUND_CONFIG

        private suspend fun syncAfterConfigurationChange(future: ChannelFuture) {
            try {
                future.suspend()
            } catch (exception: Exception) {
                if (exception is ClosedChannelException) {
                    log.atInfo().log("Connection closed during protocol change")
                } else {
                    throw exception
                }
            }
        }
    }
}