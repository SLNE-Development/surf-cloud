package dev.slne.surf.cloud.velocity.netty

import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.cloud.velocity.netty.listener.NettyPlayerConnectionBlocker
import dev.slne.surf.cloud.velocity.netty.network.VelocitySpecificPacketListenerExtension
import dev.slne.surf.cloud.velocity.plugin
import dev.slne.surf.cloud.velocity.proxy as velocityProxy

object VelocityNettyManager :
    NettyCommonClientManager(true, VelocitySpecificPacketListenerExtension) {
    override fun blockPlayerConnections() {
        velocityProxy.eventManager.register(plugin, NettyPlayerConnectionBlocker)
    }

    override fun unblockPlayerConnections() {
        velocityProxy.eventManager.unregisterListener(plugin, NettyPlayerConnectionBlocker)
    }
}