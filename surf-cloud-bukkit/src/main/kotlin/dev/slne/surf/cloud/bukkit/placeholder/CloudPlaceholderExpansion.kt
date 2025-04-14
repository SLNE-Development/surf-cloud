package dev.slne.surf.cloud.bukkit.placeholder

import dev.slne.surf.cloud.api.common.util.objectListOf
import dev.slne.surf.cloud.bukkit.placeholder.afk.CloudAfkPlaceholder
import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiExpansion

object CloudPlaceholderExpansion : PapiExpansion(
    "cloud",
    objectListOf(CloudAfkPlaceholder)
)