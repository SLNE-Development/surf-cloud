package dev.slne.surf.cloud.api.common.netty.exception

import java.io.Serial

/**
 * Exception indicating a failure to register a Netty listener.
 */
class SurfNettyListenerRegistrationException : SurfNettyListenerException {
    constructor() : super()
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        @JvmStatic
        @Serial
        private val serialVersionUID = 4077119187296696024L
    }
}
