package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.util.requiredService
import io.netty.channel.Channel
import kotlinx.coroutines.delay
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.time.Duration.Companion.seconds

abstract class EncryptionManager {
    companion object {
        val instance = requiredService<EncryptionManager>()
    }

    protected val certificatesFolder: Path by lazy {
        (coreCloudInstance.dataFolder / "certificates").createDirectories()
    }

    abstract fun setupEncryption(ch: Channel)

    abstract suspend fun init()

    protected suspend fun waitForFiles(vararg files: File) {
        if (files.all { it.exists() }) return
        val missingFiles = files.filter { !it.exists() }.toMutableList()

        while (missingFiles.isNotEmpty()) {
//            log.atInfo()
//                .log("Waiting for missing files: ${missingFiles.joinToString { it.path }}")

            println("[INFO] ${this::class.simpleName}: Waiting for missing files: ${missingFiles.joinToString { it.path }}")

            delay(5.seconds)
            missingFiles.removeIf { it.exists() }
        }
    }
}