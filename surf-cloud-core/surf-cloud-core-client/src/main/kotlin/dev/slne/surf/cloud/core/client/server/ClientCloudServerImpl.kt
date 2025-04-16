package dev.slne.surf.cloud.core.client.server

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundShutdownServerPacket
import dev.slne.surf.cloud.core.common.server.AbstractCloudServer

class ClientCloudServerImpl(uid: Long, group: String, name: String) :
    AbstractCloudServer(uid, group, name) {
    override fun shutdown() {
        ServerboundShutdownServerPacket(uid).fireAndForget()
    }
}