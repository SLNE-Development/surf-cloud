package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import java.util.*

interface CommonStandaloneServer {

    var wrapper: ServerCommonCloudServer

    fun handlePlayerConnect(playerUUID: UUID)

    fun handlePlayerDisconnect(playerUUID: UUID)
}