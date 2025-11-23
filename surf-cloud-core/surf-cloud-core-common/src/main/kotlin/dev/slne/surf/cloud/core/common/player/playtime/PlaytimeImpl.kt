package dev.slne.surf.cloud.core.common.player.playtime

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.*
import dev.slne.surf.cloud.api.common.player.playtime.Playtime
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.surfapi.core.api.util.objectListOf
import dev.slne.surf.surfapi.core.api.util.toObjectList
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PlaytimeImpl(private val entries: ObjectList<PlaytimeEntry>) : Playtime {
    override fun sumPlaytimes(since: ZonedDateTime?): Duration = entries
        .filter { since == null || it.createdAt.isAfter(since) }
        .sumOf { it.durationSeconds }
        .seconds

    override fun sumByCategory(
        category: String,
        since: ZonedDateTime?
    ): Duration = entries
        .filter {
            it.category.equals(category, ignoreCase = true)
                    && (since == null || it.createdAt.isAfter(since))
        }
        .sumOf { it.durationSeconds }
        .seconds

    override fun sumByServer(
        server: String,
        since: ZonedDateTime?
    ): Duration = entries
        .filter {
            it.server.equals(server, ignoreCase = true)
                    && (since == null || it.createdAt.isAfter(since))
        }
        .sumOf { it.durationSeconds }
        .seconds

    override fun getCategories(): ObjectSet<String> =
        entries.mapTo(mutableObjectSetOf()) { it.category }

    override fun getServers(): ObjectSet<String> =
        entries.mapTo(mutableObjectSetOf()) { it.server }

    override fun playtimeFor(
        server: String,
        category: String?,
        since: ZonedDateTime?
    ): Duration = entries
        .filter {
            it.server.equals(server, ignoreCase = true)
                    && (category == null || it.category.equals(category, ignoreCase = true))
                    && (since == null || it.createdAt.isAfter(since))
        }
        .sumOf { it.durationSeconds }
        .seconds

    override fun playtimesPerServer(
        since: ZonedDateTime?,
        sortByPlaytime: Boolean
    ): Object2ObjectMap<String, Duration> {
        val result = entries
            .filter { since == null || it.createdAt.isAfter(since) }
            .groupBy { it.server }
            .mapValuesTo(mutableObject2ObjectMapOf()) { (_, group) ->
                group.sumOf { it.durationSeconds }.seconds
            }

        return if (sortByPlaytime) {
            result.entries
                .sortedByDescending { it.value }
                .associateTo(mutableObject2ObjectMapOf()) { it.toPair() }
        } else result
    }


    override fun playtimesPerCategory(
        since: ZonedDateTime?,
        sortByPlaytime: Boolean
    ): Object2ObjectMap<String, Duration> {
        val result = entries
            .filter { since == null || it.createdAt.isAfter(since) }
            .groupBy { it.category }
            .mapValuesTo(mutableObject2ObjectMapOf()) { (_, group) ->
                group.sumOf { it.durationSeconds }.seconds
            }

        return if (sortByPlaytime) {
            result.entries
                .sortedByDescending { it.value }
                .associateTo(mutableObject2ObjectMapOf()) { it.toPair() }
        } else result
    }


    override fun playtimePerCategoryPerServer(
        since: ZonedDateTime?,
        sortByPlaytime: Boolean
    ): Object2ObjectMap<String, Object2ObjectMap<String, Duration>> {
        val filtered = entries
            .filter { since == null || it.createdAt.isAfter(since) }

        val grouped = filtered.groupBy { it.category }
            .mapValues { (_, entriesByCategory) ->
                val serverDurations = entriesByCategory
                    .groupingBy { it.server }
                    .fold(0L) { acc, e -> acc + e.durationSeconds }
                    .mapValues { it.value.seconds }

                val sorted = if (sortByPlaytime)
                    serverDurations.entries.sortedByDescending { it.value }
                else serverDurations.entries

                mutableObject2ObjectMapOf<String, Duration>().apply {
                    sorted.forEach { (k, v) -> this[k] = v }
                }
            }

        val categorySums = grouped.mapValues { (_, serverDurations) ->
            serverDurations.values.sumOf { it.inWholeSeconds }
        }

        val finalResult = if (sortByPlaytime)
            grouped.entries.sortedByDescending { categorySums[it.key] }
        else grouped.entries

        return mutableObject2ObjectMapOf<String, Object2ObjectMap<String, Duration>>().apply {
            finalResult.forEach { (k, v) -> this[k] = v }
        }
    }





    override fun averagePlaytimePerServer(
        category: String?,
        since: ZonedDateTime?
    ): Duration {
        val sumsPerServer = entries
            .filter {
                (category == null || it.category.equals(category, ignoreCase = true)) &&
                        (since == null || it.createdAt.isAfter(since))
            }
            .groupBy { it.server }
            .mapValues { (_, group) -> group.sumOf { it.durationSeconds } }
            .values

        return if (sumsPerServer.isEmpty()) Duration.ZERO
        else sumsPerServer.average().seconds
    }

    override fun timeline(
        interval: Duration,
        category: String?,
        server: String?
    ): Object2ObjectMap<ZonedDateTime, Duration> {
        val map = mutableObject2ObjectMapOf<ZonedDateTime, Duration>()

        for (entry in entries) {
            if (category != null && !entry.category.equals(category, ignoreCase = true)) continue
            if (server != null && !entry.server.equals(server, ignoreCase = true)) continue

            val bucket = floorToInterval(entry.createdAt, interval)
            map.merge(bucket, entry.durationSeconds.seconds) { a, b -> a + b }
        }

        return map
    }

    override fun topServers(
        limit: Int,
        since: ZonedDateTime?
    ): ObjectList<Pair<String, Duration>> = entries
        .filter { since == null || it.createdAt.isAfter(since) }
        .groupBy { it.server }
        .mapValues { (_, group) -> group.sumOf { it.durationSeconds }.seconds }
        .toList()
        .sortedByDescending { it.second }
        .take(limit)
        .toObjectList()

    override fun topCategories(
        limit: Int,
        since: ZonedDateTime?
    ): ObjectList<Pair<String, Duration>> = entries
        .filter { since == null || it.createdAt.isAfter(since) }
        .groupBy { it.category }
        .mapValues { (_, group) -> group.sumOf { it.durationSeconds }.seconds }
        .toList()
        .sortedByDescending { it.second }
        .take(limit)
        .toObjectList()

    override fun writeToByteBuf(buf: ByteBuf) {
        buf.writeCollection(entries) { buf, entry -> entry.writeToByteBuf(buf) }
    }

    companion object {
        val EMPTY = PlaytimeImpl(objectListOf())

        fun readFromByteBuf(buf: ByteBuf): PlaytimeImpl {
            val entries = buf.readCollection(
                { mutableObjectListOf(it) },
                { PlaytimeEntry.readFromByteBuf(it) }
            )
            return PlaytimeImpl(entries)
        }
    }
}

