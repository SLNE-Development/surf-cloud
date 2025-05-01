package dev.slne.surf.cloud.bukkit.listener.punish.mute

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent
import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.common.util.measureWithStopWatch
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import org.bukkit.entity.Player
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(VoicechatPlugin::class)
class VoicechatMuteListener : CloudLifecycleAware {

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureWithStopWatch("Register Voicechat Mute Listener") {
            server.servicesManager.load(BukkitVoicechatService::class.java)
                ?.registerPlugin(MuteVoicechatPlugin())
        }
    }

    class MuteVoicechatPlugin : AbstractMuteListener(), VoicechatPlugin {
        override fun getPluginId() = "surf-cloud-mute-plugin"

        override fun registerEvents(registration: EventRegistration) {
            registration.registerEvent(MicrophonePacketEvent::class.java) { event -> // TODO: 19.04.2025 11:47 - performance?
                val serverPlayer = event.senderConnection?.player
                val playerUuid = serverPlayer?.uuid ?: return@registerEvent

                if (isMuted(playerUuid)) {
                    event.cancel()
                    val paperPlayer =
                        serverPlayer.player as? Player ?: error("Player is not a Paper player")
                    paperPlayer.sendActionBar(MessageManager.Punish.Mute.voiceMutedActionbar)
                }
            }
        }
    }
}