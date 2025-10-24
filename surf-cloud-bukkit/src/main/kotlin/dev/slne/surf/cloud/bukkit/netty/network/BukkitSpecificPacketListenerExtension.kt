package dev.slne.surf.cloud.bukkit.netty.network

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.WorldLocation
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.player.toast.NetworkToast
import dev.slne.surf.cloud.api.common.util.observer.observingFlow
import dev.slne.surf.cloud.bukkit.listener.player.SilentDisconnectListener
import dev.slne.surf.cloud.bukkit.player.BukkitClientCloudPlayerImpl
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.client.server.ClientCloudServerImpl
import dev.slne.surf.cloud.core.common.coroutines.CommonObservableScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RegistrationInfo
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.nmsCommonBridge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.net.InetSocketAddress
import java.util.*
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class BukkitSpecificPacketListenerExtension : PlatformSpecificPacketListenerExtension {
    @OptIn(NmsUseWithCaution::class)
    override val playAddress: InetSocketAddress by lazy { nmsCommonBridge.getServerIp() }

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
        plugin.launch(plugin.entityDispatcher(player)) {
            player.kick(reason)
        }
    }

    override fun silentDisconnectPlayer(playerUuid: UUID) {
        val player = Bukkit.getPlayer(playerUuid) ?: return
        SilentDisconnectListener.silentDisconnect(player)
    }

    override suspend fun teleportPlayer(
        uuid: UUID,
        location: WorldLocation,
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

    override fun registerCloudServerToProxy(client: ClientCloudServerImpl) = Unit
    override fun unregisterCloudServerFromProxy(client: ClientCloudServerImpl) = Unit

    override suspend fun teleportPlayerToPlayer(
        uuid: UUID,
        target: UUID
    ): Boolean {
        val player = Bukkit.getPlayer(uuid) ?: return false
        val targetPlayer = Bukkit.getPlayer(target) ?: return false

        return player.teleportAsync(targetPlayer.location).await()
    }

    override fun sendToast(uuid: UUID, toast: NetworkToast) {
        val player = uuid.toCloudPlayer() as? BukkitClientCloudPlayerImpl ?: return
        player.sendToast0(toast)
    }

    override fun triggerShutdown() {
        plugin.launch(plugin.globalRegionDispatcher) {
            Bukkit.shutdown()
        }
    }

    override fun restart() {
//        server.restart() TODO: figure out a way to restart a server without relying on Pterodactyl
        shutdown()
    }

    override fun shutdown() {
        server.shutdown()
    }

    @OptIn(NmsUseWithCaution::class)
    override fun setVelocitySecret(secret: ByteArray) {
        BukkitVelocitySecretManager.currentVelocityEnabled = true
        BukkitVelocitySecretManager.currentVelocitySecret = secret
        BukkitVelocitySecretManager.currentOnlineMode = false
    }

    @OptIn(NmsUseWithCaution::class)
    object BukkitVelocitySecretManager {

        @Volatile
        var currentVelocityEnabled = nmsCommonBridge.isVelocityEnabled()

        @Volatile
        var currentVelocitySecret = nmsCommonBridge
            .getVelocitySecret()
            .toByteArray()

        @Volatile
        var currentOnlineMode = server.onlineMode

        init {
            observingFlow({ nmsCommonBridge.isVelocityEnabled() })
                .onEach { remote ->
                    if (remote != currentVelocityEnabled) {
                        nmsCommonBridge.setVelocityEnabled(currentVelocityEnabled)
                    }
                }
                .launchIn(CommonObservableScope)


            observingFlow({ nmsCommonBridge.getVelocitySecret() })
                .map { it.toByteArray() }
                .onEach { remote ->
                    if (!remote.contentEquals(currentVelocitySecret)) {
                        nmsCommonBridge.setVelocitySecret(
                            currentVelocitySecret.decodeToString()
                        )
                    }
                }
                .launchIn(CommonObservableScope)

            observingFlow({ server.onlineMode })
                .onEach { remote ->
                    if (remote != currentOnlineMode) {
                        nmsCommonBridge.setOnlineMode(remote)
                    }
                }
                .launchIn(CommonObservableScope)
        }
    }
}