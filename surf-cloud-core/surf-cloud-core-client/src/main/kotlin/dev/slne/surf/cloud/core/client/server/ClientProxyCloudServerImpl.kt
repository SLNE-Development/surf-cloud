package dev.slne.surf.cloud.core.client.server

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundShutdownServerPacket
import dev.slne.surf.cloud.core.common.server.AbstractProxyCloudServer
import java.net.InetSocketAddress

class ClientProxyCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    address: InetSocketAddress,
) : AbstractProxyCloudServer(uid, group, name, address) {
    override fun shutdown() {
        ServerboundShutdownServerPacket(uid).fireAndForget()
    }
}