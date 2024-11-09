package dev.slne.surf.cloud.core.netty

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