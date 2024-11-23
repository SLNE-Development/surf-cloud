package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.core.common.server.CloudServerImpl

class StandaloneServerImpl(
    uid: Long,
    group: String,
    name: String,
    proxy: Boolean,
    override val connection: Connection
) : CloudServerImpl(uid, group, name, proxy), ServerCloudServer