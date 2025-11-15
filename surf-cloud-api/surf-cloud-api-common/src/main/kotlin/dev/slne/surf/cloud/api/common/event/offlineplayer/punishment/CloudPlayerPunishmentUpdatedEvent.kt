package dev.slne.surf.cloud.api.common.event.offlineplayer.punishment

import dev.slne.surf.cloud.api.common.event.offlineplayer.OfflineCloudPlayerEvent
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import dev.slne.surf.surfapi.core.api.util.int2ObjectMapOf
import io.netty.handler.codec.DecoderException
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider
import java.io.Serial

class CloudPlayerPunishmentUpdatedEvent<P : Punishment>(
    source: Any,
    player: OfflineCloudPlayer,
    val updatedPunishment: P,
    val operation: Operation
) : OfflineCloudPlayerEvent(source, player), ResolvableTypeProvider {
    private val punishmentType = ResolvableType.forInstance(updatedPunishment)

    companion object {
        @Serial
        val serialVersionUID: Long = 131882253452738926L
    }

    override fun getResolvableType(): ResolvableType {
        return ResolvableType.forClassWithGenerics(javaClass, punishmentType)
    }

    @Suppress("ClassName")
    sealed interface Operation {
        val type: Type

        object ADMIN_PANEL : Operation {
            val TYPE = Type(0)
            val STREAM_CODEC = streamCodecUnitSimple(this)
            override val type = TYPE
        }

        class NOTE_ADDED(val note: PunishmentNote) : Operation {
            companion object {
                val TYPE = Type(1)
                val STREAM_CODEC = StreamCodec.composite(
                    PunishmentNote.STREAM_CODEC,
                    NOTE_ADDED::note,
                    ::NOTE_ADDED
                )
            }

            override val type = TYPE
        }

        class ATTACH_IP(val ip: PunishmentAttachedIpAddress) : Operation {
            companion object {
                val TYPE = Type(2)
                val STREAM_CODEC = StreamCodec.composite(
                    PunishmentAttachedIpAddress.STREAM_CODEC,
                    ATTACH_IP::ip,
                    ::ATTACH_IP
                )
            }

            override val type = TYPE
        }

        @JvmInline
        value class Type(val id: Int)

        companion object {
            private val TYPES = int2ObjectMapOf(
                ADMIN_PANEL.TYPE.id to ADMIN_PANEL.STREAM_CODEC,
                NOTE_ADDED.TYPE.id to NOTE_ADDED.STREAM_CODEC,
                ATTACH_IP.TYPE.id to ATTACH_IP.STREAM_CODEC,
            )

            val STREAM_CODEC = ByteBufCodecs.VAR_INT_CODEC.dispatch(
                { it.type.id },
                {
                    TYPES.get(it)
                        ?: throw DecoderException("Unknown CloudPlayerPunishmentUpdatedEvent.Operation type id: $it")
                }
            )
        }
    }
}