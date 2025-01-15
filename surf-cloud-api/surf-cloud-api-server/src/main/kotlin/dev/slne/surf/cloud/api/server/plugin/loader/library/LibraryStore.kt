package dev.slne.surf.cloud.api.server.plugin.loader.library

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi
import java.nio.file.Path

@InternalPluginApi
interface LibraryStore {
    fun addLibrary(library: Path)
}