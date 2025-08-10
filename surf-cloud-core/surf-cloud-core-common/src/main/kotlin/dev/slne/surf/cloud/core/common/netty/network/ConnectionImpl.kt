package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.exceptions.SkipPacketException
import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.util.DefaultUncaughtExceptionHandlerWithName
import dev.slne.surf.cloud.api.common.util.math.lerp
import dev.slne.surf.cloud.api.common.util.netty.suspend
import dev.slne.surf.cloud.api.common.util.threadFactory
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ClientIntent
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.HandshakeProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ServerHandshakePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.handshake.ServerboundHandshakePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.prerunning.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.*
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
import kotlinx.coroutines.withContext
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
        PacketHandlerScope.launch {
            runCatching {
                handlePacket(msg)
            }.onFailure {
                exceptionCaught(ctx, it)
            }
        }
    }

    suspend fun handlePacket(msg: NettyPacket) = withContext(PacketHandlerScope.coroutineContext) {
        val listener = _packetListener ?: return@withContext

        when (listener) {
            is ServerboundPacketListener -> {
                if (listener is ServerCommonPacketListener) {
                    val handled = when (msg) {
                        is ServerboundBundlePacket -> listener.handleBundlePacket(msg)
                        is KeepAlivePacket -> listener.handleKeepAlivePacket(msg)
                        is ServerboundPingRequestPacket -> listener.handlePingRequest(msg)
                        is ResponseNettyPacket -> {}

                        else -> null
                    }

                    if (handled != null) return@withContext
                }

                when (listener) {
                    is ServerHandshakePacketListener -> when (msg) {
                        is ServerboundHandshakePacket -> listener.handleHandshake(msg)
                        else -> error("Unexpected packet $msg")
                    }

                    is ServerInitializePacketListener -> when (msg) {
                        is ServerboundInitializeRequestIdPacket -> listener.handleIdRequest(msg)

                        else -> error("Unexpected packet $msg")
                    }

                    is ServerLoginPacketListener -> when (msg) {
                        is ServerboundLoginStartPacket -> listener.handleLoginStart(msg)
                        is ServerboundLoginAcknowledgedPacket -> listener.handleLoginAcknowledgement(
                            msg
                        )

                        else -> error("Unexpected packet $msg")
                    }

                    is ServerPreRunningPacketListener -> when (msg) {
                        is ServerboundProceedToSynchronizingAcknowledgedPacket -> listener.handleReadyToRun(
                            msg
                        )

                        is ServerboundPreRunningAcknowledgedPacket -> listener.handlePreRunningAcknowledged(
                            msg
                        )

                        is ServerboundRequestContinuation -> listener.handleRequestContinuation(msg)

                        else -> error("Unexpected packet $msg")
                    }

                    is ServerSynchronizingPacketListener -> when (msg) {
                        is FinishSynchronizingPacket -> listener.handleFinishSynchronizing(msg)
                        is ServerboundSynchronizeFinishAcknowledgedPacket -> listener.handleSynchronizeFinishAcknowledged(
                            msg
                        )

                        is SyncValueChangePacket -> listener.handleSyncValueChange(msg)
                        is SyncSetDeltaPacket -> listener.handleSyncSetDelta(msg)
                        is ServerboundCreateOfflineCloudPlayerIfNotExistsPacket -> listener.handleCreateOfflineCloudPlayerIfNotExists(
                            msg
                        )

                        else -> listener.handlePacket(msg)
                    }

                    is RunningServerPacketListener -> when (msg) {
                        is PlayerConnectToServerPacket -> listener.handlePlayerConnectToServer(
                            msg
                        )

                        is PlayerDisconnectFromServerPacket -> listener.handlePlayerDisconnectFromServer(
                            msg
                        )

                        is ServerboundSendResourcePacksPacket -> listener.handleSendResourcePacks(
                            msg
                        )

                        is ServerboundClearResourcePacksPacket -> listener.handleClearResourcePacks(
                            msg
                        )

                        is ServerboundRemoveResourcePacksPacket -> listener.handleRemoveResourcePacks(
                            msg
                        )

                        is ServerboundShowTitlePacket -> listener.handleShowTitle(msg)
                        is ServerboundSendTitlePartPacket -> listener.handleSendTitlePart(msg)
                        is ServerboundClearTitlePacket -> listener.handleClearTitle(msg)
                        is ServerboundResetTitlePacket -> listener.handleResetTitle(msg)
                        is ServerboundShowBossBarPacket -> listener.handleShowBossBar(msg)
                        is ServerboundHideBossBarPacket -> listener.handleHideBossBar(msg)
                        is ServerboundOpenBookPacket -> listener.handleOpenBook(msg)
                        is ServerboundPlaySoundPacket -> listener.handlePlaySound(msg)
                        is ServerboundStopSoundPacket -> listener.handleStopSound(msg)
                        is ServerboundSendMessagePacket -> listener.handleSendMessage(msg)
                        is ServerboundSendActionBarPacket -> listener.handleSendActionBar(msg)
                        is ServerboundSendPlayerListHeaderAndFooterPacket -> listener.handleSendPlayerListHeaderAndFooter(
                            msg
                        )

                        is ServerboundRequestDisplayNamePacket -> listener.handleRequestDisplayName(
                            msg
                        )

                        is RequestOfflineDisplayNamePacket -> listener.handleRequestOfflinePlayerDisplayName(
                            msg
                        )

                        is ServerboundClientInformationPacket -> listener.handleClientInformation(
                            msg
                        )

                        is RequestLuckpermsMetaDataPacket -> listener.handleRequestLuckpermsMetaData(
                            msg
                        )

                        is ServerboundRequestPlayerPersistentDataContainer -> listener.handleRequestPlayerPersistentDataContainer(
                            msg
                        )

                        is ServerboundPlayerPersistentDataContainerUpdatePacket -> listener.handlePlayerPersistentDataContainerUpdate(
                            msg
                        )

                        is ServerboundConnectPlayerToServerPacket -> listener.handleConnectPlayerToServer(
                            msg
                        )

                        is ServerboundQueuePlayerToGroupPacket -> listener.handleQueuePlayerToGroup(
                            msg
                        )

                        is DisconnectPlayerPacket -> listener.handleDisconnectPlayer(msg)
                        is SilentDisconnectPlayerPacket -> listener.handleSilentDisconnectPlayer(msg)

                        is TeleportPlayerPacket -> listener.handleTeleportPlayer(msg)
                        is TeleportPlayerToPlayerPacket -> listener.handleTeleportPlayerToPlayer(msg)
                        is ServerboundShutdownServerPacket -> listener.handleShutdownServer(msg)
                        is ServerboundRequestPlayerDataPacket -> listener.handleRequestPlayerData(
                            msg
                        )

                        is UpdateAFKStatePacket -> listener.handleUpdateAFKState(msg)
                        is ServerboundGeneratePunishmentIdPacket -> listener.handleGeneratePunishmentId(
                            msg
                        )

                        is ServerboundCreateKickPacket -> listener.handleCreateKick(msg)
                        is ServerboundCreateWarnPacket -> listener.handleCreateWarn(msg)
                        is ServerboundCreateMutePacket -> listener.handleCreateMute(msg)
                        is ServerboundCreateBanPacket -> listener.handleCreateBan(msg)
                        is ServerboundAttachIpAddressToBanPacket -> listener.handleAttachIpAddressToBan(
                            msg
                        )

                        is ServerboundAttachNoteToPunishmentPacket -> listener.handleAttachNoteToPunishment(
                            msg
                        )

                        is ServerboundFetchNotesFromPunishmentPacket -> listener.handleFetchNotesFromPunishment(
                            msg
                        )

                        is ServerboundFetchMutesPacket -> listener.handleFetchMutes(msg)
                        is ServerboundFetchBansPacket -> listener.handleFetchBans(msg)
                        is ServerboundFetchKicksPacket -> listener.handleFetchKicks(msg)
                        is ServerboundFetchWarnsPacket -> listener.handleFetchWarns(msg)
                        is ServerboundGetCurrentLoginValidationPunishmentCachePacket -> listener.handleGetCurrentLoginValidationPunishmentCache(
                            msg
                        )

                        is ServerboundFetchIpAddressesForBanPacket -> listener.handleFetchIpAddressesForBan(
                            msg
                        )

                        is ServerboundFetchIpBansPacket -> listener.handleFetchIpBans(msg)
                        is RequestPlayerPermissionPacket -> listener.handleRequestPlayerPermission(
                            msg
                        )

                        is SyncValueChangePacket -> listener.handleSyncValueChange(msg)
                        is SyncSetDeltaPacket -> listener.handleSyncSetDelta(msg)
                        is ServerboundCreateOfflineCloudPlayerIfNotExistsPacket -> listener.handleCreateOfflineCloudPlayerIfNotExists(
                            msg
                        )

                        is ServerboundRequestWhitelistStatusPacket -> listener.handleRequestWhitelistStatus(
                            msg
                        )

                        is ServerboundRequestWhitelistPacket -> listener.handleRequestWhitelist(msg)
                        is ServerboundCreateWhitelistPacket -> listener.handleCreateWhitelist(msg)
                        is ServerboundUpdateWhitelistPacket -> listener.handleUpdateWhitelist(msg)
                        is ServerboundRefreshWhitelistPacket -> listener.handleRefreshWhitelist(msg)

                        else -> listener.handlePacket(msg) // handle other packets
                    }
                }
            }

            is ClientboundPacketListener -> {
                if (listener is ClientCommonPacketListener) {
                    val handled = when (msg) {
                        is ClientboundBundlePacket -> listener.handleBundlePacket(msg)
                        is KeepAlivePacket -> listener.handleKeepAlive(msg)
                        is ClientboundPingPacket -> listener.handlePing(msg)
                        is ClientboundDisconnectPacket -> listener.handleDisconnect(msg)
                        is ResponseNettyPacket -> {}
                        else -> null
                    }
                    if (handled != null) return@withContext
                }

                when (listener) {
                    is ClientInitializePacketListener -> when (msg) {
                        is ClientboundInitializeIdResponsePacket -> listener.handleIdResponse(
                            msg
                        )

                        else -> error("Unexpected packet $msg")
                    }

                    is ClientLoginPacketListener -> when (msg) {
                        is ClientboundLoginFinishedPacket -> listener.handleLoginFinished(msg)

                        else -> error("Unexpected packet $msg")
                    }

                    is ClientPreRunningPacketListener -> when (msg) {
                        is ClientboundPreRunningFinishedPacket -> listener.handlePreRunningFinished(
                            msg
                        )

                        is ClientboundProceedToSynchronizingPacket -> listener.handleProceedToSynchronizing(
                            msg
                        )

                        else -> error("Unexpected packet $msg")
                    }

                    is ClientSynchronizingPacketListener -> when (msg) {
                        is ClientboundSynchronizeFinishPacket -> listener.handleSynchronizeFinish(
                            msg
                        )

                        is SyncValueChangePacket -> listener.handleSyncValueChange(msg)
                        is ClientboundBatchSyncValuePacket -> listener.handleBatchSyncValue(msg)
                        is ClientboundBatchSyncSetPacket -> listener.handleBatchSyncSet(msg)
                        is ClientboundBatchUpdateServer -> listener.handleBatchUpdateServer(msg)
                        is SyncSetDeltaPacket -> listener.handleSyncSetDelta(msg)
                        is ClientboundSetVelocitySecretPacket -> listener.handleSetVelocitySecret(
                            msg
                        )

                        else -> listener.handlePacket(msg)
                    }

                    is RunningClientPacketListener -> when (msg) {
                        is PlayerConnectedToServerPacket -> listener.handlePlayerConnectedToServer(
                            msg
                        )

                        is PlayerDisconnectFromServerPacket -> listener.handlePlayerDisconnectFromServer(
                            msg
                        )

                        is ClientboundSendResourcePacksPacket -> listener.handleSendResourcePacks(
                            msg
                        )

                        is ClientboundClearResourcePacksPacket -> listener.handleClearResourcePacks(
                            msg
                        )

                        is ClientboundRemoveResourcePacksPacket -> listener.handleRemoveResourcePacks(
                            msg
                        )

                        is ClientboundShowTitlePacket -> listener.handleShowTitle(msg)
                        is ClientboundSendTitlePartPacket -> listener.handleSendTitlePart(msg)
                        is ClientboundClearTitlePacket -> listener.handleClearTitle(msg)
                        is ClientboundResetTitlePacket -> listener.handleResetTitle(msg)
                        is ClientboundShowBossBarPacket -> listener.handleShowBossBar(msg)
                        is ClientboundHideBossBarPacket -> listener.handleHideBossBar(msg)
                        is ClientboundOpenBookPacket -> listener.handleOpenBook(msg)
                        is ClientboundPlaySoundPacket -> listener.handlePlaySound(msg)
                        is ClientboundStopSoundPacket -> listener.handleStopSound(msg)
                        is ClientboundSendMessagePacket -> listener.handleSendMessage(msg)
                        is ClientboundSendActionBarPacket -> listener.handleSendActionBar(msg)
                        is ClientboundSendPlayerListHeaderAndFooterPacket -> listener.handleSendPlayerListHeaderAndFooter(
                            msg
                        )

                        is ClientboundRequestDisplayNamePacket -> listener.handleRequestDisplayName(
                            msg
                        )

                        is RequestOfflineDisplayNamePacket -> listener.handleRequestOfflinePlayerDisplayName(
                            msg
                        )

                        is ClientboundRegisterServerPacket -> listener.handleRegisterServerPacket(
                            msg
                        )

                        is ClientboundUnregisterServerPacket -> listener.handleUnregisterServerPacket(
                            msg
                        )

                        is ClientboundAddPlayerToServerPacket -> listener.handleAddPlayerToServer(
                            msg
                        )

                        is ClientboundRemovePlayerFromServerPacket -> listener.handleRemovePlayerFromServer(
                            msg
                        )

                        is ClientboundUpdateServerInformationPacket -> listener.handleUpdateServerInformation(
                            msg
                        )

                        is ClientboundIsServerManagedByThisProxyPacket -> listener.handleIsServerManagedByThisProxy(
                            msg
                        )

                        is ClientboundTransferPlayerPacket -> listener.handleTransferPlayer(msg)
                        is RequestLuckpermsMetaDataPacket -> listener.handleRequestLuckpermsMetaData(
                            msg
                        )

                        is DisconnectPlayerPacket -> listener.handleDisconnectPlayer(msg)
                        is SilentDisconnectPlayerPacket -> listener.handleSilentDisconnectPlayer(msg)
                        is TeleportPlayerPacket -> listener.handleTeleportPlayer(msg)
                        is TeleportPlayerToPlayerPacket -> listener.handleTeleportPlayerToPlayer(msg)
                        is ClientboundRegisterCloudServersToProxyPacket -> listener.handleRegisterCloudServersToProxy(
                            msg
                        )

                        is ClientboundTriggerShutdownPacket -> listener.handleTriggerShutdown(msg)
                        is UpdateAFKStatePacket -> listener.handleUpdateAFKState(msg)
                        is ClientboundRunPrePlayerJoinTasksPacket -> listener.handleRunPlayerPreJoinTasks(
                            msg
                        )

                        is ClientboundTriggerPunishmentUpdateEventPacket -> listener.handleTriggerPunishmentUpdateEvent(
                            msg
                        )

                        is ClientboundTriggerPunishmentCreatedEventPacket -> listener.handleTriggerPunishmentCreatedEvent(
                            msg
                        )

                        is RequestPlayerPermissionPacket -> listener.handleRequestPlayerPermission(
                            msg
                        )

                        is SyncValueChangePacket -> listener.handleSyncValueChange(msg)
                        is SyncSetDeltaPacket -> listener.handleSyncSetDelta(msg)
                        is ClientboundSetVelocitySecretPacket -> listener.handleSetVelocitySecret(
                            msg
                        )

                        else -> listener.handlePacket(msg)
                    }
                }
            }

            else -> error("Invalid packet listener")
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
            sendSuspendPacket(ServerboundHandshakePacket(hostname, port, intention), flush = true)
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

        synchronized(this.pendingActions) {
            return this.processQueue()
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
            connection: ConnectionImpl
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
                        connection.configurePacketHandler(channel, pipeline)
                    }
                })
                .connect(address.address, address.port)
                .suspend()
        }

        fun configureSerialization(pipeline: ChannelPipeline, side: PacketFlow, local: Boolean) {
            val opposite = side.getOpposite()
            val receivingSide = side == PacketFlow.SERVERBOUND
            val sendingSide = opposite == PacketFlow.SERVERBOUND


            pipeline.addFirst(
                HandlerNames.LOGGER,
                LoggingHandler(bean<AbstractSurfCloudConfigHolder<*>>().config.logging.nettyLogLevel)
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