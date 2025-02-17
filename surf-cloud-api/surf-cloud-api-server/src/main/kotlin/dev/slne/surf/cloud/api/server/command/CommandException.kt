package dev.slne.surf.cloud.api.server.command

import java.io.Serial

/**
 * Thrown when an unhandled exception occurs during the execution of a Command.
 *
 * This should generally not be used directly
 */
class CommandException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3968454023580552854L
    }
}