package dev.slne.surf.cloud.core.client.server

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundShutdownServerPacket
import dev.slne.surf.cloud.core.common.server.AbstractCloudServer
import java.net.InetSocketAddress

class ClientCloudServerImpl(
    group: String,
    name: String,
    playAddress: InetSocketAddress,
    lobby: Boolean
) :
    AbstractCloudServer(group, name, playAddress, lobby) {
    override fun shutdown() {
        ServerboundShutdownServerPacket(name).fireAndForget()
    }
}