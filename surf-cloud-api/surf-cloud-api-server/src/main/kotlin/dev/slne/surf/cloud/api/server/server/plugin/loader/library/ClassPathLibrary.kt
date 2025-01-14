@file:OptIn(InternalPluginApi::class)

package dev.slne.surf.cloud.api.server.server.plugin.loader.library

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi

interface ClassPathLibrary {
    fun register(store: LibraryStore)
}