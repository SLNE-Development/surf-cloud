package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.BatchTransferResult
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.core.common.coroutines.PlayerBatchTransferScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import dev.slne.surf.cloud.core.common.sound.CommonSounds
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress

abstract class CommonCloudServerImpl(
    override val group: String,
    override val name: String,
    override val users: UserListImpl,
    override val playAddress: InetSocketAddress,

    @Volatile
    var information: ClientInformation
) : CommonCloudServer {
    private suspend fun executeBatchTransfer(
        filter: (CloudPlayer) -> Boolean = { true },
        transferAction: suspend (CloudPlayer) -> Deferred<ConnectionResultEnum>
    ): BatchTransferResult {
        val results = users
            .filter(filter)
            .associateWith { transferAction(it) }
            .mapValuesTo(mutableObject2ObjectMapOf()) { (_, deferred) -> deferred.await() }

        val success = results.all { (_, result) -> result == ConnectionResultEnum.SUCCESS }
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

    override fun isInGroup(group: String): Boolean {
        return group.equals(group, ignoreCase = true)
    }

    override suspend fun broadcast(message: Component, permission: String?, playSound: Boolean) {
        if (permission == null) {
            sendMessage(message)
            if (playSound) {
                for (sound in CommonSounds.broadcastSounds) {
                    playSound(sound, Emitter.self())
                    delay(150)
                }
            }
        } else {
            users.forEach { user ->
                user.sendMessage(message, permission)
            }
            if (playSound) {
                for (sound in CommonSounds.broadcastSounds) {
                    users.forEach { user ->
                        user.playSound(sound, Emitter.self(), permission)
                    }
                    delay(150)
                }
            }
        }
    }

    override val maxPlayerCount get() = information.maxPlayerCount
    override val currentPlayerCount get() = users.size
    override val state get() = information.state

    override fun audiences() = users

    override fun toString(): String {
        return "CloudServerImpl(group='$group', name='$name, users=$users, information=$information, maxPlayerCount=$maxPlayerCount, currentPlayerCount=$currentPlayerCount, state=$state)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommonCloudServerImpl) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}
