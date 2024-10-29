package dev.slne.surf.cloud.api.exceptions

import io.netty.handler.codec.EncoderException
import java.io.Serial

class SkipPacketException(cause: Throwable): EncoderException(cause) {
    companion object {
        @Serial
        @JvmStatic
        private val serialVersionUID: Long = 1165045559219536149L
    }
}