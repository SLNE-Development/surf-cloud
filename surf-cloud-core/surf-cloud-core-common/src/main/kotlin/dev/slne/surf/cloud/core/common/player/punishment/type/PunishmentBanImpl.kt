package dev.slne.surf.cloud.core.common.player.punishment.type

import com.google.common.net.InetAddresses
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.surfapi.core.api.util.toObjectList
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
    override val parent: PunishmentBanImpl? = null,
) : AbstractUnpunishablePunishment(), PunishmentBan {
    companion object {
        private val apiType = ResolvableType.forClass(PunishmentBan::class.java)
        val STREAM_CODEC = StreamCodec.recursive { parent ->
            StreamCodec.composite(
                ByteBufCodecs.LONG_CODEC,
                PunishmentBanImpl::id,
                ByteBufCodecs.STRING_CODEC,
                PunishmentBanImpl::punishmentId,
                ByteBufCodecs.UUID_CODEC,
                PunishmentBanImpl::punishedUuid,
                ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs::nullable),
                PunishmentBanImpl::issuerUuid,
                ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
                PunishmentBanImpl::reason,
                ByteBufCodecs.BOOLEAN_CODEC,
                PunishmentBanImpl::permanent,
                ByteBufCodecs.BOOLEAN_CODEC,
                PunishmentBanImpl::securityBan,
                ByteBufCodecs.BOOLEAN_CODEC,
                PunishmentBanImpl::raw,
                ByteBufCodecs.ZONED_DATE_TIME_CODEC.apply(ByteBufCodecs::nullable),
                PunishmentBanImpl::expirationDate,
                ByteBufCodecs.ZONED_DATE_TIME_CODEC,
                PunishmentBanImpl::punishmentDate,
                ByteBufCodecs.BOOLEAN_CODEC,
                PunishmentBanImpl::unpunished,
                ByteBufCodecs.ZONED_DATE_TIME_CODEC.apply(ByteBufCodecs::nullable),
                PunishmentBanImpl::unpunishedDate,
                ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs::nullable),
                PunishmentBanImpl::unpunisherUuid,
                parent.apply(ByteBufCodecs::nullable),
                PunishmentBanImpl::parent,
                ::PunishmentBanImpl
            )
        }
        val TYPE = PunishmentTypeAndCodec(PunishmentType.BAN, STREAM_CODEC)
    }

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

    override fun getResolvableType(): ResolvableType {
        return apiType
    }

    override fun typeCodec(): PunishmentTypeAndCodec = TYPE
}