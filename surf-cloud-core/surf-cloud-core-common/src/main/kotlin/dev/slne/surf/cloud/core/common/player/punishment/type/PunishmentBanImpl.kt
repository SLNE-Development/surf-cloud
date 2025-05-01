package dev.slne.surf.cloud.core.common.player.punishment.type

import com.google.common.net.InetAddresses
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import dev.slne.surf.cloud.api.common.util.toObjectList
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.util.bean
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.springframework.core.ResolvableType
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class PunishmentBanImpl(
    val id: Long,
    override val punishmentId: String,
    override val punishedUuid: @Contextual UUID,
    override val issuerUuid: @Contextual UUID?,
    override val reason: String?,
    override val permanent: Boolean = false,
    override val securityBan: Boolean = false,
    override val raw: Boolean = false,

    override val expirationDate: @Contextual ZonedDateTime? = null,
    override val punishmentDate: @Contextual ZonedDateTime = ZonedDateTime.now(),
    override val unpunished: Boolean = false,
    override val unpunishedDate: @Contextual ZonedDateTime? = null,
    override val unpunisherUuid: @Contextual UUID? = null,
) : AbstractUnpunishablePunishment(), PunishmentBan {
    override val punishmentUrlReplacer: String = "bans"

    override suspend fun attachIpAddress(rawIp: String): Boolean {
        require(InetAddresses.isInetAddress(rawIp)) { "Invalid IP address: $rawIp" }
       return bean<PunishmentManager>().attachIpAddressToBan(id, rawIp)
    }

    override suspend fun fetchIpAddresses(): Set<PunishmentAttachedIpAddress> {
        return bean<PunishmentManager>().fetchIpAddressesForBan(id).toSet()
    }

    override fun punishmentPlayerComponent(): Component {
        return MessageManager.Punish.Ban(this).banDisconnectComponent()
    }

    override suspend fun addNote(note: String): PunishmentNote {
       return bean<PunishmentManager>().attachNoteToBan(id, note)
    }

    override suspend fun fetchNotes(): ObjectList<out PunishmentNote> {
        return bean<PunishmentManager>().fetchNotesForBan(id).toObjectList()
    }

    override fun getResolvableType(): ResolvableType? {
        return apiType
    }

    companion object {
        private val apiType = ResolvableType.forClass(PunishmentBan::class.java)
    }
}