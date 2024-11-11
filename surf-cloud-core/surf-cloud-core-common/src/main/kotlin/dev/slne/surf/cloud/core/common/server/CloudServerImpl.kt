package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation

class CloudServerImpl(
    override val connection: ConnectionImpl,
    override var groupId: String?,
    override val serverGuid: Long,
    override val port: Int
) : CloudServer {
    var information = ClientInformation.NOT_AVAILABLE

    override val maxPlayerCount get() = information.maxPlayerCount
    override val currentPlayerCount get() = information.currentPlayerCount
    override val whitelist get() = information.whitelist
    override val state get() = information.state
}
