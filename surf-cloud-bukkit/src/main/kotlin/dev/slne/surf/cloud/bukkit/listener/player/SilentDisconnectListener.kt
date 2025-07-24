package dev.slne.surf.cloud.bukkit.listener.player

import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.bukkit.api.nms.listener.NmsClientboundPacketListener
import dev.slne.surf.surfapi.bukkit.api.nms.listener.packets.clientbound.DisconnectPacket
import dev.slne.surf.surfapi.bukkit.api.packet.listener.listener.PacketListenerResult
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import org.bukkit.entity.Player
import java.util.*

@OptIn(NmsUseWithCaution::class)
object SilentDisconnectListener : NmsClientboundPacketListener<DisconnectPacket> {
    private val silentDisconnects = mutableObjectSetOf<UUID>()

    override fun handleClientboundPacket(
        packet: DisconnectPacket,
        player: Player
    ): PacketListenerResult {
        if (!silentDisconnects.remove(player.uniqueId)) return PacketListenerResult.CONTINUE
        return PacketListenerResult.CANCEL
    }

    fun silentDisconnect(player: Player) {
        silentDisconnects.add(player.uniqueId)
        player.kick()
    }
}