package dev.slne.surf.cloud.velocity.netty

import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.velocity.netty.listener.NettyPlayerConnectionBlocker
import dev.slne.surf.cloud.velocity.plugin
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import dev.slne.surf.cloud.velocity.proxy as velocityProxy

@Component
@Order(CloudLifecycleAware.NETTY_MANAGER_PRIORITY)
class VelocityNettyManager(
    platformExtension: PlatformSpecificPacketListenerExtension,
    configHolder: AbstractSurfCloudConfigHolder<*>
) : NettyCommonClientManager(true, platformExtension, configHolder) {
    override fun blockPlayerConnections() {
        velocityProxy.eventManager.register(plugin, NettyPlayerConnectionBlocker)
    }

    override fun unblockPlayerConnections() {
        velocityProxy.eventManager.unregisterListener(plugin, NettyPlayerConnectionBlocker)
    }
}