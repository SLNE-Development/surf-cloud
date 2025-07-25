package dev.slne.surf.cloud.api.common.netty.exception

import java.io.Serial

/**
 * Base exception for packet-related Netty errors.
 */
abstract class SurfNettyPacketException : SurfNettyException {
    constructor() : super()
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)


    companion object {
        @JvmStatic
        @Serial
        private val serialVersionUID = -7658527490298368796L
    }
}
