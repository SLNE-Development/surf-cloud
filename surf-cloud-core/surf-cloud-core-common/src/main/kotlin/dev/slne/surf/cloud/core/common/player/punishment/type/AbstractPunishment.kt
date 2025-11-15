package dev.slne.surf.cloud.core.common.player.punishment.type

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkEncoded
import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.surfapi.core.api.util.object2ObjectMapOf
import io.netty.buffer.ByteBuf
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@Serializable
sealed class AbstractPunishment() : Punishment {

    abstract override val parent: AbstractPunishment?
    abstract override val punishedUuid: UUID
    abstract override val punishmentDate: ZonedDateTime
    abstract override val punishmentId: String

    abstract val punishmentUrlReplacer: String
    override val adminPanelLink: String =
        "https://admin.slne.dev/core/user/$punishedUuid/$punishmentUrlReplacer/$punishmentId"

    final override fun punishedPlayer(): CommonOfflineCloudPlayerImpl =
        punishedUuid.toOfflineCloudPlayer() as CommonOfflineCloudPlayerImpl

    final override fun issuerPlayer() = issuerUuid.toOfflineCloudPlayer()

    abstract fun typeCodec(): PunishmentTypeAndCodec

    companion object {
        private val TYPES = object2ObjectMapOf(
            PunishmentType.KICK to PunishmentKickImpl.STREAM_CODEC,
            PunishmentType.WARN to PunishmentWarnImpl.STREAM_CODEC,
            PunishmentType.MUTE to PunishmentMuteImpl.STREAM_CODEC,
            PunishmentType.BAN to PunishmentBanImpl.STREAM_CODEC,
        )

        @Suppress("UNCHECKED_CAST")
        val STREAM_CODEC = StreamCodec.ofMember<ByteBuf, AbstractPunishment>(
            { p, buf ->
                PunishmentType.STREAM_CODEC.encode(buf, p.type)
                val codec = TYPES[p.type] as? StreamCodec<ByteBuf, AbstractPunishment>
                checkEncoded(codec != null) { "Unknown PunishmentType: ${p.type}" }
                codec.encode(buf, p)
            }, { buf ->
                val type = PunishmentType.STREAM_CODEC.decode(buf)
                val codec = TYPES[type]
                checkEncoded(codec != null) { "Unknown PunishmentType: $type" }
                codec.decode(buf)
            }
        )
    }
}