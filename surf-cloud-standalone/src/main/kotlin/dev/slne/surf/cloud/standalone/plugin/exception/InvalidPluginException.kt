package dev.slne.surf.cloud.standalone.plugin.exception

import java.io.Serial

class InvalidPluginException : Exception {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 6977487608780813035L
    }

    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)

}