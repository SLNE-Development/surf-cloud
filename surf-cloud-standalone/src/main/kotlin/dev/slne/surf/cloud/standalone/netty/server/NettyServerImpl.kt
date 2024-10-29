package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.api.util.mutableObjectListOf
import dev.slne.surf.cloud.api.util.synchronize
import dev.slne.surf.cloud.core.config.cloudConfig
import dev.slne.surf.cloud.standalone.netty.server.connection.ServerConnectionListener
import io.netty.channel.epoll.Epoll
import io.netty.channel.unix.DomainSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

@Component
@Profile("independent")
class NettyServerImpl {
    val log = logger()

    private lateinit var localIp: String
    private var port = -1

    val connection by lazy { ServerConnectionListener(this) }

    private val schedules = mutableObjectListOf<() -> Unit>().synchronize()

    suspend fun initServer(): Boolean = withContext(Dispatchers.IO) {
        val config = cloudConfig.connectionConfig.nettyConfig
        localIp = config.host

        val bindAddress: SocketAddress

        if (localIp.startsWith("unix:")) {
            if (!Epoll.isAvailable()) {
                log.atSevere().log("**** INVALID CONFIGURATION! ****")
                log.atSevere().log("You are trying to use a Unix domain socket but you're not on a supported OS.")
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

    fun finalizeServer() {
        connection.acceptConnections()
    }

    suspend fun stopServer() {
        connection.stop()
    }

    suspend fun tick() {
        connection.connections.forEach {  }
        connection.tick()
        schedules.removeAll { function ->
            function()
            true
        }
    }

    fun schedule(function: () -> Unit) {
        schedules.add(function)
    }
}
