package dev.slne.surf.cloud.api.common.netty.network

enum class ConnectionProtocol {
    HANDSHAKING,
    INITIALIZE,
    LOGIN,
    PRE_RUNNING,
    RUNNING
}