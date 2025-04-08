package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.BatchTransferResult
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.coroutines.PlayerBatchTransferScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

abstract class CommonCloudServerImpl(
    override val uid: Long,
    override val group: String,
    override val name: String,
    override val users: UserListImpl,

    @Volatile
    var information: ClientInformation
) : CommonCloudServer {
    private suspend fun executeBatchTransfer(
        filter: (CloudPlayer) -> Boolean = { true },
        transferAction: suspend (CloudPlayer) -> Deferred<ConnectionResult>
    ): BatchTransferResult {
        val results = users
            .filter(filter)
            .associateWith { transferAction(it) }
            .mapValuesTo(mutableObject2ObjectMapOf()) { (_, deferred) -> deferred.await() }

        val success = results.all { (_, result) -> result.first == ConnectionResultEnum.SUCCESS }
        return success to results
    }

    override suspend fun sendAll(server: CloudServer): BatchTransferResult =
        executeBatchTransfer { PlayerBatchTransferScope.async { it.connectToServer(server) } }

    override suspend fun sendAll(
        server: CloudServer,
        filter: (CloudPlayer) -> Boolean
    ): BatchTransferResult =
        executeBatchTransfer(filter) { PlayerBatchTransferScope.async { it.connectToServer(server) } }

    override suspend fun sendAll(category: String): BatchTransferResult =
        executeBatchTransfer { PlayerBatchTransferScope.async { it.connectToServer(category) } }

    override val maxPlayerCount get() = information.maxPlayerCount
    override val currentPlayerCount get() = users.size
    override val state get() = information.state

    override fun audiences() = users

    override fun shutdown() {
        coreCloudInstance.shutdownServer(this)
    }

    override fun toString(): String {
        return "CloudServerImpl(group='$group', uid=$uid, name='$name, users=$users, information=$information, maxPlayerCount=$maxPlayerCount, currentPlayerCount=$currentPlayerCount, state=$state)"
    }
}
