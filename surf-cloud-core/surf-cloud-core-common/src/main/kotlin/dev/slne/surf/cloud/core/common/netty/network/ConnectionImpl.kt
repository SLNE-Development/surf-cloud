package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.exceptions.SkipPacketException
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode.*
import dev.slne.surf.cloud.api.common.util.DefaultUncaughtExceptionHandlerWithName
import dev.slne.surf.cloud.api.common.util.math.lerp
import dev.slne.surf.cloud.api.common.util.netty.suspend
import dev.slne.surf.cloud.api.common.util.threadFactory
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerIoScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ClientIntent
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.HandshakeProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.PROTOCOL_VERSION
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ServerboundHandshakePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ClientInitializePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.InitializeProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientLoginPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientboundLoginDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.LoginProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.handler.codec.compression.ZstdDecoder
import io.netty.handler.codec.compression.ZstdEncoder
import io.netty.handler.flow.FlowControlHandler
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.NotSslRecordException
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.TimeoutException
import io.netty.util.AttributeKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.ClosedChannelException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class ConnectionImpl(
    val receiving: PacketFlow,
    val encryptionManager: EncryptionManager
) : SimpleChannelInboundHandler<NettyPacket>(), Connection {
    val log = logger()

    private val pendingActions = ConcurrentLinkedQueue<WrappedConsumer>()
    private val drainingActions = AtomicBoolean(false)

    private var _channel: Channel? = null
    val channel get() = _channel ?: error("Channel is not initialized")

    private var _address: SocketAddress? = null
    val address get() = _address ?: error("Address is not initialized")

    @Volatile
    private var _packetListener: PacketListener? = null
    val packetListener get() = _packetListener ?: error("Listener not set")

    val sending get() = receiving.getOpposite()

    @Volatile
    var inboundProtocolInfo: ProtocolInfo<*> = INITIAL_PROTOCOL
        private set

    @Volatile
    var outboundProtocolInfo: ProtocolInfo<*> = INITIAL_PROTOCOL
        private set

    @Volatile
    private var sendLoginDisconnect = true

    var preparing = true
        private set

    private var disconnectionHandled = false

    private var packetsReceived = 0
    override var receivedPackets = 0
        private set
    override var sentPackets = 0
        private set
    override var averageReceivedPackets = 0f
        private set
    override var averageSentPackets = 0f
        private set

    override var latency: Int = 0

    private var handlingFault = false

    /**
     *  The hostname of the connection (address:port)
     */
    override var hostname: String = ""

    override lateinit var virtualHost: InetSocketAddress

    var isPending: Boolean = true

    var stopReadingPackets = false
        private set

    var disconnectionDetails: DisconnectionDetails? = null
        private set

    var encrypted: Boolean = false
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

        setupEncryption()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        disconnect(DisconnectReason.END_OF_STREAM)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, e: Throwable?) {
        if (e is DecoderException && e.cause is NotSslRecordException) {
            ctx.close()
            return
        }

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
                    disconnect(DisconnectReason.TIMEOUT)
                } else {
                    val reason = "Internal Exception: ${throwable?.message}"
                    val disconnectionDetails =
                        _packetListener?.createDisconnectionInfo(
                            DisconnectReason.INTERNAL_EXCEPTION,
                            reason
                        )
                            ?: DisconnectionDetails(DisconnectReason.INTERNAL_EXCEPTION, reason)

                    if (previousHandlingFault) {
                        log.atFine().withCause(throwable).log("Failed to sent packet")

                        val doesDisconnectExist =
                            _packetListener?.protocol != ConnectionProtocol.INITIALIZE && _packetListener?.protocol != ConnectionProtocol.HANDSHAKING

                        if (sending == PacketFlow.CLIENTBOUND && doesDisconnectExist) {
                            val packet = if (sendLoginDisconnect) ClientboundLoginDisconnectPacket(
                                disconnectionDetails
                            ) else ClientboundDisconnectPacket(disconnectionDetails)

                            ConnectionManagementScope.launch {
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

    override fun channelRead0(ctx: ChannelHandlerContext, msg: NettyPacket) {
        if (!channel.isOpen) return

        val packetListener = _packetListener
        check(packetListener != null) { "Received a packet before the packet listener was initialized" }

        if (stopReadingPackets) return
        if (!packetListener.shouldHandleMessage(msg)) return

        this.receivedPackets++
        handlePacket(ctx, msg)
    }

    private object PacketModeCache : ClassValue<PacketHandlerMode>() {
        override fun computeValue(type: Class<*>): PacketHandlerMode {
            val annotation = type.getAnnotation(SurfNettyPacket::class.java)
            return annotation?.handlerMode ?: DEFAULT
        }
    }

    private fun resolvePacketMode(packet: NettyPacket): PacketHandlerMode {
        return PacketModeCache.get(packet.javaClass)
    }

    fun handlePacket(msg: NettyPacket) {
        handlePacket(channel.pipeline().context(this), msg)
    }

    fun handlePacket(ctx: ChannelHandlerContext, msg: NettyPacket) {
        val packetMode = resolvePacketMode(msg)
        when (packetMode) {
            NETTY -> {
                try {
                    if (!handleInternalPacket(packetListener, msg)) {
                        dispatchPluginListeners(ctx, msg)
                    }
                } catch (e: Throwable) {
                    handlePacketHandleException(ctx, msg, e)
                }
            }

            DEFAULT, INHERIT -> {
                PacketHandlerScope.launch {
                    try {
                        if (!handleInternalPacket(packetListener, msg)) {
                            dispatchPluginListeners(ctx, msg)
                        }
                    } catch (e: Throwable) {
                        handlePacketHandleException(ctx, msg, e)
                    }
                }
            }

            IO -> {
                PacketHandlerIoScope.launch {
                    try {
                        if (!handleInternalPacket(packetListener, msg)) {
                            dispatchPluginListeners(ctx, msg)
                        }
                    } catch (e: Throwable) {
                        handlePacketHandleException(ctx, msg, e)
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleInternalPacket(listener: PacketListener, msg: NettyPacket): Boolean {
        if (msg !is InternalNettyPacket<*>) return false
        msg as InternalNettyPacket<PacketListener>
        msg.handle(listener)

        return true
    }

    private fun dispatchPluginListeners(ctx: ChannelHandlerContext, msg: NettyPacket) {
        val protocol = inboundProtocolInfo.id
        if (protocol != ConnectionProtocol.RUNNING && protocol != ConnectionProtocol.SYNCHRONIZING) {
            error("Unexpected packet: " + msg::class.simpleName + " in protocol " + protocol)
        }

        val info = NettyPacketInfo(this, protocol)

        NettyListenerRegistry.dispatch(
            ctx.channel(),
            msg,
            info
        ) { e, listener ->
            log.atWarning()
                .withCause(e)
                .log(
                    "Failed to call listener %s for packet %s",
                    listener.owner::class.simpleName,
                    msg::class.simpleName
                )
        }
    }

    private fun handlePacketHandleException(
        ctx: ChannelHandlerContext,
        msg: NettyPacket,
        e: Throwable
    ) {
        if (e is Error) return exceptionCaught(ctx, e)

        log.atWarning()
            .withCause(e)
            .log("Exception while handling packet %s", msg::class.simpleName)
    }

    private fun validateListener(protocolInfo: ProtocolInfo<*>, listener: PacketListener) {
        check(listener.flow == this.receiving) { "Trying to set listener for wrong side: connection is $receiving, but listener is ${listener.flow}" }
        check(protocolInfo.id == listener.protocol) { "Listener protocol (${listener.protocol}) does not match requested one (${protocolInfo})" }
    }

    suspend fun <T : PacketListener> setupInboundProtocol(newState: ProtocolInfo<T>, listener: T) {
        validateListener(newState, listener)
        check(newState.flow == receiving) { "Invalid inbound protocol: ${newState.flow}" }

        this._packetListener = listener

        var protocol = UnconfiguredPipelineHandler.setupInboundProtocol(newState)
        val bundlerInfo = newState.bundlerInfo

        if (bundlerInfo != null) {
            protocol += {
                it.pipeline().addAfter(
                    HandlerNames.DECODER,
                    HandlerNames.BUNDLER,
                    PacketBundlePacker(bundlerInfo)
                )
            }
        }

        protocol += { this.inboundProtocolInfo = newState }

        syncAfterConfigurationChange(channel.writeAndFlush(protocol))
    }

    suspend fun setupOutboundProtocol(newState: ProtocolInfo<*>) {
        check(newState.flow == sending) { "Invalid outbound protocol: ${newState.flow}" }

        var protocol = UnconfiguredPipelineHandler.setupOutboundProtocol(newState)
        val bundlerInfo = newState.bundlerInfo

        if (bundlerInfo != null) {
            protocol += {
                it.pipeline().addAfter(
                    HandlerNames.ENCODER,
                    HandlerNames.UNBUNDLER,
                    PacketBundleUnpacker(bundlerInfo)
                )
            }
        }

        val login = newState.id == ConnectionProtocol.LOGIN
        protocol += { this.sendLoginDisconnect = login }
        protocol += { this.outboundProtocolInfo = newState }

        syncAfterConfigurationChange(channel.writeAndFlush(protocol))
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
            sendSuspendPacket(
                ServerboundHandshakePacket(
                    PROTOCOL_VERSION,
                    hostname,
                    port,
                    intention
                ), flush = true
            )
            setupOutboundProtocol(serverboundProtocolInfo)
        }
    }

    override fun send(packet: NettyPacket, flush: Boolean) {
        internalSend(packet, flush)
    }

    override suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        convertExceptions: Boolean
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

    override fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
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
                    || (packet.isReady() && pendingActions.isEmpty() && packet.extraPackets.isNullOrEmpty()))
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
                    add(PacketSendAction(packet, false, null))
                    extraPackets.forEachIndexed { index, extraPacket ->
                        add(
                            PacketSendAction(
                                extraPacket,
                                index == extraPackets.size - 1,
                                if (index == extraPackets.size - 1) deferred else null
                            )
                        )
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
        this.sentPackets++
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
            disconnect(DisconnectionDetails(DisconnectReason.INTERNAL_EXCEPTION, e.message))
        }
    }

    fun runOnceConnected(block: ConnectionImpl.() -> Unit) {
        if (connected) {
            flushQueue()
            block(this)
        } else {
            pendingActions.add(WrappedConsumer(block))
        }
    }

    suspend fun runOnceConnectedSuspend(block: suspend ConnectionImpl.() -> Unit) {
        if (connected) {
            flushQueue()
            block(this)
        } else {
            val deferred = CompletableDeferred<Unit>()
            pendingActions.add(WrappedConsumer {
                ConnectionManagementScope.launch {
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
        val channel = _channel ?: return false
        val eventLoop = channel.eventLoop()
        if (eventLoop.inEventLoop()) {
            drainQueueOnEventLoop()
        } else {
            eventLoop.execute {
                drainQueueOnEventLoop()
            }
        }

        return true
    }

    private fun drainQueueOnEventLoop() {
        // disable parallel drains
        if (!drainingActions.compareAndSet(false, true)) return

        try {
            while (true) {
                val head = pendingActions.peek() ?: break

                // Already consumed? Then throw out and next round
                if (head.isConsumed()) {
                    pendingActions.poll()
                    continue
                }

                // Head blocked? Then do not remove and stop processing
                if (head is PacketSendAction && !head.packet.isReady()) {
                    break
                }

                // Jetzt darf verarbeitet werden
                val queued = pendingActions.poll() ?: continue
                if (queued.tryMarkConsumed()) {
                    queued.consumer(this)
                }
            }
        } finally {
            drainingActions.set(false)
            // If new elements came in during the drain, trigger again
            if (!pendingActions.isEmpty()) {
                // We are already in the EventLoop thread - Prevent reentrancy:
                if (drainingActions.compareAndSet(false, true)) {
                    try {
                        // short second pass
                        while (true) {
                            val head = pendingActions.peek() ?: break
                            if (head is PacketSendAction && !head.packet.isReady()) break
                            val queued = pendingActions.poll() ?: continue
                            if (queued.tryMarkConsumed()) queued.consumer(this)
                        }
                    } finally {
                        drainingActions.set(false)
                    }
                } else {
                    // Another thread is already draining - it will pick it up
                }
            }
        }
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


        this.averageSentPackets = lerp(
            0.75f, this.sentPackets.toFloat(),
            this.averageSentPackets
        )
        this.averageReceivedPackets = lerp(
            0.75f, this.receivedPackets.toFloat(),
            this.averageReceivedPackets
        )
        this.sentPackets = 0
        this.receivedPackets = 0
    }

    fun disconnect(reason: DisconnectReason) {
        disconnect(DisconnectionDetails(reason))
    }

    fun disconnect(reason: DisconnectionDetails): ChannelFuture? {
        preparing = false
        clearPacketQueue()

        val channel = _channel

        if (channel == null) {
            this.delayedDisconnect = reason
            return null
        }

        if (connected) {
            val future = channel.close()
            this.disconnectionDetails = reason
            return future
        }

        return null
    }

    fun clearPacketQueue() {
        pendingActions.clear()
    }

    fun configurePacketHandler(channel: Channel, pipeline: ChannelPipeline) {
        pipeline.addLast("hackfix", object : ChannelOutboundHandlerAdapter() {
            override fun write(
                ctx: ChannelHandlerContext?,
                msg: Any?,
                promise: ChannelPromise?
            ) {
                super.write(ctx, msg, promise)
            }
        }).addLast(HandlerNames.PACKET_HANDLER, this)

        channel.attr(CHANNEL_ATTRIBUTE_KEY).set(this)
    }

    fun setupEncryption() {
        encryptionManager.setupEncryption(channel)
    }

    fun setReadOnly() {
        _channel?.config()?.isAutoRead = false
    }

    fun enableAutoRead() {
        _channel?.config()?.isAutoRead = true
    }

    suspend fun handleDisconnection() {
        val channel = _channel ?: return
        if (channel.isOpen) return
        if (disconnectionHandled) return

        disconnectionHandled = true
        _packetListener?.onDisconnect(
            disconnectionDetails ?: DisconnectionDetails(DisconnectReason.UNKNOWN)
        )

        clearPacketQueue()
    }

    override fun getLoggableAddress() =
        getLoggableAddress(bean<AbstractSurfCloudConfigHolder<*>>().config.logging.logIps)

    fun getLoggableAddress(logIps: Boolean) =
        if (_address == null) "local" else (if (logIps) _address.toString() else "IP hidden")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConnectionImpl) return false

        if (_channel != other._channel) return false
        if (receiving != other.receiving) return false
        if (_address != other._address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = receiving.hashCode()
        result = 31 * result + (_channel?.hashCode() ?: 0)
        result = 31 * result + (_address?.hashCode() ?: 0)
        return result
    }


    private object Util {
        fun canSendImmediate(connection: ConnectionImpl, packet: NettyPacket): Boolean {
            return connection.isPending || connection._packetListener?.protocol != ConnectionProtocol.RUNNING
                    // region Clientbound
                    || packet is ClientboundBundlePacket
                    || packet is ClientboundClearResourcePacksPacket
                    || packet is ClientboundClearTitlePacket
                    || packet is ClientboundDisconnectPacket
                    || packet is ClientboundHideBossBarPacket
                    || packet is KeepAlivePacket
                    || packet is ClientboundOpenBookPacket
                    || packet is ClientboundPingPacket
                    || packet is ClientboundPlaySoundPacket
                    || packet is ClientboundPongResponsePacket
                    || packet is ClientboundRemoveResourcePacksPacket
                    || packet is ClientboundResetTitlePacket
                    || packet is ClientboundSendActionBarPacket
                    || packet is ClientboundSendMessagePacket
                    || packet is ClientboundSendPlayerListHeaderAndFooterPacket
                    || packet is ClientboundSendResourcePacksPacket
                    || packet is ClientboundSendTitlePartPacket
                    || packet is ClientboundShowBossBarPacket
                    || packet is ClientboundShowTitlePacket
                    || packet is ClientboundStopSoundPacket
                    // endregion
                    // region Bidirectional
                    || packet is PlayerConnectToServerPacket
                    || packet is PlayerDisconnectFromServerPacket
                    // endregion
                    // region Serverbound
                    || packet is ServerboundBundlePacket
                    || packet is ServerboundClearResourcePacksPacket
                    || packet is ServerboundClearTitlePacket
                    || packet is ServerboundClientInformationPacket
                    || packet is ServerboundHideBossBarPacket
                    || packet is ServerboundOpenBookPacket
                    || packet is ServerboundPingRequestPacket
                    || packet is ServerboundPlaySoundPacket
                    || packet is ServerboundPongPacket
                    || packet is ServerboundRemoveResourcePacksPacket
                    || packet is ServerboundResetTitlePacket
                    || packet is ServerboundSendActionBarPacket
                    || packet is ServerboundSendMessagePacket
                    || packet is ServerboundSendPlayerListHeaderAndFooterPacket
                    || packet is ServerboundSendResourcePacksPacket
                    || packet is ServerboundSendTitlePartPacket
                    || packet is ServerboundShowBossBarPacket
                    || packet is ServerboundShowTitlePacket
                    || packet is ServerboundStopSoundPacket
            // endregion
        }

        fun buildExtraPackets(packet: NettyPacket): List<NettyPacket>? {
            val extra = packet.extraPackets ?: return null
            if (extra.isEmpty()) return null

            return buildList(extra.size + 1) { buildExtraPackets0(extra) }
        }

        private fun MutableList<NettyPacket>.buildExtraPackets0(extraPackets: List<NettyPacket>) {
            for (extra in extraPackets) {
                add(extra)
                val extraExtra = extra.extraPackets

                if (!extraExtra.isNullOrEmpty()) buildExtraPackets0(extraExtra)
            }
        }
    }

    private open class WrappedConsumer(val consumer: (ConnectionImpl) -> Unit) {
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

        val NETWORK_WORKER_GROUP: MultiThreadIoEventLoopGroup by lazy {
            MultiThreadIoEventLoopGroup(
                threadFactory {
                    nameFormat("Netty Client IO #%d")
                    daemon(true)
                    uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
                }, NioIoHandler.newFactory()
            )
        }

        val NETWORK_EPOLL_WORKER_GROUP: MultiThreadIoEventLoopGroup by lazy {
            MultiThreadIoEventLoopGroup(threadFactory {
                nameFormat("Netty Epoll Client IO #%d")
                daemon(true)
                uncaughtExceptionHandler(DefaultUncaughtExceptionHandlerWithName(log))
            }, EpollIoHandler.newFactory())
        }

        private val INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND
        val CHANNEL_ATTRIBUTE_KEY: AttributeKey<ConnectionImpl> =
            AttributeKey.newInstance("connection")

        suspend fun connect(
            address: InetSocketAddress,
            useEpoll: Boolean,
            connection: ConnectionImpl,
            configHolder: AbstractSurfCloudConfigHolder<*>
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

                        configureSerialization(
                            pipeline,
                            PacketFlow.CLIENTBOUND,
                            false,
                            configHolder
                        )
                        connection.configurePacketHandler(channel, pipeline)
                    }
                })
                .connect(address.address, address.port)
                .suspend()
        }

        fun configureSerialization(
            pipeline: ChannelPipeline,
            side: PacketFlow,
            local: Boolean,
            configHolder: AbstractSurfCloudConfigHolder<*>
        ) {
            val opposite = side.getOpposite()
            val receivingSide = side == PacketFlow.SERVERBOUND
            val sendingSide = opposite == PacketFlow.SERVERBOUND


            pipeline.addFirst(
                HandlerNames.LOGGER,
                LoggingHandler(configHolder.config.logging.nettyLogLevel)
            )
//                .addLast(HandlerNames.SSL_HANDLER_ENFORCER, EnforceSslHandler())
                .addLast(HandlerNames.COMPRESS, ZstdEncoder(8))
                .addLast(HandlerNames.DECOMPRESS, ZstdDecoder())
                .addLast(HandlerNames.SPLITTER, createFrameDecoder(local))
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
                .addLast(HandlerNames.RESPONDING_PACKET_SEND, RespondingPacketSendHandler())
        }

        private fun createFrameEncoder(local: Boolean) =
            if (local) NoOpFrameEncoder() else Varint21LengthFieldPrepender()

        private fun createFrameDecoder(local: Boolean) =
            if (local) NoOpFrameDecoder() else Varint21FrameDecoder()

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