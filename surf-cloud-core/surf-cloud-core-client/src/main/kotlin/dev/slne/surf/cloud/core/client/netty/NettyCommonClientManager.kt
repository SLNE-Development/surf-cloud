package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.core.common.netty.NettyManager

abstract class NettyCommonClientManager : NettyManager() {
    val nettyClient by lazy { ClientNettyClientImpl() }

    override suspend fun afterStart() {
        super.afterStart()
        nettyClient.start()
    }

    override fun stop() {
        super.stop()
        nettyClient.stop()
    }
}