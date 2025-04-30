package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.server.CommonCloudServer

interface ServerCommonCloudServer: CommonCloudServer {
    val connection: Connection

    override val displayName: String
        get() = super.displayName + " (${connection.getLoggableAddress()})"
}