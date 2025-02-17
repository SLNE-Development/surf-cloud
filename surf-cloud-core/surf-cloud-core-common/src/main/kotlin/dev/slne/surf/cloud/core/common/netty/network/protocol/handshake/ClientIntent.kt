package dev.slne.surf.cloud.core.common.netty.network.protocol.handshake

enum class ClientIntent {
    STATUS, // Currently not in use
    INITIALIZE,
    LOGIN,
}