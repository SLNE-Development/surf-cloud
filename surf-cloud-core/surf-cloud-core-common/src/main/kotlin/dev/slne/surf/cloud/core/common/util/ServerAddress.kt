package dev.slne.surf.cloud.core.common.util

import com.google.common.net.HostAndPort
import java.net.IDN
import java.net.InetSocketAddress

class ServerAddress(private val hostAndPort: HostAndPort) {

    constructor(host: String, port: Int) : this(HostAndPort.fromParts(host, port))

    val host: String get() = runCatching { IDN.toASCII(hostAndPort.host) }.getOrDefault("")
    val port: Int get() = hostAndPort.port
}

fun String.toServerAddress(): ServerAddress {
    try {
        return ServerAddress(HostAndPort.fromString(this).withDefaultPort(25566))
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid server address: $this")
    }
}

fun InetSocketAddress(serverAddress: ServerAddress) =
    InetSocketAddress(serverAddress.host, serverAddress.port)