/**
 * Floors the given [ZonedDateTime] to the nearest interval defined by the [Duration].
 *
 * @param time The [ZonedDateTime] to be floored.
 * @param interval The [Duration] representing the interval to floor to.
 * @return A new [ZonedDateTime] floored to the nearest interval.
 */
private fun floorToInterval(time: ZonedDateTime, interval: Duration): ZonedDateTime {
    val seconds = interval.inWholeSeconds
    val epochSeconds = time.toEpochSecond()
    val floored = (epochSeconds / seconds) * seconds
    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(floored), time.zone)
}

/**
 * Represents a single entry in the playtime data.
 *
 * @property category The category of the playtime entry.
 * @property server The server associated with the playtime entry.
 * @property durationSeconds The duration of playtime in seconds.
 * @property createdAt The timestamp when the playtime entry was created.
 */
@Serializable
data class PlaytimeEntry(
    val id: Long?,
    val category: String,
    val server: String,
    val durationSeconds: Long,
    val createdAt: @Contextual ZonedDateTime,
) {
    fun writeToByteBuf(buf: ByteBuf) {
        buf.writeNullableLong(id)
        buf.writeUtf(category)
        buf.writeUtf(server)
        buf.writeLong(durationSeconds)
        buf.writeZonedDateTime(createdAt)
    }

    companion object {
        fun readFromByteBuf(buf: ByteBuf): PlaytimeEntry {
            val id = buf.readNullableLong()
            val category = buf.readUtf()
            val server = buf.readUtf()
            val durationSeconds = buf.readLong()
            val createdAt = buf.readZonedDateTime()
            return PlaytimeEntry(id, category, server, durationSeconds, createdAt)
        }
    }
}
