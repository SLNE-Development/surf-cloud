package dev.slne.surf.cloud.api.server.command

import com.mojang.brigadier.exceptions.CommandExceptionType
import dev.slne.surf.surfapi.core.api.messages.Colors
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer

class CommandSource(
    silent: Boolean = false,
    callback: CommandResultCallback = CommandResultCallback.EMPTY
) : ExecutionCommandSource<CommandSource> {

    override var callback = callback
        private set
    override var silent = silent
        private set

    fun sendFailure(message: Component) {
        if (!silent) {
            sendMessage(message.colorIfAbsent(Colors.ERROR))
        }
    }

    fun sendSuccess(message: Component) {
        if (!silent) {
            sendMessage(message.colorIfAbsent(Colors.SUCCESS))
        }
    }

    fun sendInfo(info: Component) {
        if (!silent) {
            sendMessage(info.colorIfAbsent(Colors.INFO))
        }
    }

    fun sendMessage(message: Component) {
        if (!silent) {
            println(ANSIComponentSerializer.ansi().serialize(message))
        }
    }

    fun sendSuccess(message: String) = sendSuccess(Component.text(message))
    fun sendFailure(message: String) = sendFailure(Component.text(message))
    fun sendInfo(message: String) = sendInfo(Component.text(message))

    override fun withCallback(callback: CommandResultCallback): CommandSource {
        if (this.callback == callback) return this
        return copy(callback = callback)
    }

    override fun handleError(
        exceptionType: CommandExceptionType,
        message: Component,
        success: Boolean
    ) {
        if (!success) {
            sendFailure(message)
        }
    }

    private fun copy(
        silent: Boolean = this.silent,
        callback: CommandResultCallback = this.callback
    ) = CommandSource(silent, callback)
}