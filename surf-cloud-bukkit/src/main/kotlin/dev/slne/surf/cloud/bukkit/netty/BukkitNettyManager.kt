package dev.slne.surf.cloud.bukkit.netty

import dev.slne.surf.cloud.bukkit.netty.listener.NettyPlayerConnectionBlocker
import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.surfapi.bukkit.api.event.unregister
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(CloudLifecycleAware.NETTY_MANAGER_PRIORITY)
class BukkitNettyManager(
    platformExtension: PlatformSpecificPacketListenerExtension,
    configHolder: AbstractSurfCloudConfigHolder<*>
) : NettyCommonClientManager(false, platformExtension, configHolder) {

    override fun blockPlayerConnections() {
        NettyPlayerConnectionBlocker.register()
    }

    override fun unblockPlayerConnections() {
        NettyPlayerConnectionBlocker.unregister()
    }
}