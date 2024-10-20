package dev.slne.surf.cloud.core.netty.common.source

import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.api.server.CloudServer
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoAction
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info.CloudServerInfoPacket

abstract class AbstractProxiedNettySource<Client : ProxiedNettySource<Client>>(nettyBase: AbstractNettyBase<*, *, Client>) :
    AbstractNettySource<Client>(nettyBase), ProxiedNettySource<Client> {
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

//    fun cloudServer(cloudServer: CloudServer) {
//        this.cloudServer = cloudServer
//        //    if (this.cloudServer != null) {
////      this.direction = this.cloudServer.getDirection();
////    }
//    }

    fun broadcastServerInfo(
        action: CloudServerInfoAction,
        vararg sources: AbstractNettySource<*>
    ) {
        if (action == CloudServerInfoAction.UPDATE_SERVER_INFO) {
            // we are calling the update function,
            // so our instance knows when we changed something for us or another source
            //
            // note here that it is a little hacky, but does it job

//      ((NettyClientTrackerImpl<ProxiedSource>) base.container.sourceTracker()).updateClient(this); TODO not needed or replace with spring events

            val cloudServer = cloudServer
            checkNotNull(cloudServer) { "We should have a cloud server here" }
            updateServerInfo(cloudServer)
        }
        val packet = CloudServerInfoPacket(action, cloudServer)

        if (sources.isEmpty()) {
            base.connection.broadcast(packet)
        } else {
            for (source in sources) {
                source.sendPacket(packet)
            }
        }
    }

    override val serverGuid: Long
        get() = cloudServer?.serverGuid ?: -1

    override fun hasServerGuid(): Boolean = serverGuid != -1L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractProxiedNettySource<*>) return false
        if (!super.equals(other)) return false

        if (cloudServer != other.cloudServer) return false
        if (lastCloudServer != other.lastCloudServer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (cloudServer?.hashCode() ?: 0)
        result = 31 * result + (lastCloudServer?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "AbstractProxiedNettySource(cloudServer=$cloudServer, lastCloudServer=$lastCloudServer) ${super.toString()}"
    }
}
