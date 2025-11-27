package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.util.ByIdMap

data class DisconnectionDetails(val reason: DisconnectReason, val additionalInfo: String? = null) {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            DisconnectReason.STREAM_CODEC,
            DisconnectionDetails::reason,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            DisconnectionDetails::additionalInfo,
            ::DisconnectionDetails
        )
    }

    fun buildMessage(): String {
        return if (additionalInfo != null) {
            "${reason.message}: $additionalInfo"
        } else {
            reason.message
        }
    }

    fun createException() = IllegalStateException(buildMessage())
}

enum class DisconnectReason(
    val id: Int,
    val message: String,
    val shouldReconnect: Boolean = true
) {
    UNKNOWN(0, "Disconnected", true),
    TIMEOUT(1, "Timed out", true),
    INTERNAL_EXCEPTION(2, "Internal exception", true),
    END_OF_STREAM(3, "End of stream", true),
    SERVER_ID_FETCHED(4, "Server ID fetched", false),
    CLIENT_SHUTDOWN(5, "Client shutdown", false),
    CLIENT_NAME_ALREADY_EXISTS(6, "Client name already exists", true),
    SERVER_SHUTDOWN(7, "Server is shutting down", true),
    OUTDATED_SERVER(8, "Outdated server", false),
    OUTDATED_CLIENT(9, "Outdated client", false),
    TOOK_TOO_LONG(10, "Took too long to log in", true);

    companion object {
        val BY_ID = ByIdMap.continuous(
            DisconnectReason::id,
            entries.toTypedArray(),
            ByIdMap.OutOfBoundsStrategy.ZERO
        )

        val STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DisconnectReason::id)
    }
}
