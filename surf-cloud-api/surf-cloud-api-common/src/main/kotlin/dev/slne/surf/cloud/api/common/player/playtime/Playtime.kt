package dev.slne.surf.cloud.api.common.player.playtime

import dev.slne.surf.cloud.api.common.server.CloudServer
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.time.ZonedDateTime
import kotlin.time.Duration

/**
 * Provides a comprehensive analytical view of playtime data, allowing various queries and analyses
 * based on servers, categories, and timeframes.
 *
 * This interface is immutable and provides methods to perform detailed aggregations and analytics
 * without altering the underlying data.
 */
interface Playtime {

    /**
     * Returns the total playtime across all servers and categories.
     *
     * @param since Optional start time. If provided, only playtime after this timestamp is considered.
     * @return The summed total playtime as a [Duration].
     */
    fun sumPlaytimes(since: ZonedDateTime? = null): Duration

    /**
     * Returns the total playtime for a specific category.
     *
     * @param category The category to filter by.
     * @param since Optional start time to filter playtime.
     * @return The summed total playtime for the category as a [Duration].
     */
    fun sumByCategory(category: String, since: ZonedDateTime? = null): Duration

    /**
     * Returns the total playtime on a specific server identified by its name.
     *
     * @param server The server name to filter by.
     * @param since Optional start time to filter playtime.
     * @return The summed total playtime for the specified server as a [Duration].
     */
    fun sumByServer(server: String, since: ZonedDateTime? = null): Duration

    /**
     * Returns the total playtime on a specific [CloudServer].
     *
     * @param server The [CloudServer] to filter by.
     * @param since Optional start time to filter playtime.
     * @return The summed total playtime for the specified server as a [Duration].
     */
    fun sumByServer(server: CloudServer, since: ZonedDateTime? = null): Duration {
        return sumByServer(server.name, since)
    }

    /**
     * Returns a set of all distinct categories present in the playtime data.
     *
     * @return An [ObjectSet] of unique category names.
     */
    fun getCategories(): ObjectSet<String>

    /**
     * Returns a set of all distinct server names present in the playtime data.
     *
     * @return An [ObjectSet] of unique server names.
     */
    fun getServers(): ObjectSet<String>

    /**
     * Returns the total playtime for a specific server and optionally a specific category.
     *
     * @param server The server name.
     * @param category Optional category to further filter results.
     * @param since Optional start time to filter playtime.
     * @return The summed total playtime matching the specified filters as a [Duration].
     */
    fun playtimeFor(
        server: String,
        category: String? = null,
        since: ZonedDateTime? = null
    ): Duration

    /**
     * Returns a mapping of servers to their respective total playtime durations.
     *
     * @param since Optional start time to filter playtime.
     * @return An [Object2ObjectMap] where keys are server names and values are durations.
     */
    fun playtimesPerServer(since: ZonedDateTime? = null): Object2ObjectMap<String, Duration>

    /**
     * Returns a mapping of categories to their respective total playtime durations.
     *
     * @param since Optional start time to filter playtime.
     * @return An [Object2ObjectMap] where keys are category names and values are durations.
     */
    fun playtimesPerCategory(since: ZonedDateTime? = null): Object2ObjectMap<String, Duration>

    /**
     * Returns the average playtime per server, optionally filtered by category and start time.
     *
     * @param category Optional category to filter by.
     * @param since Optional start time to filter playtime.
     * @return The average playtime across servers as a [Duration].
     */
    fun averagePlaytimePerServer(category: String? = null, since: ZonedDateTime? = null): Duration

    /**
     * Generates a timeline mapping timestamps to accumulated playtime durations, grouped by specified intervals.
     * Each interval represents a bucket of time starting at the beginning of the interval and includes the total
     * playtime that occurred within that interval. This is particularly useful for analyzing player activity trends,
     * identifying peak playing times, or generating visualizations such as heatmaps and activity charts.
     *
     * For example, if you choose an hourly interval, each timestamp in the resulting map will correspond
     * precisely to the start of that hour, with its associated duration representing the sum of all playtime
     * recorded between that hour and the start of the next hour.
     *
     * ### Example use case:
     *
     * Suppose you want to analyze player activity throughout the last day to determine peak gaming hours on
     * the server named "PvP-Arena" within the "competitive" category. You could use:
     *
     * ```kotlin
     * val hourlyTimeline = playtime.timeline(
     *     interval = 1.hours,
     *     category = "competitive",
     *     server = "PvP-Arena"
     * )
     *
     * hourlyTimeline.forEach { (hour, duration) ->
     *     println("Playtime from $hour to ${hour.plusHours(1)}: $duration")
     * }
     * ```
     *
     * The resulting output might look like:
     *
     * ```
     * Playtime from 2025-04-08T14:00Z to 2025-04-08T15:00Z: 30m
     * Playtime from 2025-04-08T15:00Z to 2025-04-08T16:00Z: 45m
     * Playtime from 2025-04-08T16:00Z to 2025-04-08T17:00Z: 1h
     * ...
     * ```
     *
     * This clearly illustrates player activity peaks, enabling targeted actions such as scheduling server events,
     * balancing loads, or informing community engagement strategies.
     *
     * @param interval The duration of each interval (e.g., hourly, daily, weekly).
     * @param category Optional category filter. If specified, only playtime matching this category is considered.
     * @param server Optional server filter. If specified, only playtime on this particular server is included.
     * @return An [Object2ObjectMap] mapping interval-start timestamps to the total accumulated playtime durations
     *         within each interval.
     */
    fun timeline(
        interval: Duration,
        category: String? = null,
        server: String? = null
    ): Object2ObjectMap<ZonedDateTime, Duration>

    /**
     * Retrieves a ranked list of servers sorted by total playtime in descending order.
     *
     * @param limit Maximum number of results to return.
     * @param since Optional start time to filter playtime.
     * @return An [ObjectList] of pairs, each containing a server name and its corresponding playtime duration.
     */
    fun topServers(limit: Int = 5, since: ZonedDateTime? = null): ObjectList<Pair<String, Duration>>

    /**
     * Retrieves a ranked list of categories sorted by total playtime in descending order.
     *
     * @param limit Maximum number of results to return.
     * @param since Optional start time to filter playtime.
     * @return An [ObjectList] of pairs, each containing a category name and its corresponding playtime duration.
     */
    fun topCategories(limit: Int = 5, since: ZonedDateTime? = null): ObjectList<Pair<String, Duration>>
}