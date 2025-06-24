package dev.slne.surf.cloud.standalone.player.punishment.handler

import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerPunishEvent
import dev.slne.surf.cloud.api.common.event.offlineplayer.punishment.CloudPlayerUnpunishEvent
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.util.mapAsync
import dev.slne.surf.cloud.core.common.coroutines.PunishmentHandlerScope
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(PrePlayerJoinTask.ATTACH_IP_ADDRESS_HANDLER)
class DefaultBanPunishmentHandler(private val punishmentManager: PunishmentManager) :
    PrePlayerJoinTask {
    private val log = logger()

    @CloudEventHandler
    fun onBan(event: CloudPlayerPunishEvent<PunishmentBan>) {
        val punishment = event.punishment
        if (!punishment.active) return

        val player = punishment.punishedPlayer()
        val messages = MessageManager.Punish.Ban(punishment)

        player.player?.disconnect(messages.banDisconnectComponent())
        PunishmentHandlerScope.launch {
            player.latestIpAddress()?.let { ip ->
                punishment.attachIpAddress(ip)
            }
            messages.announceBan()
        }
    }

    @CloudEventHandler
    fun onUnban(event: CloudPlayerUnpunishEvent<PunishmentBan>) {
        PunishmentHandlerScope.launch {
            MessageManager.Punish.Ban(event.punishment)
                .announceUnban()
        }
    }

    override suspend fun preJoin(player: OfflineCloudPlayer): PrePlayerJoinTask.Result {
        val cache = punishmentManager.getCurrentLoginValidationPunishmentCache(player.uuid)
        if (cache == null) {
            log.atWarning()
                .withStackTrace(StackSize.SMALL)
                .log("No Punishment cache found for player $player")
            return PrePlayerJoinTask.Result.ERROR
        }
        val player = player as StandaloneCloudPlayerImpl
        val activeBans = cache.bans.filter { it.active }
        activeBans.mapAsync { it.attachIpAddress(player.ip) }.awaitAll()

        return PrePlayerJoinTask.Result.ALLOWED
    }
}