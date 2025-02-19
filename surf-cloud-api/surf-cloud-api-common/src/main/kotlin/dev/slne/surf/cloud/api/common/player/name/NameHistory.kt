package dev.slne.surf.cloud.api.common.player.name

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
}

/**
 * Represents a player's name along with the timestamp of when it was set.
 *
 * @property timestamp The time in milliseconds when the name was set.
 * @property name The player's name at the given timestamp.
 */
data class NameEntry(val timestamp: Long, val name: String)