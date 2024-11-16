package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.server.CloudServer

interface ServerCloudServer: CloudServer {
    val connection: Connection
}