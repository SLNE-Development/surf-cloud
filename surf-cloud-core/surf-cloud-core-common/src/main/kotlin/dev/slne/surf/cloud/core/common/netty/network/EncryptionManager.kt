package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.coreCloudInstance
import io.netty.channel.Channel
import kotlinx.coroutines.delay
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

abstract class EncryptionManager {
    private val log = logger()

    protected val certificatesFolder: Path by lazy {
        coreCloudInstance.dataFolder.resolve("certificates").also { it.toFile().mkdirs() }
    }

    abstract fun setupEncryption(ch: Channel)

    abstract suspend fun init()

    protected suspend fun waitForFiles(vararg files: File) {
        if (files.all { it.exists() }) return
        val missingFiles = files.filter { !it.exists() }.toMutableList()

        while (missingFiles.isNotEmpty()) {
            log.atInfo()
                .log("Waiting for missing files: ${missingFiles.joinToString { it.path }}")

            delay(5.seconds)
            missingFiles.removeIf { it.exists() }
        }
    }
}