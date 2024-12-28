package dev.slne.surf.cloud.standalone.persistent

import dev.slne.surf.cloud.standalone.standaloneCloudInstance
import java.io.File

object DataStorage {
    const val MAX_STACK_DEPTH = 512

    val storageDir: File =
        standaloneCloudInstance.dataFolder.resolve("storage").toFile().also { it.mkdirs() }
}