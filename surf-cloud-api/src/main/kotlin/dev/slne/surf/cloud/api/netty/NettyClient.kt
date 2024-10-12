package dev.slne.surf.cloud.api.netty

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.source.NettyServerSource
import org.jetbrains.annotations.ApiStatus

@PlatformRequirement(PlatformRequirement.Platform.CLIENT)
@ApiStatus.NonExtendable
interface NettyClient : NettyBase<NettyServerSource> // Only type information
