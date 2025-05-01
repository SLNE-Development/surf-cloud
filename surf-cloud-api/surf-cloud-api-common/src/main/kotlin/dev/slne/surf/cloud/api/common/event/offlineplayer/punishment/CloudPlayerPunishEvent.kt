package dev.slne.surf.cloud.api.common.event.offlineplayer.punishment

import dev.slne.surf.cloud.api.common.event.offlineplayer.OfflineCloudPlayerEvent
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

class CloudPlayerPunishEvent<P : Punishment>(
    source: Any,
    player: OfflineCloudPlayer,
    val punishment: P
) : OfflineCloudPlayerEvent(source, player), ResolvableTypeProvider {
    private val punishmentType = ResolvableType.forInstance(punishment)

    companion object {
        private const val serialVersionUID: Long = 131882253452738926L
    }

    override fun getResolvableType(): ResolvableType? {
        return ResolvableType.forClassWithGenerics(javaClass, punishmentType)
    }
}