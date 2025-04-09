package dev.slne.surf.cloud.core.common.player.playtime

import dev.slne.surf.cloud.api.common.player.playtime.Playtime
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.toObjectList
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlaytimeImpl(private val entries: ObjectList<PlaytimeEntry>) : Playtime {
    override fun sumPlaytimes(since: ZonedDateTime?): Duration {
        if (since == null) {
            return entries.sumOf { it.durationSeconds }.seconds
        }

        return entries
            .filter { it.createdAt.isAfter(since) }
            .sumOf { it.durationSeconds }
            .seconds
    }

    override fun sumByCategory(
        category: String,
        since: ZonedDateTime?
    ): Duration {
        if (since == null) {
            return entries
                .filter { it.category.equals(category, true) }
                .sumOf { it.durationSeconds }
                .seconds
        }

        return entries
            .filter { it.category.equals(category, true) && it.createdAt.isAfter(since) }
            .sumOf { it.durationSeconds }
            .seconds
    }

    override fun sumByServer(
        server: String,
        since: ZonedDateTime?
    ): Duration {
        if (since == null) {
            return entries
                .filter { it.server.equals(server, true) }
                .sumOf { it.durationSeconds }
                .seconds
        }

        return entries
            .filter { it.server.equals(server, true) && it.createdAt.isAfter(since) }
            .sumOf { it.durationSeconds }
            .seconds
    }

    override fun getCategories(): ObjectSet<String> {
        return entries.mapTo(mutableObjectSetOf()) { it.category }
    }

    override fun getServers(): ObjectSet<String> {
        return entries.mapTo(mutableObjectSetOf()) { it.server }
    }

    override fun playtimeFor(
        server: String,
        category: String?,
        since: ZonedDateTime?
    ): Duration {
        if (since == null) {
            return entries
                .filter {
                    it.server.equals(server, true) && (category == null || it.category.equals(
                        category,
                        true
                    ))
                }
                .sumOf { it.durationSeconds }
                .seconds
        }

        return entries
            .filter {
                it.server.equals(server, true) && (category == null || it.category.equals(
                    category,
                    true
                )) && it.createdAt.isAfter(since)
            }
            .sumOf { it.durationSeconds }
            .seconds
    }

    override fun playtimesPerServer(since: ZonedDateTime?): Object2ObjectMap<String, Duration> {
        if (since == null) {
            return entries.groupBy { it.server }
                .mapValuesTo(mutableObject2ObjectMapOf()) { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
        }

        return entries
            .filter { it.createdAt.isAfter(since) }
            .groupBy { it.server }
            .mapValuesTo(mutableObject2ObjectMapOf()) { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
    }

    override fun playtimesPerCategory(since: ZonedDateTime?): Object2ObjectMap<String, Duration> {
        if (since == null) {
            return entries.groupBy { it.category }
                .mapValuesTo(mutableObject2ObjectMapOf()) { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
        }

        return entries
            .filter { it.createdAt.isAfter(since) }
            .groupBy { it.category }
            .mapValuesTo(mutableObject2ObjectMapOf()) { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
    }

    override fun averagePlaytimePerServer(
        category: String?,
        since: ZonedDateTime?
    ): Duration {
        if (since == null) {
            val values = entries
                .filter { category == null || it.category.equals(category, true) }
                .groupBy { it.server }
                .mapValuesTo(mutableObject2ObjectMapOf()) { (_, entry) -> entry.sumOf { it.durationSeconds } }
                .values

            if (values.isEmpty()) {
                return Duration.ZERO
            }

            return values
                .average()
                .seconds
        }

        val values = entries
            .filter {
                (category == null || it.category.equals(
                    category,
                    true
                )) && it.createdAt.isAfter(since)
            }
            .groupBy { it.server }
            .mapValuesTo(mutableObject2ObjectMapOf()) { (_, entry) -> entry.sumOf { it.durationSeconds } }
            .values

        if (values.isEmpty()) {
            return Duration.ZERO
        }

        return values
            .average()
            .seconds
    }

    override fun timeline(
        interval: Duration,
        category: String?,
        server: String?
    ): Object2ObjectMap<ZonedDateTime, Duration> {
        val map = mutableObject2ObjectMapOf<ZonedDateTime, Duration>()

        for (entry in entries) {
            if (category != null && !entry.category.equals(category, true)) continue
            if (server != null && !entry.server.equals(server, true)) continue

            val bucket = floorToInterval(entry.createdAt, interval)
            map.merge(bucket, entry.durationSeconds.seconds) { a, b -> a + b }
        }

        return map
    }

    override fun topServers(
        limit: Int,
        since: ZonedDateTime?
    ): ObjectList<Pair<String, Duration>> {
        if (since == null) {
            return entries
                .groupBy { it.server }
                .mapValues { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
                .toList()
                .sortedByDescending { it.second }
                .take(limit)
                .toObjectList()
        }

        return entries
            .filter { it.createdAt.isAfter(since) }
            .groupBy { it.server }
            .mapValues { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .toObjectList()
    }

    override fun topCategories(
        limit: Int,
        since: ZonedDateTime?
    ): ObjectList<Pair<String, Duration>> {
        if (since == null) {
            return entries
                .groupBy { it.category }
                .mapValues { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
                .toList()
                .sortedByDescending { it.second }
                .take(limit)
                .toObjectList()
        }

        return entries
            .filter { it.createdAt.isAfter(since) }
            .groupBy { it.category }
            .mapValues { (_, entry) -> entry.sumOf { it.durationSeconds }.seconds }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .toObjectList()
    }
}

private fun floorToInterval(time: ZonedDateTime, interval: Duration): ZonedDateTime {
    val seconds = interval.inWholeSeconds
    val epochSeconds = time.toEpochSecond()
    val floored = (epochSeconds / seconds) * seconds
    return ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(floored), time.zone)
}


data class PlaytimeEntry(
    val category: String,
    val server: String,
    val durationSeconds: Long,
    val createdAt: ZonedDateTime
)
