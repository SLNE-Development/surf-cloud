package dev.slne.surf.cloud.standalone.player.name

import dev.slne.surf.cloud.api.common.player.name.NameEntry
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.toEpochUtcMillis
import dev.slne.surf.cloud.standalone.player.db.player.name.CloudPlayerNameHistoryEntity
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectLists

class NameHistoryImpl(private val entries: ObjectList<NameEntry>) : NameHistory {
    constructor(entries: Iterable<CloudPlayerNameHistoryEntity>) : this(
        entries.mapTo(
            mutableObjectListOf()
        ) { NameEntry(it.createdDate.toEpochSecond(), it.name) }
    )

    override val nameChanges = entries.size

    override fun names(): ObjectList<NameEntry> = ObjectLists.unmodifiable(entries)
    override fun lastKnownName(): NameEntry? = entries.firstOrNull()
    override fun getNameAtTime(time: Long): NameEntry? = entries.find { it.timestamp <= time }
    override fun wasNameUsed(name: String): Boolean = entries.any { it.name == name }
    override fun firstUsedName(): NameEntry? = entries.lastOrNull()

    override fun timeSinceLastChange(): Long =
        entries.firstOrNull()?.let { System.currentTimeMillis() - it.timestamp } ?: 0
}