package dev.slne.surf.cloud.api.common.netty.network

/**
 * Enum representing the different protocol states in the Netty connection.
 */
enum class ConnectionProtocol {
    HANDSHAKING,
    INITIALIZE,
    LOGIN,
    PRE_RUNNING,
    RUNNING,
    SHUTDOWN
}