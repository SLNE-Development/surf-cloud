package dev.slne.surf.cloud.core.util

import com.google.common.net.HostAndPort
import java.net.IDN

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