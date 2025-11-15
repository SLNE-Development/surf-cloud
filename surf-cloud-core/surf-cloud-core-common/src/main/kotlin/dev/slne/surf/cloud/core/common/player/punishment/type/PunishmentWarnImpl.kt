package dev.slne.surf.cloud.core.common.player.punishment.type

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import dev.slne.surf.cloud.api.common.player.punishment.type.warn.PunishmentWarn
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
data class PunishmentWarnImpl(
    val id: Long,
    override val punishmentId: String,
    override val punishedUuid: @Contextual UUID,
    override val issuerUuid: @Contextual UUID?,
    override val reason: String?,

    override val punishmentDate: @Contextual ZonedDateTime = ZonedDateTime.now(),
    override val parent: PunishmentWarnImpl? = null,
) : AbstractPunishment(), PunishmentWarn {
    companion object {
        private val apiType = ResolvableType.forClass(PunishmentWarn::class.java)

        val STREAM_CODEC = StreamCodec.recursive { parent ->
            StreamCodec.composite(
                ByteBufCodecs.LONG_CODEC,
                PunishmentWarnImpl::id,
                ByteBufCodecs.STRING_CODEC,
                PunishmentWarnImpl::punishmentId,
                ByteBufCodecs.UUID_CODEC,
                PunishmentWarnImpl::punishedUuid,
                ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs::nullable),
                PunishmentWarnImpl::issuerUuid,
                ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
                PunishmentWarnImpl::reason,
                ByteBufCodecs.ZONED_DATE_TIME_CODEC,
                PunishmentWarnImpl::punishmentDate,
                parent.apply(ByteBufCodecs::nullable),
                PunishmentWarnImpl::parent,
                ::PunishmentWarnImpl
            )
        }

        val TYPE = PunishmentTypeAndCodec(PunishmentType.WARN, STREAM_CODEC)
    }

    override val punishmentUrlReplacer: String = "warns"

    override fun punishmentPlayerComponent(): Component {
        return MessageManager.Punish.Warn(this).warnComponent()
    }

    override suspend fun addNote(note: String): PunishmentNote {
        return bean<PunishmentManager>().attachNoteToWarn(id, note)
    }

    override suspend fun fetchNotes(): ObjectList<out PunishmentNote> {
        return bean<PunishmentManager>().fetchNotesForWarn(id).toObjectList()
    }

    override fun getResolvableType(): ResolvableType {
        return apiType
    }

    override fun typeCodec(): PunishmentTypeAndCodec = TYPE
}