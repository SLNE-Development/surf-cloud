package dev.slne.surf.cloud.api.server.command.util

import com.mojang.brigadier.context.StringRange

operator fun StringRange.contains(index: Int): Boolean {
    return index >= start && index <= end
}