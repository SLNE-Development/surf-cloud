package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.config.cloudConfig
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.standalone.netty.server.connection.ServerConnectionListener
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import dev.slne.surf.cloud.standalone.spark.provider.CloudTickHook
import dev.slne.surf.cloud.standalone.spark.provider.CloudTickReporter
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.epoll.Epoll
import io.netty.channel.unix.DomainSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@Component
@Profile("server")
class NettyServerImpl : InitializingBean, DisposableBean {
    val log = logger()

    private lateinit var localIp: String
    private var port = -1

    val connection by lazy { ServerConnectionListener(this) }
    private val clients = mutableObjectListOf<ServerClientImpl>()
    private val clientsLock = Mutex()
    private val schedules = mutableObjectListOf<() -> Unit>().synchronize()

    @Volatile
    private var running = false

    override fun afterPropertiesSet() = runBlocking {
        initServer()
        finalizeServer()
    }

    override fun destroy() = runBlocking {
        stopServer()
    }

    suspend fun initServer(): Boolean = withContext(Dispatchers.IO) {
        val config = cloudConfig.connectionConfig.nettyConfig
        localIp = config.host

        val bindAddress: SocketAddress

        if (localIp.startsWith("unix:")) {
            if (!Epoll.isAvailable()) {
                log.atSevere().log("**** INVALID CONFIGURATION! ****")
                log.atSevere()
                    .log("You are trying to use a Unix domain socket but you're not on a supported OS.")
                return@withContext false
            }
            bindAddress = DomainSocketAddress(localIp.substring(5))
        } else {
            var inetaddress: InetAddress? = null

            if (localIp.isNotEmpty()) {
                inetaddress = InetAddress.getByName(localIp)
            }

            if (port < 0) {
                port = config.port
            }

            bindAddress = InetSocketAddress(inetaddress, port)
        }

        log.atInfo().log("Starting Netty server on $localIp:$port")

        try {
            connection.bind(bindAddress)
            true
        } catch (e: Exception) {
            log.atSevere().log("**** FAILED TO BIND TO PORT!")
            log.atSevere().log("The exception was: $e")
            log.atSevere().log("Perhaps a server is already running on that port?")
            throw IllegalStateException("Failed to bind to port", e)
        }
    }

    suspend fun finalizeServer() {
        running = true
        connection.acceptConnections()
    }

    suspend fun stopServer() {
        connection.stop()
        running = false
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    protected suspend fun tick() {
        if (!running) return

        val duration = measureTimeMillis {
            CloudTickHook.tick()
            connection.connections.forEach { it.tick() }
            connection.tick()
            schedules.removeAll { function ->
                function()
                true
            }
        }
        CloudTickReporter.tick(duration.toDouble())
    }

    fun schedule(function: () -> Unit) {
        schedules.add(function)
    }

    suspend fun registerClient(client: ServerClientImpl, proxy: Boolean) {
        clientsLock.withLock {
            if (clients.any { it.serverName == client.serverName }) {
                log.atSevere()
                    .log("Client with name ${client.serverName} already exists")
                client.connection.disconnect(
                    DisconnectionDetails(
                        DisconnectReason.CLIENT_NAME_ALREADY_EXISTS,
                        client.serverName
                    )
                )
                return
            }

            clients.add(client)
        }

        log.atInfo().log("Registered client ${client.displayName}")

        val server = if (proxy) {
            StandaloneProxyCloudServerImpl(
                client.serverId,
                client.serverCategory,
                client.serverName,
                client.playAddress,
                client.connection,
            )
        } else {
            StandaloneCloudServerImpl(
                client.serverId,
                client.serverCategory,
                client.serverName,
                client.playAddress,
                client.lobbyServer,
                client.connection
            )
        }

        serverManagerImpl.registerServer(server)
    }

    suspend fun unregisterClient(client: ServerClientImpl) {
        clientsLock.withLock {
            clients.remove(client)
        }

        log.atInfo().log("Unregistered client ${client.displayName}")

        serverManagerImpl.unregisterServer(client.serverId)
    }

    suspend fun forEachClient(action: suspend (ServerClientImpl) -> Unit) {
        clientsLock.withLock {
            clients.forEach { action(it) }
        }
    }

    suspend fun clientsSnapshot(): List<ServerClientImpl> {
        return clientsLock.withLock {
            clients.toList()
        }
    }
}
