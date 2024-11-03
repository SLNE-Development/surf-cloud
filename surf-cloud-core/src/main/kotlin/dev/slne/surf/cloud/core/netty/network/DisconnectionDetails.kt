package dev.slne.surf.cloud.core.netty.network

import com.mojang.serialization.Codec
import dev.slne.surf.cloud.api.util.codec.createRecordCodec
import dev.slne.surf.cloud.api.util.codec.getter

data class DisconnectionDetails(val reason: String) {
    companion object {
        val CODEC = createRecordCodec<DisconnectionDetails> {
            group(
                Codec.STRING.fieldOf("reason").getter { reason }
            ).apply(this, ::DisconnectionDetails)
        }
    }
}


