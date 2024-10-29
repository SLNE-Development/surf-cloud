package dev.slne.surf.cloud.core.netty.common.source

import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.api.server.CloudServer
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoAction
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoPacket

abstract class AbstractProxiedNettySource<Client : ProxiedNettySource<Client>>(connection: Connection) :
    AbstractNettySource<Client>(connection), ProxiedNettySource<Client> {
    override var cloudServer: CloudServer? = null
    override var lastCloudServer: CloudServer? = null

    fun updateServerInfo(other: CloudServer) {
        if (this.cloudServer == null) {
            this.cloudServer = other
            return
        }

        this.lastCloudServer = this.cloudServer
        this.cloudServer = other
    }

    fun broadcastServerInfo(
        action: CloudServerInfoAction,
        vararg sources: AbstractNettySource<*>
    ) {
        val cloudServer = cloudServer
        checkNotNull(cloudServer) { "We should have a cloud server here" }

        if (action == CloudServerInfoAction.UPDATE_SERVER_INFO) {
            // we are calling the update function,
            // so our instance knows when we changed something for us or another source
            //
            // note here that it is a little hacky, but does it job

//      ((NettyClientTrackerImpl<ProxiedSource>) base.container.sourceTracker()).updateClient(this); TODO not needed or replace with spring events
            updateServerInfo(cloudServer)
        }
        val packet = CloudServerInfoPacket(action, cloudServer)

        if (sources.isEmpty()) {
            connection.broadcast(packet)
        } else {
            for (source in sources) {
                source.sendPacket(packet)
            }
        }
    }

    override val serverGuid: Long
        get() = cloudServer?.serverGuid ?: -1

    override fun hasServerGuid(): Boolean = serverGuid != -1L
}
