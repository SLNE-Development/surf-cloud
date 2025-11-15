package dev.slne.surf.cloud.api.common.player.punishment.type.note

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import io.netty.buffer.ByteBuf
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface PunishmentNote {
    val noteId: UUID
    val punishmentId: String
    val note: String
    val isBotNote: Boolean

    @Serializable
    @InternalApi
    data class PunishmentNoteImpl(
        override val noteId: @Contextual UUID = UUID.randomUUID(),
        override val punishmentId: String,
        override val note: String,
        override val isBotNote: Boolean,
    ) : PunishmentNote

    companion object {
        val STREAM_CODEC: StreamCodec<ByteBuf, PunishmentNote> = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            PunishmentNote::noteId,
            ByteBufCodecs.STRING_CODEC,
            PunishmentNote::punishmentId,
            ByteBufCodecs.STRING_CODEC,
            PunishmentNote::note,
            ByteBufCodecs.BOOLEAN_CODEC,
            PunishmentNote::isBotNote,
            ::PunishmentNoteImpl
        )
    }
}