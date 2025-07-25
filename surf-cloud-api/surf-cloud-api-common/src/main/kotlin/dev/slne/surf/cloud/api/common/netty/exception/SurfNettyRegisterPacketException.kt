package dev.slne.surf.cloud.api.common.netty.exception

import java.io.Serial

/**
 * Exception indicating a failure to register a Netty packet.
 */
class SurfNettyRegisterPacketException : SurfNettyPacketException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        @Serial
        private val serialVersionUID = -5234200008867680129L
    }
}
