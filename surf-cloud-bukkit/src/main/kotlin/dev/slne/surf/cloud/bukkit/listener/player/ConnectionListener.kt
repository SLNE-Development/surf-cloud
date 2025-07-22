package dev.slne.surf.cloud.bukkit.listener.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwait
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerConnectToServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerDisconnectFromServerPacket
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.net.Inet4Address
import kotlin.time.Duration.Companion.seconds

object ConnectionListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun AsyncPlayerPreLoginEvent.onAsyncPlayerPreLogin() {
        if (loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return
        }


        /**
         * We are in an async event wich uses a cached thread pool,
         * so we can use runBlocking here without blocking the main thread
         */
        runBlocking {
            val result = PlayerConnectToServerPacket(
                uniqueId,
                name,
                CloudPersistentData.SERVER_ID,
                false,
                address as? Inet4Address
                    ?: error("Player address is not an Inet4Address")
            ).fireAndAwait(30.seconds)?.result
                ?: PrePlayerJoinTask.Result.DENIED(MessageManager.loginTimedOut)

            if (result !is PrePlayerJoinTask.Result.ALLOWED) {
                disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    if (result is PrePlayerJoinTask.Result.DENIED) result.reason else MessageManager.unknownErrorDuringLogin
                )
            }
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onPlayerQuit() {
        PlayerDisconnectFromServerPacket(
            player.uniqueId,
            CloudPersistentData.SERVER_ID,
            false
        ).fireAndForget()
    }
}