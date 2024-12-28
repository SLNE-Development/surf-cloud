package dev.slne.surf.cloud.bukkit.netty.network

import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.util.position.FineLocation
import dev.slne.surf.cloud.api.common.util.position.FineTeleportCause
import dev.slne.surf.cloud.api.common.util.position.FineTeleportFlag
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
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

    override suspend fun teleportPlayer(
        uuid: UUID,
        location: FineLocation,
        teleportCause: FineTeleportCause,
        flags: Array<out FineTeleportFlag>
    ): Boolean {
        val player = Bukkit.getPlayer(uuid) ?: return false
        val cloudPlayer = player.toCloudPlayer() ?: return false

        return cloudPlayer.teleport(location, teleportCause, *flags)
    }
}