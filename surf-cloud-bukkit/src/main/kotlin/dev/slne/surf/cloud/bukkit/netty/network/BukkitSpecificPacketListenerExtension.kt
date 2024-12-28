package dev.slne.surf.cloud.bukkit.netty.network

import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.util.*

object BukkitSpecificPacketListenerExtension : PlatformSpecificPacketListenerExtension {
    override fun isServerManagedByThisProxy(address: InetSocketAddress): Boolean {
        error("Requested wrong server! This packet can only be acknowledged on a proxy!")
    }

    override suspend fun transferPlayerToServer(
        playerUuid: UUID,
        serverAddress: InetSocketAddress
    ): Pair<ServerboundTransferPlayerPacketResponse.Status, Component?> {
        error("Requested wrong server! This packet can only be acknowledged on a proxy!")
    }

    override fun disconnectPlayer(playerUuid: UUID, reason: Component) {
        error("Requested wrong server! This packet can only be acknowledged on a proxy!")
    }
}