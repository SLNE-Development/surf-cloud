package dev.slne.surf.cloud.bukkit.netty.network

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.bukkit.listener.player.SilentDisconnectListener
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RegistrationInfo
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse
import kotlinx.coroutines.future.await
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
        val player = Bukkit.getPlayer(playerUuid) ?: return
        player.kick(reason)
    }

    override fun silentDisconnectPlayer(playerUuid: UUID) {
        val player = Bukkit.getPlayer(playerUuid) ?: return
        SilentDisconnectListener.silentDisconnect(player)
    }

    override suspend fun teleportPlayer(
        uuid: UUID,
        location: TeleportLocation,
        teleportCause: TeleportCause,
        flags: Array<out TeleportFlag>
    ): Boolean {
        val player = Bukkit.getPlayer(uuid) ?: return false
        val cloudPlayer = player.toCloudPlayer() ?: return false

        return cloudPlayer.teleport(location, teleportCause, *flags)
    }

    override fun registerCloudServersToProxy(packets: Array<RegistrationInfo>) {
        error("Requested wrong server! This packet can only be acknowledged on a proxy!")
    }

    override suspend fun teleportPlayerToPlayer(
        uuid: UUID,
        target: UUID
    ): Boolean {
        val player = Bukkit.getPlayer(uuid) ?: return false
        val targetPlayer = Bukkit.getPlayer(target) ?: return false

        return player.teleportAsync(targetPlayer.location).await()
    }

    override fun triggerShutdown() {
        plugin.launch(plugin.globalRegionDispatcher) {
            Bukkit.shutdown()
        }
    }
}