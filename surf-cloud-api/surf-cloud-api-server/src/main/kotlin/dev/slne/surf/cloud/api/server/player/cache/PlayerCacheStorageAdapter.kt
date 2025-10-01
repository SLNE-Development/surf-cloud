package dev.slne.surf.cloud.api.server.player.cache

import java.util.*

interface PlayerCacheStorageAdapter<T: Any> {
    suspend fun load(playerId: UUID): T?
    suspend fun save(playerId: UUID, value: T?)
}