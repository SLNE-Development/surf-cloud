package dev.slne.surf.cloud.core.client.netty.state

import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.util.DefaultUncaughtExceptionHandlerWithName
import dev.slne.surf.cloud.api.common.util.threadFactory
import dev.slne.surf.cloud.core.client.config.ClientConfigHolder
import dev.slne.surf.cloud.core.client.netty.ClientChannelHandler
import dev.slne.surf.cloud.core.client.netty.ClientNettyClientImpl
import dev.slne.surf.cloud.core.client.netty.network.ClientCommonPacketListenerImpl
import dev.slne.surf.cloud.core.client.netty.network.ClientHandshakePacketListenerImpl
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.network.*
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.LoginProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ServerboundLoginStartPacket
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollIoHandler
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.springframework.util.backoff.BackOffExecution
import java.net.ConnectException
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class ConnectionManager(
    val address: InetSocketAddress,
    val useEpoll: Boolean = true,
    val client: ClientNettyClientImpl,
    val configHolder: AbstractSurfCloudConfigHolder<*>,
    private val reconnectBackoff: ReconnectBackoff
) : PacketWire {

    private val stateMachine = StateMachine()
    private val listeners = CopyOnWriteArrayList<ConnectionStateChangeListener>()
    private var backoffExecution: BackOffExecution by Delegates.notNull()

    private val connection =
        ConnectionImpl(PacketFlow.CLIENTBOUND, EncryptionManager.instance, this)
    private val packetQueue = ReconnectPacketQueue()
    var currentPacketLister: PacketListener? = null

    val group = determineGroup(useEpoll)
    private val bootstrap = Bootstrap()
        .group(group)
        .channel(determineChannelClass(useEpoll))
        .remoteAddress(address)
        .handler(Initializer())

    @Volatile
    private var channel: Channel? = null

    @Volatile
    private var reconnectTask: ScheduledFuture<*>? = null
    private val scheduledReconnect = AtomicBoolean(false)
    private val tickTask = tickSecond0()

    fun addListener(listener: ConnectionStateChangeListener) {
        listeners.add(listener)
    }

    private fun tickSecond0() = group.scheduleAtFixedRate({
        if (connection.connected) {
            connection.tick(false)
        } else {
            if (connection.handleDisconnection(false)) {
                tryStartScheduleReconnect(false, null)
            }
        }
    }, 0, 1, TimeUnit.SECONDS)

    private fun fireEvent(
        from: StateMachine.State,
        to: StateMachine.State,
        cause: Throwable? = null
    ) {
        val event = ConnectionEvent(from, to, cause)
        for (listener in listeners) {
            try {
                listener.onConnectionStateChanged(event)
            } catch (e: Throwable) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to fire event ${event::class.simpleName} to ${listener::class.simpleName}")
            }
        }
    }

    fun transition(to: StateMachine.State, cause: Throwable? = null): Boolean {
        val from = stateMachine.state
        if (stateMachine.setState(to)) {
            fireEvent(from, to, cause)
            return true
        }

        log.atWarning()
            .withStackTrace(StackSize.MEDIUM)
            .log("Tried to transition from $from to $to, but transition is not valid")

        return false
    }

    fun start(isInBootstrap: Boolean): ChannelFuture? {
        if (transition(StateMachine.State.CONNECTING)) {
            return doConnect(isInBootstrap)
        }

        return null
    }

    private fun doConnect(isInBootstrap: Boolean) =
        bootstrap.connect().addListener(ChannelFutureListener { future ->
            if (future.isSuccess) {
                channel = future.channel()
                backoffExecution = reconnectBackoff.backoff.start()
                scheduledReconnect.set(false)

                startProtocol(isInBootstrap)
            } else {
                scheduleReconnect(isInBootstrap, future.cause())
            }
        })

    fun tryStartScheduleReconnect(isInBootstrap: Boolean, cause: Throwable?) {
        if (!scheduledReconnect.compareAndSet(false, true)) return
        scheduleReconnect(isInBootstrap, cause)
    }

    private fun scheduleReconnect(isInBootstrap: Boolean, cause: Throwable?) {
        if (stateMachine.state == StateMachine.State.DISCONNECTED) {
            return
        }

        val delay = backoffExecution.nextBackOff()
        log.atSevere()
            .withCause(cause)
            .log("Connection to server lost, reconnecting in $delay ms")

        if (transition(StateMachine.State.DEGRADED, cause)) {
            reconnectTask?.cancel(false)
            reconnectTask = group.schedule({
                if (stateMachine.state == StateMachine.State.DISCONNECTED) {
                    return@schedule
                }

                start(isInBootstrap)
            }, delay, TimeUnit.MILLISECONDS)
        }
    }

    fun onChannelInactive(channel: Channel) {
        log.atInfo().log("Channel inactive: %s", channel)

        if (this.channel !== channel) return
        tryStartScheduleReconnect(false, null)
    }

    fun onException(channel: Channel, cause: Throwable) {
        log.atWarning()
            .withCause(cause)
            .log("Channel exception: %s", channel)

        if (this.channel === channel) {
            channel.close()
        }
    }

    fun executePendingSynchronizingPackets() {
        packetQueue.drainForProtocol(ConnectionProtocol.SYNCHRONIZING) { packet, deferred ->
            sendImmediately(packet, deferred, false)
        }

        connection.flushChannel()
    }

    fun executePendingRunningPackets() {
        packetQueue.drainForProtocol(ConnectionProtocol.RUNNING) { packet, deferred ->
            sendImmediately(packet, deferred, false)
        }

        connection.flushChannel()
    }

    /**
     * Sends a packet immediately if the connection is in a usable state, or
     * enqueues it for later when reconnecting, depending on type and state.
     *
     * Behaviour:
     *
     * - If the current state is [StateMachine.State.CONNECTED] and the
     *   underlying [connection] is still marked as connected, the packet is
     *   sent immediately.
     *
     * - If [canBypassQueue] is true (typically for critical internal packets
     *   such as protocol-handshake packets), the packet is always sent
     *   immediately, regardless of the current connection state.
     *
     * - If the connection is not ready (DEGRADED/DISCONNECTED) and the packet
     *   is a [RespondingNettyPacket], it is **never** enqueued. Instead, a
     *   [RespondingPacketDisconnectedException] is thrown (and the optional
     *   [deferred] is completed exceptionally). This is because responding
     *   packets represent request/response interactions whose context is
     *   bound to a single connection; replaying them after reconnect would
     *   risk stale or inconsistent behaviour on the server.
     *
     * - All other non-responding packets are enqueued into [packetQueue] and
     *   will be flushed once the appropriate [ConnectionProtocol] phase
     *   (SYNCHRONIZING/RUNNING) is reached again.
     */
    fun sendOrQueue(
        packet: NettyPacket,
        phase: ConnectionProtocol,
        canBypassQueue: Boolean,
        deferred: CompletableDeferred<Boolean>? = null
    ) {
        val currentState = stateMachine.state
        val currentChannel = channel

        val canSendNow = currentState == StateMachine.State.CONNECTED &&
                currentChannel != null &&
                connection.connected

        if (canSendNow || canBypassQueue) {
            sendImmediately(packet, deferred)
        } else {
            if (packet is RespondingNettyPacket<*>) {
                val exception =
                    RespondingPacketDisconnectedException("Cannot send responding packet ${packet::class.simpleName} while connection is not connected (state=$currentState)")

                deferred?.completeExceptionally(exception)
                throw exception
            }

            packetQueue.enqueue(packet, phase, deferred)
        }
    }

    private fun sendImmediately(
        packet: NettyPacket,
        deferred: CompletableDeferred<Boolean>? = null,
        flush: Boolean = true
    ) {
        connection.internalSend(packet, deferred = deferred, flush = flush)
    }

    override fun send(
        packet: NettyPacket,
        flush: Boolean
    ) {
        sendOrQueue(
            packet,
            connection.outboundProtocolInfo.id,
            packet is CriticalInternalNettyPacket<*>
        )
    }

    override suspend fun sendWithIndication(
        packet: NettyPacket,
        flush: Boolean,
        convertExceptions: Boolean
    ): Boolean {
        val result = runCatching {
            val deferred = CompletableDeferred<Boolean>()
            sendOrQueue(packet, connection.outboundProtocolInfo.id, false, deferred)
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
        sendOrQueue(packet, connection.outboundProtocolInfo.id, false, deferred)
    }

    private fun startProtocol(isInBootstrap: Boolean) {
        log.atInfo()
            .log("Connecting to server at ${address.hostName}:${address.port}")

        try {
            val handshakeListener = ClientHandshakePacketListenerImpl(
                client,
                connection,
                client.platformExtension,
                client.statusUpdate,
            )

            currentPacketLister = handshakeListener

            connection.initiateServerboundRunningConnection(
                address.hostName,
                address.port,
                LoginProtocols.SERVERBOUND,
                LoginProtocols.CLIENTBOUND,
                handshakeListener,
                false
            )
            CloudProperties.javaClass
            connection.send(
                ServerboundLoginStartPacket(
                    client.serverCategory,
                    client.serverName,
                    client.proxy,
                    (configHolder as ClientConfigHolder).config.isLobby,
                )
            )

            if (!isInBootstrap) {
                ConnectionManagementScope.launch {
                    client.preRunningFinishedSignal.awaitNext()
                    client.startSynchronizeTask()
                }
            }

            ConnectionManagementScope.launch {
                client.enteredSynchronizingStateSignal.awaitNext()
                executePendingSynchronizingPackets()
            }

            ConnectionManagementScope.launch {
                client.synchronizeFinishedSignal.awaitNext()
                executePendingRunningPackets()
                transition(StateMachine.State.CONNECTED)
            }
        } catch (e: Exception) {
            if (e is ConnectException) {
                scheduleReconnect(isInBootstrap, e)
            } else {
                val cause = e.cause as? Exception ?: e

                throw FatalSurfError {
                    simpleErrorMessage("Couldn't connect to server")
                    detailedErrorMessage("An error occurred while trying to connect to the server.")
                    cause(cause)
                    exitCode(ExitCodes.CLIENT_COULD_NOT_CONNECT_TO_SERVER)
                    additionalInformation("Server address: ${address.hostName}:${address.port}")
                    additionalInformation("Server category: ${client.serverCategory}")
                    possibleSolution("Check if the server is online and reachable.")
                    possibleSolution("Check if the server address is correct.")
                }
            }
        }
    }

    fun shutdown() {
        val from = stateMachine.state
        if (!stateMachine.setState(StateMachine.State.DISCONNECTED)) {
            return
        }
        fireEvent(from, StateMachine.State.DISCONNECTED)

        tickTask.cancel(false)
        reconnectTask?.cancel(true)
        reconnectTask = null

        val listener = currentPacketLister
        if (listener is ClientCommonPacketListenerImpl) {
            listener.close()
        }

        connection.disconnect(DisconnectReason.CLIENT_SHUTDOWN)
        channel = null
        packetQueue.clear()
    }

    private inner class Initializer : ChannelInitializer<Channel>() {
        override fun initChannel(channel: Channel) {
            runCatching {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true)
            }

            val pipeline = channel.pipeline()
                .addLast(HandlerNames.TIMEOUT, ReadTimeoutHandler(30))

            ConnectionImpl.configureSerialization(
                pipeline,
                PacketFlow.CLIENTBOUND,
                false,
                configHolder
            )

            this@ConnectionManager.channel = channel
            connection.configurePacketHandler(channel, pipeline)
            pipeline.addLast(ClientChannelHandler(this@ConnectionManager))
        }
    }

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

        fun determineGroup(useEpoll: Boolean): MultiThreadIoEventLoopGroup {
            if (Epoll.isAvailable() && useEpoll) {
                log.atInfo().log("Using Epoll")
                return NETWORK_EPOLL_WORKER_GROUP
            } else {
                log.atInfo().log("Using NIO")
                return NETWORK_WORKER_GROUP
            }
        }

        fun determineChannelClass(useEpoll: Boolean): Class<out SocketChannel> {
            return if (Epoll.isAvailable() && useEpoll) {
                EpollSocketChannel::class.java
            } else {
                NioSocketChannel::class.java
            }
        }
    }
}