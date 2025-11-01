package dev.slne.surf.cloud.api.common.event.offlineplayer.punishment

import dev.slne.surf.cloud.api.common.event.offlineplayer.OfflineCloudPlayerEvent
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import kotlinx.serialization.Serializable
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

class CloudPlayerPunishmentUpdatedEvent<P : Punishment>(
    source: Any,
    player: OfflineCloudPlayer,
    val updatedPunishment: P,
    val operation: Operation
) : OfflineCloudPlayerEvent(source, player), ResolvableTypeProvider {
    private val punishmentType = ResolvableType.forInstance(updatedPunishment)

    companion object {
        private const val serialVersionUID: Long = 131882253452738926L
    }

    override fun getResolvableType(): ResolvableType {
        return ResolvableType.forClassWithGenerics(javaClass, punishmentType)
    }

    @Suppress("ClassName")
    @Serializable
    sealed interface Operation {
        @Serializable
        object ADMIN_PANEL : Operation

        @Serializable
        class NOTE_ADDED(val note: PunishmentNote) : Operation

        @Serializable
        class ATTACH_IP(val ip: PunishmentAttachedIpAddress) : Operation
    }
}