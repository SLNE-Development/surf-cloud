package dev.slne.surf.cloud.api.server.plugin.loader.library

import dev.slne.surf.cloud.api.common.util.InternalApi
import java.nio.file.Path

@InternalApi
interface LibraryStore {
    fun addLibrary(library: Path)
}