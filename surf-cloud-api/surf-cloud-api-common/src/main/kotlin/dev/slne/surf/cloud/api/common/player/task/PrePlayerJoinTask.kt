package dev.slne.surf.cloud.api.common.player.task

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.common.util.int2ObjectMapOf
import io.netty.handler.codec.DecoderException
import kotlinx.serialization.Contextual
import net.kyori.adventure.text.Component

interface PrePlayerJoinTask {
    suspend fun preJoin(player: OfflineCloudPlayer): Result

    sealed interface Result {
        val type: Type

        data object ALLOWED : Result {
            val TYPE = Type(0)
            val STREAM_CODEC = streamCodecUnitSimple(this)
            override val type = TYPE
        }

        data class DENIED(val reason: @Contextual Component) : Result {
            override val type = TYPE

            companion object {
                val TYPE = Type(1)
                val STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.COMPONENT_CODEC,
                    DENIED::reason,
                    ::DENIED
                )
            }
        }

        data object ERROR : Result {
            val TYPE = Type(2)
            val STREAM_CODEC = streamCodecUnitSimple(this)
            override val type = TYPE
        }

        @JvmInline
        value class Type(val id: Int)

        companion object {
            private val TYPES = int2ObjectMapOf(
                ALLOWED.TYPE.id to ALLOWED.STREAM_CODEC,
                DENIED.TYPE.id to DENIED.STREAM_CODEC,
                ERROR.TYPE.id to ERROR.STREAM_CODEC
            )

            val STREAM_CODEC = ByteBufCodecs.VAR_INT_CODEC.dispatch(
                { it.type.id },
                {
                    TYPES.get(it)
                        ?: throw DecoderException("Unknown PrePlayerJoinTask.Result type id: $it")
                }
            )
        }
    }

    @InternalApi
    companion object {
        const val PUNISHMENT_MANAGER = 500
        const val VELOCITY_PLAYER_JOIN_VALIDATION = 600
        const val MUTE_PUNISHMENT_LISTENER = 750
        const val ATTACH_IP_ADDRESS_HANDLER = 900
        const val PUNISHMENT_LOGIN_VALIDATION_HANDLER = 1000
    }
}