package dev.slne.surf.cloud.api.netty.exception

import java.io.Serial


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
