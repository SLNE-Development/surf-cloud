package dev.slne.surf.cloud.api.common.event.offlineplayer.punishment

import dev.slne.surf.cloud.api.common.event.offlineplayer.OfflineCloudPlayerEvent
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider
import java.io.Serial

class CloudPlayerUnpunishEvent<P : Punishment>(
    source: Any,
    player: OfflineCloudPlayer,
    val punishment: P
) : OfflineCloudPlayerEvent(source, player), ResolvableTypeProvider {
    private val punishmentType = ResolvableType.forInstance(punishment)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -5256589007047048724L
    }

    override fun getResolvableType(): ResolvableType? {
        return ResolvableType.forClassWithGenerics(javaClass, punishmentType)
    }
}