package dev.slne.surf.cloud.standalone.player.name

import dev.slne.surf.cloud.api.common.player.name.NameEntry
import dev.slne.surf.cloud.api.common.player.name.NameHistoryFactory
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.player.name.NameHistoryImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerNameHistoryEntity

fun NameHistoryFactory.create(entries: Iterable<CloudPlayerNameHistoryEntity>) =
    NameHistoryImpl(entries.mapTo(mutableObjectListOf()) {
        NameEntry(
            it.createdAt.toEpochSecond(),
            it.name
        )
    })