package dev.slne.surf.cloud.api.netty.exception

import java.io.Serial


class SurfNettyPacketNotRegisteredException : SurfNettyPacketException {
    constructor() : super()
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {

        @JvmStatic
        @Serial
        private val serialVersionUID = -7120879291609543337L
    }
}
