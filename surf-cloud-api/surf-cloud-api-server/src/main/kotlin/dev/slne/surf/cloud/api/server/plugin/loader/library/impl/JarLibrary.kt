@file:OptIn(InternalApi::class)

package dev.slne.surf.cloud.api.server.plugin.loader.library.impl

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.server.plugin.loader.library.ClassPathLibrary
import dev.slne.surf.cloud.api.server.plugin.loader.library.LibraryLoadingException
import dev.slne.surf.cloud.api.server.plugin.loader.library.LibraryStore
import java.nio.file.Path
import kotlin.io.path.notExists

class JarLibrary(
    val path: Path
) : ClassPathLibrary {
    override fun register(store: LibraryStore) {
        if (path.notExists()) {
            throw LibraryLoadingException("Could not find library at $path")
        }

        store.addLibrary(path)
    }
}