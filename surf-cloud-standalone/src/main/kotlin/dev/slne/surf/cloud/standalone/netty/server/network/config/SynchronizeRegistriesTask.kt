package dev.slne.surf.cloud.standalone.netty.server.network.config

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.clientbound.ClientboundBatchSyncSetPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.clientbound.ClientboundBatchSyncValuePacket
import dev.slne.surf.cloud.standalone.sync.SyncRegistryImpl

object SynchronizeRegistriesTask : CloudInitialSynchronizeTask {

    override suspend fun execute(client: NettyClient) {
        client.connection.send(ClientboundBatchSyncValuePacket(SyncRegistryImpl.instance.prepareBatchSyncValues()))
        client.connection.send(ClientboundBatchSyncSetPacket(SyncRegistryImpl.instance.prepareBatchSyncSets()))
    }
}