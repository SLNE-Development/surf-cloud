package dev.slne.surf.cloud.bukkit.listener.player

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.bukkit.api.nms.listener.NmsClientboundPacketListener
import dev.slne.surf.surfapi.bukkit.api.nms.listener.packets.clientbound.DisconnectPacket
import dev.slne.surf.surfapi.bukkit.api.packet.listener.listener.PacketListenerResult
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@OptIn(NmsUseWithCaution::class)
object SilentDisconnectListener : NmsClientboundPacketListener<DisconnectPacket> {
    private val silentDisconnects = ConcurrentHashMap.newKeySet<UUID>()

    override fun handleClientboundPacket(
        packet: DisconnectPacket,
        player: Player
    ): PacketListenerResult {
        if (!silentDisconnects.remove(player.uniqueId)) return PacketListenerResult.CONTINUE
        return PacketListenerResult.CANCEL
    }

    fun silentDisconnect(player: Player) {
        silentDisconnects.add(player.uniqueId)
        plugin.launch(plugin.entityDispatcher(player)) {
            player.kick()
        }
    }
}