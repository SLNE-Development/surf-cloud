package dev.slne.surf.cloud.api.common.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import java.lang.reflect.Method
import java.nio.file.Path
import java.util.*
import java.util.function.ToIntFunction
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.moveTo
import kotlin.reflect.jvm.kotlinFunction

const val LINEAR_LOOKUP_THRESHOLD = 8

fun <T> createIndexLookup(values: List<T>): ToIntFunction<T> {
    val size = values.size
    return if (size < LINEAR_LOOKUP_THRESHOLD) {
        ToIntFunction { values.indexOf(it) }
    } else {
        mutableObject2IntMapOf<T>().apply {
            for (index in 0 until size) {
                put(values[index], index)
            }
            defaultReturnValue(-1)
        }
    }
}

fun IntArray.toUuid(): UUID {
    return UUID(
        this[0].toLong() shl 32 or (this[1].toLong() and 0xFFFFFFFF),
        this[2].toLong() shl 32 or (this[3].toLong() and 0xFFFFFFFF)
    )
}

fun UUID.toIntArray() = leastMostToIntArray(mostSignificantBits, leastSignificantBits)


private fun leastMostToIntArray(uuidMost: Long, uuidLeast: Long): IntArray {
    return intArrayOf(
        (uuidMost shr 32).toInt(),
        uuidMost.toInt(),
        (uuidLeast shr 32).toInt(),
        uuidLeast.toInt()
    )
}

fun Byte.toUnsignedInt(): Int = java.lang.Byte.toUnsignedInt(this)

fun safeReplaceFile(current: Path, newPath: Path, backup: Path) {
    safeReplaceOrMoveFile(current, newPath, backup, false)
}

private val fileOperationLogger = ComponentLogger.logger("FileOperations")

fun safeReplaceOrMoveFile(current: Path, newPath: Path, backup: Path, noRestoreOnFail: Boolean): Boolean {
    if (current.exists() &&
        !runWithRetries(
            10,
            "create backup $backup",
            createDeleter(backup),
            createRenamer(current, backup),
            createFileCreatedCheck(backup)
        )
    ) {
        return false
    } else if (!runWithRetries(
            10,
            "remove old $current",
            createDeleter(current),
            createFileDeletedCheck(current)
        )
    ) {
        return false
    } else if (!runWithRetries(
            10,
            "replace $current with $newPath",
            createRenamer(newPath, current),
            createFileCreatedCheck(current)
        ) && !noRestoreOnFail
    ) {
        runWithRetries(
            10,
            "restore $current from $backup",
            createRenamer(backup, current),
            createFileCreatedCheck(current)
        )
        return false
    } else {
        return true
    }
}

private fun runWithRetries(retries: Int, taskName: String, vararg tasks: () -> Boolean): Boolean {
    repeat(retries) { attempt ->
        if (executeInSequence(*tasks)) {
            return true
        }
        fileOperationLogger.error("Failed to {}, retrying {}/{}", taskName, attempt + 1, retries)
    }
    fileOperationLogger.error("Failed to {}, aborting, progress might be lost", taskName)
    return false
}

private fun executeInSequence(vararg tasks: () -> Boolean): Boolean {
    for (task in tasks) {
        if (!task()) {
            fileOperationLogger.warn("Failed to execute task")
            return false
        }
    }
    return true
}

private fun createDeleter(path: Path): () -> Boolean = {
    try {
        path.deleteIfExists()
        true
    } catch (e: Exception) {
        fileOperationLogger.warn("Failed to delete $path", e)
        false
    }
}

private fun createRenamer(src: Path, dest: Path): () -> Boolean = {
    try {
        src.moveTo(dest)
        true
    } catch (e: Exception) {
        fileOperationLogger.error("Failed to rename $src to $dest", e)
        false
    }
}

private fun createFileCreatedCheck(path: Path): () -> Boolean = {
    path.isRegularFile()
}

private fun createFileDeletedCheck(path: Path): () -> Boolean = {
    !path.exists()
}

fun Method.isSuspending() = kotlinFunction?.isSuspend == true

suspend inline fun <T, R> Iterable<T>.mapAsync(crossinline transform: suspend (T) -> R): List<Deferred<R>> =
    coroutineScope {
        map { async { transform(it) } }
    }