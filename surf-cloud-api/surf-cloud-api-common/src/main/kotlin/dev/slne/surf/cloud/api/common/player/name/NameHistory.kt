package dev.slne.surf.cloud.api.common.player.name

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUtf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readVarLong
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUtf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeVarLong
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.objects.ObjectList

/**
 * Represents an immutable history of player names with their associated timestamps.
 */
interface NameHistory {
    /**
     * The total number of name changes made.
     */
    val nameChanges: Int

    /**
     * Returns a list of all name entries, including their timestamps.
     *
     * @return A list of [NameEntry] objects representing the full name history.
     */
    fun names(): ObjectList<NameEntry>

    /**
     * Returns the most recently known name.
     *
     * @return The latest [NameEntry], or `null` if no names are recorded.
     */
    fun lastKnownName(): NameEntry?

    /**
     * Retrieves the name that was used at a specific point in time.
     *
     * @param time The timestamp in milliseconds for which to retrieve the name.
     * @return The [NameEntry] valid at the specified time, or `null` if no name was set at that time.
     */
    fun getNameAtTime(time: Long): NameEntry?

    /**
     * Checks if a specific name was ever used.
     *
     * @param name The name to check.
     * @return `true` if the name was used at any point, `false` otherwise.
     */
    fun wasNameUsed(name: String): Boolean

    /**
     * Returns the first name that was used.
     *
     * @return The first [NameEntry], or `null` if no names are recorded.
     */
    fun firstUsedName(): NameEntry?

    /**
     * Calculates the time elapsed since the last name change.
     *
     * @return The time in milliseconds since the last name change, or `0` if no names are recorded.
     */
    fun timeSinceLastChange(): Long

    /**
     * Writes the name history to a [ByteBuf].
     *
     * @param buf The buffer to write the history to.
     */
    fun writeToByteBuf(buf: ByteBuf)

    companion object {
        /**
         * Reads a [NameHistory] from a [ByteBuf].
         *
         * @param buf The buffer to read the history from.
         * @return The [NameHistory] read from the buffer.
         */
        fun readFromByteBuf(buf: ByteBuf): NameHistory {
            return NameHistoryFactory.instance.createFromByteBuf(buf)
        }
    }
}

/**
 * Represents a player's name along with the timestamp of when it was set.
 *
 * @property timestamp The time in milliseconds when the name was set.
 * @property name The player's name at the given timestamp.
 */
data class NameEntry(val timestamp: Long, val name: String) {
    fun writeToByteBuf(buf: ByteBuf) {
        buf.writeVarLong(timestamp)
        buf.writeUtf(name)
    }

    companion object {
        fun readFromByteBuf(buf: ByteBuf): NameEntry {
            val timestamp = buf.readVarLong()
            val name = buf.readUtf()
            return NameEntry(timestamp, name)
        }
    }
}

@InternalApi
interface NameHistoryFactory {
    fun create(entries: ObjectList<NameEntry>): NameHistory
    fun createFromByteBuf(buf: ByteBuf): NameHistory
    fun empty(): NameHistory

    companion object {
        val instance = requiredService<NameHistoryFactory>()
    }
}