package dev.slne.surf.cloud.core.client.server

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound.ServerboundShutdownServerPacket
import dev.slne.surf.cloud.core.common.server.AbstractCloudServer
import java.net.InetSocketAddress

class ClientCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    playAddress: InetSocketAddress,
    lobby: Boolean
) :
    AbstractCloudServer(uid, group, name, playAddress, lobby) {
    override fun shutdown() {
        ServerboundShutdownServerPacket(uid).fireAndForget()
    }
}