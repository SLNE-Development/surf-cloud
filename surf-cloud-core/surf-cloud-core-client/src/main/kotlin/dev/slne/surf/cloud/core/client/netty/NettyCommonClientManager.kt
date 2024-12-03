package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.netty.NettyManager

abstract class NettyCommonClientManager(
    val proxy: Boolean,
    val platformExtension: PlatformSpecificPacketListenerExtension
) : NettyManager() {
    val nettyClient by lazy { ClientNettyClientImpl(proxy, platformExtension) }

    override suspend fun afterStart() {
        super.afterStart()
        nettyClient.start()
    }

    override fun stop() {
        super.stop()
        nettyClient.stop()
    }
}