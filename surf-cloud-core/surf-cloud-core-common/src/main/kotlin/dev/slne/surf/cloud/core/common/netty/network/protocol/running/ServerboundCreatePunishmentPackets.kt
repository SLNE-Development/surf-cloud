package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.AbstractPunishment
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@SurfNettyPacket(
    "cloud:serverbound:create_kick",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundCreateKickPacket(
    val punishedUuid: UUID,
    val issuerUuid: UUID?,
    val reason: String?,
    val initialNotes: MutableList<String>,
    val parentId: Long?
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundCreateKickPacket::punishedUuid,
            ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateKickPacket::issuerUuid,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateKickPacket::reason,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs.list()),
            ServerboundCreateKickPacket::initialNotes,
            ByteBufCodecs.LONG_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateKickPacket::parentId,
            ::ServerboundCreateKickPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleCreateKick(this)
    }
}

@SurfNettyPacket(
    "cloud:serverbound:create_warn",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundCreateWarnPacket(
    val punishedUuid: UUID,
    val issuerUuid: UUID?,
    val reason: String?,
    val initialNotes: MutableList<String>,
    val parentId: Long?
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundCreateWarnPacket::punishedUuid,
            ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateWarnPacket::issuerUuid,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateWarnPacket::reason,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs.list()),
            ServerboundCreateWarnPacket::initialNotes,
            ByteBufCodecs.LONG_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateWarnPacket::parentId,
            ::ServerboundCreateWarnPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleCreateWarn(this)
    }
}

@SurfNettyPacket(
    "cloud:serverbound:create_mute",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundCreateMutePacket(
    val punishedUuid: UUID,
    val issuerUuid: UUID?,
    val reason: String?,
    val permanent: Boolean,
    val expirationDate: ZonedDateTime?,
    val initialNotes: MutableList<String>,
    val parentId: Long?
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundCreateMutePacket::punishedUuid,
            ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateMutePacket::issuerUuid,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateMutePacket::reason,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundCreateMutePacket::permanent,
            ByteBufCodecs.ZONED_DATE_TIME_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateMutePacket::expirationDate,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs.list()),
            ServerboundCreateMutePacket::initialNotes,
            ByteBufCodecs.LONG_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateMutePacket::parentId,
            ::ServerboundCreateMutePacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleCreateMute(this)
    }
}

@SurfNettyPacket(
    "cloud:serverbound:create_ban",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundCreateBanPacket(
    val punishedUuid: UUID,
    val issuerUuid: UUID?,
    val reason: String?,
    val permanent: Boolean,
    val expirationDate: ZonedDateTime?,
    val securityBan: Boolean,
    val raw: Boolean,
    val initialNotes: MutableList<String>,
    val initialIpAddresses: MutableList<String>,
    val parentId: Long?
) : RespondingNettyPacket<ClientboundCreatedPunishmentResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundCreateBanPacket::punishedUuid,
            ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateBanPacket::issuerUuid,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateBanPacket::reason,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundCreateBanPacket::permanent,
            ByteBufCodecs.ZONED_DATE_TIME_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateBanPacket::expirationDate,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundCreateBanPacket::securityBan,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundCreateBanPacket::raw,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs.list()),
            ServerboundCreateBanPacket::initialNotes,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs.list()),
            ServerboundCreateBanPacket::initialIpAddresses,
            ByteBufCodecs.LONG_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundCreateBanPacket::parentId,
            ::ServerboundCreateBanPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleCreateBan(this)
    }
}

@Serializable
@SurfNettyPacket("cloud:clientbound:created_punishment_response", PacketFlow.CLIENTBOUND)
class ClientboundCreatedPunishmentResponsePacket(val punishment: AbstractPunishment) :
    ResponseNettyPacket()