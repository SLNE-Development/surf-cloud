package dev.slne.surf.cloud.api.server.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder

interface ConsoleCommand

inline fun <S> ConsoleCommand.literal(
    literal: String,
    block: LiteralArgumentBuilder<S>.() -> Unit = {}
): LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal<S>(literal).apply(block)

inline fun <S, T> ConsoleCommand.argument(
    name: String,
    type: ArgumentType<T>,
    block: RequiredArgumentBuilder<S, T>.() -> Unit = {}
): RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument<S, T>(name, type).apply(block)

inline fun <S, T : ArgumentBuilder<S, T>, AT> ArgumentBuilder<S, T>.then(
    name: String,
    type: ArgumentType<AT>,
    block: RequiredArgumentBuilder<S, AT>.() -> Unit = {}
): T = then(RequiredArgumentBuilder.argument<S, AT>(name, type).apply(block))

inline fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.then(
    literal: String,
    block: LiteralArgumentBuilder<S>.() -> Unit = {}
): T = then(LiteralArgumentBuilder.literal<S>(literal).apply(block))