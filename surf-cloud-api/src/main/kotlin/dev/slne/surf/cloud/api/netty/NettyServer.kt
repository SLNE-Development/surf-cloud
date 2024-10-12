package dev.slne.surf.cloud.api.netty

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import org.jetbrains.annotations.ApiStatus

@PlatformRequirement(PlatformRequirement.Platform.SERVER)
@ApiStatus.NonExtendable
interface NettyServer : NettyBase<NettyClientSource> // Only type information
