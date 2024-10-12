package dev.slne.surf.cloud.api.netty.source

import dev.slne.surf.cloud.api.netty.NettyBase
import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
@PlatformRequirement(PlatformRequirement.Platform.COMMON)
interface NettySource<Client : ProxiedNettySource<Client>> {
    val base: NettyBase<Client>

    val proxied: Boolean
        get() = this is ProxiedNettySource<*>

    fun sendPacket(packet: NettyPacket<*>)
}
