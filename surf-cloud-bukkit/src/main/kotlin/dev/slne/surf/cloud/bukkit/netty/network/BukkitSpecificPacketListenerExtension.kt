package dev.slne.surf.cloud.bukkit.netty.network

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.util.observer.observingFlow
import dev.slne.surf.cloud.bukkit.listener.player.SilentDisconnectListener
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import dev.slne.surf.cloud.core.client.server.ClientCloudServerImpl
import dev.slne.surf.cloud.core.common.coroutines.CommonObservableScope
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RegistrationInfo
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundTransferPlayerPacketResponse
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.nmsCommonBridge
import dev.slne.surf.surfapi.bukkit.api.util.dispatcher
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class BukkitSpecificPacketListenerExtension : PlatformSpecificPacketListenerExtension {
    private val properties by lazy {
        val propertiesPath = Path("server.properties")
        if (propertiesPath.notExists()) {
            Properties()
        } else {
            Properties().apply {
                propertiesPath.inputStream().use { inputStream ->
                    load(inputStream)
                }
            }
        }
    }

    override val playAddress: InetSocketAddress by lazy {
        InetSocketAddress(InetAddress.getByName(server.ip), server.port)
    }

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
        plugin.launch(player.dispatcher()) {
            player.kick(reason)
        }
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

    @OptIn(NmsUseWithCaution::class)
    override fun setVelocitySecret(secret: ByteArray) {
//        nmsCommonBridge.setVelocityEnabled(true)
//        nmsCommonBridge.setVelocitySecret(secret.toString(StandardCharsets.UTF_8))
//        nmsCommonBridge.setOnlineMode(false)

        BukkitVelocitySecretManager.currentVelocityEnabled = true
        BukkitVelocitySecretManager.currentVelocitySecret = secret
        BukkitVelocitySecretManager.currentOnlineMode = false
    }

    override fun triggerShutdown() {
        plugin.launch(plugin.globalRegionDispatcher) {
            Bukkit.shutdown()
        }
    }

    override fun restart() {
        server.restart()
    }

    override fun shutdown() {
        server.shutdown()
    }

    @OptIn(NmsUseWithCaution::class)
    object BukkitVelocitySecretManager {

        @Volatile
        var currentVelocityEnabled = nmsCommonBridge.isVelocityEnabled()

        @Volatile
        var currentVelocitySecret = nmsCommonBridge
            .getVelocitySecret()
            .toByteArray(StandardCharsets.UTF_8)

        @Volatile
        var currentOnlineMode = server.onlineMode

        init {
            observingFlow({ nmsCommonBridge.isVelocityEnabled() })
                .onEach { remote ->
                    if (remote != currentVelocityEnabled) {
                        println("Updating Velocity enabled state: $remote")
                        nmsCommonBridge.setVelocityEnabled(currentVelocityEnabled)
                    }
                }
                .launchIn(CommonObservableScope)


            observingFlow({ nmsCommonBridge.getVelocitySecret() })
                .map { it.toByteArray(StandardCharsets.UTF_8) }
                .onEach { remote ->
                    if (!remote.contentEquals(currentVelocitySecret)) {
                        println("Updating Velocity secret: ${remote.toString(StandardCharsets.UTF_8)}")
                        nmsCommonBridge.setVelocitySecret(
                            currentVelocitySecret.toString(
                                StandardCharsets.UTF_8
                            )
                        )
                    }
                }
                .launchIn(CommonObservableScope)

            observingFlow({ server.onlineMode })
                .onEach { remote ->
                    if (remote != currentOnlineMode) {
                        println("Updating online mode: $remote")
                        nmsCommonBridge.setOnlineMode(remote)
                    }
                }
                .launchIn(CommonObservableScope)
        }
    }
}