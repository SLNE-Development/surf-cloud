@file:OptIn(InternalApi::class)

package dev.slne.surf.cloud.api.server.plugin.loader.library

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi

interface ClassPathLibrary {
    fun register(store: LibraryStore)
}