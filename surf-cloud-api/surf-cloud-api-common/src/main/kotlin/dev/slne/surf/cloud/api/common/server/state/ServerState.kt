package dev.slne.surf.cloud.api.common.server.state

enum class ServerState(val allowJoin: Boolean) {
    RESTARTING(false),
    LOBBY(true),
    OFFLINE(false),
    ONLINE(true)
}
