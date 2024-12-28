package dev.slne.surf.cloud.api.common.util.nbt

import it.unimi.dsi.fastutil.io.FastBufferedInputStream
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream
import net.querz.nbt.io.NBTInputStream
import net.querz.nbt.io.NBTOutputStream
import net.querz.nbt.tag.CompoundTag
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object FastNbtIo {

    private val outputOptions = arrayOf(
        StandardOpenOption.SYNC,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
    )

    fun writeCompressed(nbt: CompoundTag, path: Path) {
        Files.newOutputStream(path, *outputOptions).use { output ->
            FastBufferedOutputStream(output).use { fastOutput ->
                writeCompressed(nbt, fastOutput)
            }
        }
    }

    fun writeCompressed(nbt: CompoundTag, out: OutputStream) {
        createCompressorStream(out).use { output ->
            writeCompoundTag(output, nbt)
        }
    }

    fun readCompressed(path: Path, maxDepth: Int) = Files.newInputStream(path).use { input ->
        FastBufferedInputStream(input).use { fastInput ->
            readCompressed(fastInput, maxDepth)
        }
    }


    fun readCompressed(stream: InputStream, maxDepth: Int) =
        createDecompressorStream(stream).use { input ->
            readCompoundTag(input, maxDepth)
        }


    fun readCompoundTag(read: InputStream, maxDepth: Int) =
        NBTInputStream(read).use { input ->
            input.readTag(maxDepth).tag as? CompoundTag ?: error("Root tag is not a CompoundTag")
        }

    fun writeCompoundTag(out: OutputStream, tag: CompoundTag) {
        NBTOutputStream(out).use { output ->
            output.writeTag(tag, Int.MAX_VALUE)
        }
    }

    private fun createDecompressorStream(stream: InputStream) =
        FastBufferedInputStream(GZIPInputStream(stream))

    private fun createCompressorStream(stream: OutputStream) =
        FastBufferedOutputStream(GZIPOutputStream(stream))
}