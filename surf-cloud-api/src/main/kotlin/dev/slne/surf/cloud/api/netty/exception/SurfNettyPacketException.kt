package dev.slne.surf.cloud.api.netty.exception

import java.io.Serial


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
