package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.core.common.netty.NettyManager
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(CloudLifecycleAware.NETTY_MANAGER_PRIORITY)
class StandaloneNettyManager : NettyManager() {
    override fun blockPlayerConnections() {}
    override fun unblockPlayerConnections() {}
}