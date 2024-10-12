package dev.slne.surf.cloud.api.netty.source

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.server.CloudServer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
@PlatformRequirement(PlatformRequirement.Platform.SERVER)
interface ProxiedNettySource<Client : ProxiedNettySource<Client>> : NettySource<Client> {
    val cloudServer: CloudServer? // TODO: 29.09.2024 10:49 - better name
    val lastCloudServer: CloudServer?
    val serverGuid: Long

    fun hasServerGuid(): Boolean
}
