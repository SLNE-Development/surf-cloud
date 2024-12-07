package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.server.CloudServerImpl
import dev.slne.surf.cloud.standalone.server.queue.SingleServerQueue

class StandaloneCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    override val connection: Connection,
) : CloudServerImpl(uid, group, name), ServerCloudServer,
    CommonStandaloneServer by CommonStandaloneServerImpl() {

    init {
        wrapper = this
    }

    val queue = SingleServerQueue(this)
}

fun ServerCommonCloudServer?.asStandaloneServer() = this as? StandaloneCloudServerImpl