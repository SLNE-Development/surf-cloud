package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.api.server.server.ServerProxyCloudServer
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundTriggerShutdownPacket
import dev.slne.surf.cloud.core.common.server.AbstractProxyCloudServer

class StandaloneProxyCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    override val connection: ConnectionImpl
) : AbstractProxyCloudServer(uid, group, name), ServerProxyCloudServer,
    CommonStandaloneServer by CommonStandaloneServerImpl() {

    fun registerClients(vararg clients: StandaloneCloudServerImpl) {
    }

    override fun shutdown() {
        connection.send(ClientboundTriggerShutdownPacket)
    }

    init {
        wrapper = this
    }
}

fun ServerCommonCloudServer?.asStandaloneProxy() = this as? StandaloneProxyCloudServerImpl