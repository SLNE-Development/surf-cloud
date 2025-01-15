package dev.slne.surf.cloud.standalone.plugin.loader.library

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.server.plugin.loader.library.LibraryStore
import java.nio.file.Path

class StandaloneLibraryStore: LibraryStore {
    val paths = mutableObjectListOf<Path>()

    override fun addLibrary(library: Path) {
        paths.add(library)
    }
}