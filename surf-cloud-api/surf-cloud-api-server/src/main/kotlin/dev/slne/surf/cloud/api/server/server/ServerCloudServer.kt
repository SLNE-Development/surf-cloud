package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.server.CloudServer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerCloudServer : ServerCommonCloudServer, CloudServer {
    val expectedPlayers: Int
}