package dev.slne.surf.cloud.core.common.netty.network.connection

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.netty.network.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.HandshakeProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ClientInitializePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientLoginPacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.*
import io.netty.handler.codec.compression.ZstdDecoder
import io.netty.handler.codec.compression.ZstdEncoder
import io.netty.handler.flow.FlowControlHandler
import io.netty.handler.flush.FlushConsolidationHandler
import io.netty.handler.logging.LoggingHandler
import io.netty.util.AttributeKey
import kotlinx.coroutines.CompletableDeferred
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.atomic.AtomicBoolean

class ConnectionImpl(
    val receiving: PacketFlow,
    val encryptionManager: EncryptionManager,
    private val wire: PacketWire
) : Connection {
    val log = logger()

    internal val actionQueue = NetworkActionQueue(this)
    internal val protocolSwitcher = ProtocolSwitcher(this)
    internal val networkPacketHandler = NetworkPacketHandler(this)
    internal val networkPacketSendHandler = NetworkPacketSendHandler(this)
    internal val stats = ConnectionStats(this)

    internal var _channel: Channel? = null
    val channel get() = checkNotNull(_channel) { "Channel is not initialized" }

    internal var _address: SocketAddress? = null
    val address get() = checkNotNull(_address) { "Address is not initialized" }

    @Volatile
    internal var _packetListener: PacketListener? = null
    val packetListener get() = checkNotNull(_packetListener) { "Packet listener is not initialized" }

    @Volatile
    internal var handler: ChannelInboundHandler = UnconfiguredPipelineHandler.Inbound()
        private set

    val sending get() = receiving.getOpposite()

    @Volatile
    var inboundProtocolInfo: ProtocolInfo<*> = INITIAL_PROTOCOL
        internal set

    @Volatile
    var outboundProtocolInfo: ProtocolInfo<*> = INITIAL_PROTOCOL
        internal set

    @Volatile
    internal var sendLoginDisconnect = true

    var preparing = true
        internal set

    private val disconnectionHandled = AtomicBoolean(false)

    private val packetsReceived by stats::packetsReceived
    override val receivedPackets by stats::receivedPackets
    override val sentPackets by stats::sentPackets
    override val averageReceivedPackets by stats::averageReceivedPackets
    override val averageSentPackets by stats::averageSentPackets

    override var latency by stats::latency

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
    internal var delayedDisconnect: DisconnectionDetails? = null
        private set

    val connected get() = _channel != null && _channel!!.isOpen
    val connecting get() = _channel == null

    override val client: NettyClient?
        get() = (_packetListener as? CommonPacketListener)?.client

    fun handlePacket(msg: NettyPacket) {
        networkPacketHandler.handlePacket(msg)
    }

    fun setListenerForServerboundHandshake(packetListener: PacketListener) {
        check(this._packetListener == null) { "Listener already set" }
        check(receiving == PacketFlow.SERVERBOUND && packetListener.flow == PacketFlow.SERVERBOUND && packetListener.protocol == INITIAL_PROTOCOL.id) { "Invalid initial listener" }
        this._packetListener = packetListener
    }

    fun <T : PacketListener> setupInboundProtocol(
        newState: ProtocolInfo<T>,
        listener: T
    ): ChannelFuture = protocolSwitcher.setupInboundProtocol(newState, listener)

    fun <T : PacketListener> setupOutboundProtocol(
        newState: ProtocolInfo<T>,
    ) = protocolSwitcher.setupOutboundProtocol(newState)

    fun initiateServerboundInitializeConnection(
        hostname: String,
        port: Int,
        listener: ClientInitializePacketListener
    ) = protocolSwitcher.initiateServerboundInitializeConnection(hostname, port, listener)

    fun initiateServerboundRunningConnection(
        hostname: String,
        port: Int,
        listener: ClientLoginPacketListener
    ) = protocolSwitcher.initiateServerboundRunningConnection(hostname, port, listener)

    fun <S : ServerboundPacketListener, C : ClientboundPacketListener> initiateServerboundRunningConnection(
        hostname: String,
        port: Int,
        serverboundProtocolInfo: ProtocolInfo<S>,
        clientboundProtocolInfo: ProtocolInfo<C>,
        clientboundListener: C,
        initialze: Boolean
    ) = protocolSwitcher.initiateServerboundRunningConnection(
        hostname,
        port,
        serverboundProtocolInfo,
        clientboundProtocolInfo,
        clientboundListener,
        initialze
    )

    override fun send(packet: NettyPacket, flush: Boolean) {
        wire.send(packet, flush)
    }

    override suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        convertExceptions: Boolean
    ): Boolean {
        return wire.sendWithIndication(packet, flush, convertExceptions)
    }

    override fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>
    ) {
        wire.sendWithIndication(packet, flush, deferred)
    }

    fun internalSend(
        packet: NettyPacket,
        flush: Boolean = true,
        deferred: CompletableDeferred<Boolean>? = null
    ) = actionQueue.internalSend(packet, flush, deferred)


    internal fun sendPacketNow(
        packet: NettyPacket,
        flush: Boolean = true,
        deferred: CompletableDeferred<Boolean>? = null,
        sendListener: ChannelFutureListener? = null
    ) = networkPacketSendHandler.sendPacketNow(packet, flush, deferred, sendListener)


    fun flushChannel() = actionQueue.runOnceConnected {
        flushChannelNow()
    }

    internal fun flushChannelNow() {
        val channel = _channel ?: return
        val eventLoop = channel.eventLoop()

        if (eventLoop.inEventLoop()) {
            channel.flush()
        } else {
            eventLoop.execute { channel.flush() }
        }
    }

    fun tick(clearActionQueueOnDisconnect: Boolean) {
        actionQueue.flushQueue()

        val packetListener = _packetListener
        if (packetListener is TickablePacketListener) {
            packetListener.tickSecond()
        }

        if (!connected && !disconnectionHandled.get()) {
            handleDisconnection(clearActionQueueOnDisconnect)
        }

        stats.recalculateAverages()
    }

    fun disconnect(reason: DisconnectReason) {
        disconnect(DisconnectionDetails(reason))
    }

    fun disconnect(reason: DisconnectionDetails): ChannelFuture? {
        preparing = false
        actionQueue.clear()

        val channel = _channel
        _channel = null

        if (channel == null) {
            this.delayedDisconnect = reason
            return null
        }

        if (channel.isOpen) {
            val future = channel.close()
            this.disconnectionDetails = reason
            return future
        }

        return null
    }

    fun configurePacketHandler(channel: Channel, pipeline: ChannelPipeline) {
        val handler = ConnectionPipelineHandler(this)
        this.handler = handler

        pipeline.addLast("hackfix", object : ChannelOutboundHandlerAdapter() {
            override fun write(
                ctx: ChannelHandlerContext?,
                msg: Any?,
                promise: ChannelPromise?
            ) {
                super.write(ctx, msg, promise)
            }
        }).addLast(HandlerNames.PACKET_HANDLER, handler)

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

    fun handleDisconnection(clearActionQueue: Boolean): Boolean {
        val channel = _channel ?: return false
        if (channel.isOpen) return false
        if (!disconnectionHandled.compareAndSet(false, true)) return false

        _packetListener?.onDisconnect(
            disconnectionDetails ?: DisconnectionDetails(DisconnectReason.UNKNOWN)
        )

        if (clearActionQueue) {
            actionQueue.clear()
        }

        return true
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
    }

    companion object {
        private val log = logger()
        private val INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND
        val CHANNEL_ATTRIBUTE_KEY: AttributeKey<ConnectionImpl> =
            AttributeKey.newInstance("connection")

        fun configureSerialization(
            pipeline: ChannelPipeline,
            side: PacketFlow,
            local: Boolean,
            configHolder: AbstractSurfCloudConfigHolder<*>
        ) {
            val opposite = side.getOpposite()
            val receivingSide = side == PacketFlow.SERVERBOUND
            val sendingSide = opposite == PacketFlow.SERVERBOUND


            pipeline
                .addFirst(FlushConsolidationHandler())
                .addFirst(HandlerNames.LOGGER, createLoggingHandler(configHolder))
//                .addLast(HandlerNames.SSL_HANDLER_ENFORCER, EnforceSslHandler())
                .addLast(HandlerNames.COMPRESS, ZstdEncoder(4))
                .addLast(HandlerNames.DECOMPRESS, ZstdDecoder())
                .addLast(HandlerNames.SPLITTER, createFrameDecoder(local))
                .addLast(FlowControlHandler())
                .addLast(inboundHandlerName(receivingSide), createInboundHandler(receivingSide))
                .addLast(HandlerNames.PREPENDER, createFrameEncoder(local))
                .addLast(outboundHandlerName(sendingSide), createOutboundHandler(sendingSide))
                .addLast(HandlerNames.RESPONDING_PACKET_SEND, RespondingPacketSendHandler())
        }

        private fun createLoggingHandler(configHolder: AbstractSurfCloudConfigHolder<*>) =
            LoggingHandler(configHolder.config.logging.nettyLogLevel)

        private fun createFrameEncoder(local: Boolean) =
            if (local) NoOpFrameEncoder() else Varint21LengthFieldPrepender()

        private fun createFrameDecoder(local: Boolean) =
            if (local) NoOpFrameDecoder() else Varint21FrameDecoder()

        private fun outboundHandlerName(sendingSide: Boolean) =
            if (sendingSide) HandlerNames.ENCODER else HandlerNames.OUTBOUND_CONFIG

        private fun createOutboundHandler(sendingSide: Boolean) =
            if (sendingSide) PacketEncoder(INITIAL_PROTOCOL) else UnconfiguredPipelineHandler.Outbound()

        private fun inboundHandlerName(receivingSide: Boolean) =
            if (receivingSide) HandlerNames.DECODER else HandlerNames.INBOUND_CONFIG

        private fun createInboundHandler(receivingSide: Boolean) =
            if (receivingSide) PacketDecoder(INITIAL_PROTOCOL) else UnconfiguredPipelineHandler.Inbound()
    }
}