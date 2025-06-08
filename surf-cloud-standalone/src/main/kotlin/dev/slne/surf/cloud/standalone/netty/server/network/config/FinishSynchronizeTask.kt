package dev.slne.surf.cloud.standalone.netty.server.network.config

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.FinishSynchronizingPacket

object FinishSynchronizeTask : CloudInitialSynchronizeTask {
    override suspend fun execute(client: NettyClient) {
        client.connection.send(FinishSynchronizingPacket)
    }
}