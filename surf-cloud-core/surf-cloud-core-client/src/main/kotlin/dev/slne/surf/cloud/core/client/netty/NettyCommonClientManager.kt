package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.netty.NettyManager
import kotlinx.coroutines.runBlocking

abstract class NettyCommonClientManager(
    val proxy: Boolean,
    val platformExtension: PlatformSpecificPacketListenerExtension
) : NettyManager() {
    val nettyClient by lazy { ClientNettyClientImpl(proxy, platformExtension) }

    override suspend fun bootstrap() {
        super.bootstrap()
        nettyClient.bootstrap()
    }

    override suspend fun onEnable() {
        super.onEnable()
    }

    override suspend fun afterStart() {
        super.afterStart()
        nettyClient.finalize()
    }

    override fun stop() = runBlocking {
        super.stop()
        nettyClient.stop()
    }
}