package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.api.server.server.ServerProxyCloudServer
import dev.slne.surf.cloud.core.common.server.ProxyCloudServerImpl

class StandaloneProxyCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    override val connection: Connection
) : ProxyCloudServerImpl(uid, group, name), ServerProxyCloudServer,
    CommonStandaloneServer by CommonStandaloneServerImpl() {
    init {
        wrapper = this
    }
}

fun ServerCommonCloudServer?.asStandaloneProxy() = this as? StandaloneProxyCloudServerImpl