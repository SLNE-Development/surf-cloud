package dev.slne.surf.cloud.api.common.plugin.spring.task

import dev.slne.surf.cloud.api.common.netty.NettyClient
import org.jetbrains.annotations.ApiStatus

interface CloudInitialSynchronizeTask {
    val name: String
        get() = this::class.simpleName ?: "Unknown"

    @ApiStatus.OverrideOnly
    suspend fun execute(client: NettyClient)
}