package dev.slne.surf.cloud.velocity.listener.player

import com.velocitypowered.api.event.ResultedEvent.ComponentResult
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwait
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerConnectToServerPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerDisconnectFromServerPacket
import dev.slne.surf.cloud.core.common.player.task.PrePlayerJoinTask.Result
import org.springframework.stereotype.Component
import java.net.Inet4Address
import kotlin.time.Duration.Companion.seconds

@Component
class ConnectionListener {

    @Subscribe(priority = Short.MAX_VALUE)
    suspend fun onLogin(event: LoginEvent) {
        val player = event.player

        val result = PlayerConnectToServerPacket(
            player.uniqueId,
            player.username,
            CloudPersistentData.SERVER_ID,
            true,
            player.remoteAddress.address as? Inet4Address
                ?: error("Player address is not an Inet4Address")
        ).fireAndAwait(30.seconds)?.result ?: Result.DENIED(MessageManager.loginTimedOut)

        if (result !is Result.ALLOWED) {
            event.result =
                ComponentResult.denied(if (result is Result.DENIED) result.reason else MessageManager.unknownErrorDuringLogin)
        }
    }

    @Subscribe(priority = Short.MAX_VALUE)
    fun onDisconnect(event: DisconnectEvent) {
        PlayerDisconnectFromServerPacket(
            event.player.uniqueId,
            CloudPersistentData.SERVER_ID,
            true
        ).fireAndForget()
    }
}