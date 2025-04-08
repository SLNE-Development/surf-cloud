package dev.slne.surf.cloud.api.client.netty

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.surfapi.core.api.util.requiredService
import org.jetbrains.annotations.ApiStatus.NonExtendable

@NonExtendable
interface CloudClientNettyManager {
    val client: NettyClient

    companion object : CloudClientNettyManager by requiredService<CloudClientNettyManager>()
}