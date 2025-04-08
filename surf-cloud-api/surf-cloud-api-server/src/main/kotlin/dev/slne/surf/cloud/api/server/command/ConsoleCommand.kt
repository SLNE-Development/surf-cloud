package dev.slne.surf.cloud.api.server.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder

interface ConsoleCommand

inline fun <S> ConsoleCommand.literal(
    literal: String,
    block: LiteralArgumentBuilder<S>.() -> Unit = {}
): LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal<S>(literal).apply(block)