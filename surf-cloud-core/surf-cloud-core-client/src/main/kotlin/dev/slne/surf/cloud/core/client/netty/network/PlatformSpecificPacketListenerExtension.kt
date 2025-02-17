package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RegistrationInfo
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.util.*

interface PlatformSpecificPacketListenerExtension {

    fun isServerManagedByThisProxy(address: InetSocketAddress): Boolean

    suspend fun transferPlayerToServer(
        playerUuid: UUID,
        serverAddress: InetSocketAddress
    ): Pair<ServerboundTransferPlayerPacketResponse.Status, Component?>

    fun disconnectPlayer(playerUuid: UUID, reason: Component)

    suspend fun teleportPlayer(
        uuid: UUID,
        location: TeleportLocation,
        teleportCause: TeleportCause,
        flags: Array<out TeleportFlag>
    ): Boolean

    fun registerCloudServersToProxy(packets: Array<RegistrationInfo>)

    fun triggerShutdown()
}