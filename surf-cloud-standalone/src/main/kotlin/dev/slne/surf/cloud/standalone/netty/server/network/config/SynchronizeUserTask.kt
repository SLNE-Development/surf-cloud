package dev.slne.surf.cloud.standalone.netty.server.network.config

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask

object SynchronizeUserTask: CloudInitialSynchronizeTask {
    override suspend fun execute(client: NettyClient) {

    }
}