package dev.slne.surf.cloud.core.client.netty.network

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
}