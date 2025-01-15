package dev.slne.surf.cloud.api.server.plugin.loader.library

class LibraryLoadingException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}