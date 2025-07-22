package dev.slne.surf.cloud.api.common.util.nbt

import it.unimi.dsi.fastutil.io.FastBufferedInputStream
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
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

    fun writeCompressed(nbt: CompoundBinaryTag, path: Path) {
        Files.newOutputStream(path, *outputOptions).use { output ->
            FastBufferedOutputStream(output).use { fastOutput ->
                writeCompressed(nbt, fastOutput)
            }
        }
    }

    fun writeCompressed(nbt: CompoundBinaryTag, out: OutputStream) {
        createCompressorStream(out).use { output ->
            writeCompoundTag(output, nbt)
        }
    }

    fun readCompressed(path: Path) = Files.newInputStream(path).use { input ->
        FastBufferedInputStream(input).use { fastInput ->
            readCompressed(fastInput)
        }
    }


    fun readCompressed(stream: InputStream) =
        createDecompressorStream(stream).use { input ->
            readCompoundTag(input)
        }


    fun readCompoundTag(read: InputStream) = BinaryTagIO.unlimitedReader().read(read)

    fun writeCompoundTag(out: OutputStream, tag: CompoundBinaryTag) {
        BinaryTagIO.writer().write(tag, out)
    }

    private fun createDecompressorStream(stream: InputStream) =
        FastBufferedInputStream(GZIPInputStream(stream))

    private fun createCompressorStream(stream: OutputStream) =
        FastBufferedOutputStream(GZIPOutputStream(stream))
}