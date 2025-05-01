package dev.slne.surf.cloud.api.server.netty.packet

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.server.server.ServerCloudServerManager
import dev.slne.surf.surfapi.core.api.util.logger

private val log = logger()

suspend fun NettyPacket.broadcast() {
    ServerCloudServerManager.retrieveAllServers().forEach { server ->
        try {
            server.connection.send(this)
        } catch (e: Throwable) {
            log.atWarning()
                .withCause(e)
                .log("Failed to send packet in broadcast action to server ${server.displayName}")
        }
    }
}