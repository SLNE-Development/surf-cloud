package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.core.netty.network.Connection

class ServerClientImpl(val serverId: Long, val serverCategory: String) {

    lateinit var connection: Connection
}