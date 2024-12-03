package dev.slne.surf.cloud.velocity.netty.network

import com.velocitypowered.api.proxy.ConnectionRequestBuilder
import com.velocitypowered.api.proxy.server.PingOptions
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse.Status
import dev.slne.surf.cloud.velocity.proxy
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.util.*

object VelocitySpecificPacketListenerExtension : PlatformSpecificPacketListenerExtension {
    override fun isServerManagedByThisProxy(address: InetSocketAddress) =
        proxy.allServers.any { it.serverInfo.address == address } // TODO: Check if this is correct

    override suspend fun transferPlayerToServer(
        playerUuid: UUID,
        serverAddress: InetSocketAddress
    ): Pair<Status, Component?> {
        val server = proxy.allServers.find { it.serverInfo.address == serverAddress }
            ?: error("Server $serverAddress is no longer managed by this proxy")
        val player =
            proxy.getPlayer(playerUuid).orElseThrow { error("Player $playerUuid not found") }

        val result = player.createConnectionRequest(server).connect().await()
        return when(result.status) {
            ConnectionRequestBuilder.Status.SUCCESS -> Status.SUCCESS
            ConnectionRequestBuilder.Status.ALREADY_CONNECTED -> Status.ALREADY_CONNECTED
            ConnectionRequestBuilder.Status.CONNECTION_IN_PROGRESS -> Status.CONNECTION_IN_PROGRESS
            ConnectionRequestBuilder.Status.CONNECTION_CANCELLED -> Status.CONNECTION_CANCELLED
            ConnectionRequestBuilder.Status.SERVER_DISCONNECTED -> Status.SERVER_DISCONNECTED
        } to result.reasonComponent.orElse(null)
    }
}