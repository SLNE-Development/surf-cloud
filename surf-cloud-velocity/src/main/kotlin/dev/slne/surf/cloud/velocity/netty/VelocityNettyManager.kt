package dev.slne.surf.cloud.velocity.netty

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.core.client.netty.CommonCloudClientNettyManagerImpl
import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.cloud.velocity.netty.network.VelocitySpecificPacketListenerExtension

object VelocityNettyManager: NettyCommonClientManager(true, VelocitySpecificPacketListenerExtension)