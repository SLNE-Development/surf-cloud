package dev.slne.surf.cloud.api.common.netty.exception

import java.io.Serial

/**
 * Base exception for Netty-related errors in the Surf Cloud application.
 */
abstract class SurfNettyException : RuntimeException {
    constructor() : super()
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        @JvmStatic
        @Serial
        private val serialVersionUID = 63872266580221436L
    }
}
