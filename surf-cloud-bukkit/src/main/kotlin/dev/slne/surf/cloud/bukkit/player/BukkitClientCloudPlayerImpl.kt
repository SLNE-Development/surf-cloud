package dev.slne.surf.cloud.bukkit.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.client.paper.toBukkitTpCause
import dev.slne.surf.cloud.api.client.paper.toBukkitTpFlag
import dev.slne.surf.cloud.api.client.paper.toLocation
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.WorldLocation
import dev.slne.surf.cloud.api.common.player.toast.NetworkToast
import dev.slne.surf.cloud.bukkit.listener.player.SilentDisconnectListener
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.DisconnectPlayerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SilentDisconnectPlayerPacket
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.packets.player.toast.Toast
import dev.slne.surf.surfapi.core.api.util.logger
import io.papermc.paper.advancement.AdvancementDisplay
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class BukkitClientCloudPlayerImpl(uuid: UUID, name: String) : ClientCloudPlayerImpl<Player>(
    uuid,
    name
) {
    public override val audience: Player? get() = Bukkit.getPlayer(uuid)
    override val platformClass: Class<Player> = Player::class.java

    override fun disconnect(reason: Component) {
        DisconnectPlayerPacket(uuid, reason).fireAndForget()
    }

    override fun disconnectSilent() {
        SilentDisconnectListener.silentDisconnect(
            audience ?: return SilentDisconnectPlayerPacket(uuid).fireAndForget()
        )
    }

    override suspend fun teleport(
        location: WorldLocation,
        teleportCause: TeleportCause,
        vararg flags: TeleportFlag
    ): Boolean {
        val player = audience ?: return super.teleport(location, teleportCause, *flags)

        val bukkitTeleportFlags = flags.map { it.toBukkitTpFlag() }.toTypedArray()
        return player.teleportAsync(
            location.toLocation(),
            teleportCause.toBukkitTpCause(),
            *bukkitTeleportFlags
        ).await()
    }

    override fun sendToast(toast: NetworkToast) {
        if (!sendToast0(toast)) {
            super.sendToast(toast)
        }
    }

    @OptIn(NmsUseWithCaution::class)
    fun sendToast0(networkToast: NetworkToast): Boolean {
        val player = audience ?: return false

        val toastIcon = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.ITEM)
            .get(networkToast.icon)

        if (toastIcon == null) {
            log.atWarning()
                .log("No item found for icon ${networkToast.icon}")

            return false
        }

        val frame = when (networkToast.frame) {
            NetworkToast.Frame.TASK -> AdvancementDisplay.Frame.TASK
            NetworkToast.Frame.CHALLENGE -> AdvancementDisplay.Frame.CHALLENGE
            NetworkToast.Frame.GOAL -> AdvancementDisplay.Frame.GOAL
        }

        val toast = Toast(toastIcon.createItemStack(), networkToast.title, frame)
        toast.createOperation().execute(player)

        return true
    }

    companion object {
        private val log = logger()
    }
}