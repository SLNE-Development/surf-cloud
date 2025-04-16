package dev.slne.surf.cloud.core.common.netty.network

import com.mojang.serialization.Codec
import dev.slne.surf.cloud.api.common.util.asOptional
import dev.slne.surf.cloud.api.common.util.codec.createRecordCodec
import dev.slne.surf.cloud.api.common.util.codec.getter
import kotlin.jvm.optionals.getOrNull

data class DisconnectionDetails(val reason: DisconnectReason, val additionalInfo: String? = null) {
    companion object {
        val CODEC = createRecordCodec<DisconnectionDetails> {
            group(
                Codec.STRING.fieldOf("reason").getter { reason.name },
                Codec.STRING.optionalFieldOf("additionalInfo")
                    .getter { additionalInfo.asOptional() }
            ).apply(this) { reason, additionalInfo ->
                DisconnectionDetails(
                    DisconnectReason.valueOf(
                        reason
                    ), additionalInfo.getOrNull()
                )
            }
        }
    }

    fun buildMessage(): String {
        return if (additionalInfo != null) {
            "${reason.message}: $additionalInfo"
        } else {
            reason.message
        }
    }
}

enum class DisconnectReason(val message: String, val shouldRestart: Boolean = true) {
    UNKNOWN("Disconnected", true),
    TIMEOUT("Timed out", true),
    INTERNAL_EXCEPTION("Internal exception", true),
    END_OF_STREAM("End of stream", true),
    SERVER_ID_FETCHED("Server ID fetched", false),
    CLIENT_SHUTDOWN("Client shutdown", false),
    CLIENT_NAME_ALREADY_EXISTS("Client name already exists", false),
    SERVER_SHUTDOWN("Server is shutting down", true),
    OUTDATED_SERVER("Outdated server", false),
    OUTDATED_CLIENT("Outdated client", false),
    PROXY_ALREADY_CONNECTED("Proxy already connected", false),
    TOOK_TOO_LONG("Took too long to log in", false),
}
