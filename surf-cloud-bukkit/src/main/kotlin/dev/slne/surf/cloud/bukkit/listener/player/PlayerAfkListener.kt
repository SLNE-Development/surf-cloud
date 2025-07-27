package dev.slne.surf.cloud.bukkit.listener.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional.UpdateAFKStatePacket
import dev.slne.surf.surfapi.core.api.util.mutableObject2BooleanMapOf
import dev.slne.surf.surfapi.core.api.util.mutableObject2LongMapOf
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@Component
class PlayerAfkListener : Listener {
    private val afkTime = 10.seconds.inWholeMilliseconds
    private val lastMovedTime = mutableObject2LongMapOf<UUID>()
    private val currentSentState = mutableObject2BooleanMapOf<UUID>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasChangedOrientation()) return
        if (!event.hasChangedPosition()) return
        lastMovedTime[event.player.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        lastMovedTime[event.player.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        lastMovedTime.removeLong(uuid)
        currentSentState.removeBoolean(uuid)
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    fun afkCheckTask() {
        val currentTime = System.currentTimeMillis()

        lastMovedTime.object2LongEntrySet().fastForEach { entry ->
            val uuid = entry.key
            val lastMoved = entry.longValue
            val timeSinceLastMove = currentTime - lastMoved
            val isAfk = timeSinceLastMove >= afkTime
            val previousState = currentSentState.put(uuid, isAfk)
            if (previousState != isAfk) {
                broadcastChange(uuid, isAfk)
            }
        }
    }

    private fun broadcastChange(uuid: UUID, isAfk: Boolean) {
        UpdateAFKStatePacket(uuid, isAfk).fireAndForget()
    }
}