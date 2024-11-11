package dev.slne.surf.cloud.api.common.netty

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.util.requiredService
import org.jetbrains.annotations.ApiStatus.NonExtendable

@NonExtendable
interface CloudNettyManager {
    val connection: Connection

    companion object {
        val instance = requiredService<CloudNettyManager>()
    }
}

val nettyManager get() = CloudNettyManager.instance