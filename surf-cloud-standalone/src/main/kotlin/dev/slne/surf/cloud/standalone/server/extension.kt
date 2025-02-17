package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl

val CommonCloudServerImpl.connection: ConnectionImpl
    get() = when (this) {
        is StandaloneCloudServerImpl -> this.connection
        is StandaloneProxyCloudServerImpl -> this.connection
        else -> error("Unknown server type")
    }