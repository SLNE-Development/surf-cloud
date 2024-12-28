package dev.slne.surf.cloud.standalone.persistent

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.nbt.FastNbtIo
import dev.slne.surf.cloud.api.common.util.safeReplaceFile
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import net.querz.nbt.tag.CompoundTag
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime


object PlayerDataStorage {
    private val log = logger()
    private val formatter = FileNameDateFormatter.create()

    val playerDir = DataStorage.storageDir.resolve("playerdata").also { it.mkdirs() }

    fun save(player: StandaloneCloudPlayerImpl) = runCatching {
        val tag = CompoundTag()
        player.savePlayerData(tag)
        val tempDataFile = Files.createTempFile(playerDir.toPath(), player.uuid.toString() + "-", ".dat")

        FastNbtIo.writeCompressed(tag, tempDataFile)

        val playerFile = playerDir.resolve("${player.uuid}.dat").toPath()
        val oldPlayerFile = playerDir.resolve("${player.uuid}.dat_old").toPath()

        safeReplaceFile(playerFile, tempDataFile, oldPlayerFile)
    }.onFailure {
        log.atWarning().withCause(it).log("Failed to save player data for player ${player.uuid}")
    }

    fun load(player: StandaloneCloudPlayerImpl): CompoundTag? {
        val tag = loadFile(player.uuid.toString()) ?: return null

        player.readPlayerData(tag)

        return tag
    }

    private fun loadFile(uuid: String): CompoundTag? {
        val loaded = loadFile(uuid, "dat")

        if (loaded == null) {
            backup(uuid, "dat")
        }

        return loaded ?: loadFile(uuid, "dat_old")
    }

    private fun loadFile(fileName: String, extension: String): CompoundTag? {
        val playerFile = playerDir.resolve("$fileName.$extension")
        if (!playerFile.exists() || !playerFile.isFile) return null

        return FastNbtIo.readCompressed(playerFile.toPath(), DataStorage.MAX_STACK_DEPTH)
    }

    private fun backup(fileName: String, extension: String) {
        val playerFile = playerDir.resolve("$fileName.$extension").toPath()
        val backupFile = playerDir.resolve(
            "${fileName}_corrupted_${
                LocalDateTime.now().format(formatter)
            }.$extension"
        ).toPath()

        if (Files.isRegularFile(playerFile)) {
            runCatching {
                Files.copy(
                    playerFile,
                    backupFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
                )
            }.onFailure {
                log.atWarning().withCause(it).log("Failed to backup corrupted player file")
            }
        }
    }
}