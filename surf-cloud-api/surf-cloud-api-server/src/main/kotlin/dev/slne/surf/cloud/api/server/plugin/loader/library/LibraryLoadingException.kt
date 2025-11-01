package dev.slne.surf.cloud.api.server.plugin.loader.library

import java.io.Serial

class LibraryLoadingException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}