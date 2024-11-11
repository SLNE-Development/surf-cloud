package dev.slne.surf.cloud.api.common.netty.exception

import java.io.Serial

abstract class SurfNettyListenerException : SurfNettyException {
    constructor() : super()
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)


    companion object {
        @JvmStatic
        @Serial
        private val serialVersionUID = 7748641971923464222L
    }
}
