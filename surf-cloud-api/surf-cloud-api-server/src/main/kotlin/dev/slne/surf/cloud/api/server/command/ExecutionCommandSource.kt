package dev.slne.surf.cloud.api.server.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ResultConsumer
import com.mojang.brigadier.exceptions.CommandExceptionType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.kyori.adventure.text.Component

interface ExecutionCommandSource<T : ExecutionCommandSource<T>> {
    val callback: CommandResultCallback
    val silent: Boolean

    fun withCallback(callback: CommandResultCallback): T
    fun clearCallbacks(): T = withCallback(CommandResultCallback.EMPTY)

    fun handleError(exceptionType: CommandExceptionType, message: Component, success: Boolean)
    fun handleError(exception: CommandSyntaxException, success: Boolean) =
        handleError(exception.type, Component.text(exception.rawMessage.string), success)

    companion object {
        fun <T : ExecutionCommandSource<T>> resultConsumer() =
            ResultConsumer<T> { source, success, result ->
                source.source.callback.onResult(
                    success,
                    result
                )
            }
    }

}