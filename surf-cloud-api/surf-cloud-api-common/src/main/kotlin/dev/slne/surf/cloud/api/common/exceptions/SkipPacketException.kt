package dev.slne.surf.cloud.api.common.exceptions

import io.netty.handler.codec.EncoderException
import java.io.Serial

/**
 * Exception used to indicate that a packet should be skipped during encoding.
 *
 * @param cause The root cause of the exception.
 */
class SkipPacketException(cause: Throwable) : EncoderException(cause) {
    companion object {
        @Serial
        @JvmStatic
        private val serialVersionUID: Long = 1165045559219536149L
    }
}