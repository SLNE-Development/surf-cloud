package dev.slne.surf.cloud.bukkit.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.client.paper.toBukkitTpCause
import dev.slne.surf.cloud.api.client.paper.toBukkitTpFlag
import dev.slne.surf.cloud.api.client.paper.toLocation
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag
import dev.slne.surf.cloud.api.common.player.teleport.WorldLocation
import dev.slne.surf.cloud.bukkit.listener.player.SilentDisconnectListener
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.DisconnectPlayerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.SilentDisconnectPlayerPacket
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
}