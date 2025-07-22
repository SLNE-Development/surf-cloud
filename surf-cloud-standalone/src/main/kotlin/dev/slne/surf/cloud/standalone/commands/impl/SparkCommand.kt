package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.ConsoleCommand
import dev.slne.surf.cloud.api.server.command.literal
import dev.slne.surf.cloud.api.server.command.then
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.commands.ArgumentSuggestion
import dev.slne.surf.cloud.standalone.spark.SparkHook
import me.lucko.spark.standalone.StandaloneCommandSender

object SparkCommand : ConsoleCommand {
    private val sparkHook by lazy { bean<SparkHook>() }

    private val suggestionProvider = SuggestionProvider<CommandSource> { context, builder ->
        val input = builder.remainingLowerCase
        val resultBuilder = if (input.contains(" ")) builder.createOffset(
            builder.start + input.substringBeforeLast(" ").length + 1
        ) else builder
        val suggestions = sparkHook.spark.suggest(
            input.split(" ").toTypedArray(),
            StandaloneCommandSender.SYSTEM_OUT
        )
        ArgumentSuggestion.suggestStrings(suggestions, resultBuilder)
    }

    fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(literal("spark") {
            then("input", StringArgumentType.greedyString()) {
                executes { context ->
                    val input = StringArgumentType.getString(context, "input")
                    val sparkHook = bean<SparkHook>()
                    sparkHook.spark.execute(
                        input.split(" ").toTypedArray(),
                        StandaloneCommandSender.SYSTEM_OUT
                    )
                    Command.SINGLE_SUCCESS
                }


                suggests(suggestionProvider)
            }
        })
    }
}