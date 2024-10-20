package dev.slne.surf.cloud.api.util

import org.springframework.boot.ansi.AnsiOutput

fun buildAnsiString(vararg elements: Any): String = AnsiOutput.toString(*elements)