@file:OptIn(ExperimentalPathApi::class)

package dev.slne.surf.cloud.standalone.plugin.provider.source

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.standalone.plugin.entrypoint.EntrypointHandler
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

object DirectoryProviderSource : ProviderSource<Path, List<Path>> {
    private val log = logger()
    private val fileProviderSource = FileProviderSource { "Directory '$it'" }

    override fun prepareContext(context: Path): List<Path> {
        context.createDirectories()

        return Files.walk(context, 1, FileVisitOption.FOLLOW_LINKS).asSequence()
            .filter { it.isRegularFile() && !it.startsWith(".") }
            .mapNotNull {
                try {
                    fileProviderSource.prepareContext(it)
                } catch (_: IllegalArgumentException) {
                    null
                } catch (e: Exception) {
                    log.atSevere()
                        .withCause(e)
                        .log("Failed to prepare context for file '$it': ${e.message}")
                    null
                }
            }
            .toList()
    }

    override fun registerProviders(
        entrypointHandler: EntrypointHandler,
        context: List<Path>
    ) {
        for (path in context) {
            try {
                fileProviderSource.registerProviders(entrypointHandler, path)
            } catch (_: IllegalArgumentException) {
            } catch (e: Exception) {
                log.atSevere()
                    .withCause(e)
                    .log("Error loading Plugin $path': ${e.message}")
            }
        }
    }
}