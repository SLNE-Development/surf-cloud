package dev.slne.surf.cloud.api.common.player.cache

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer

interface PlayerCacheLoader<T : Any> { // TODO: 25.08.2025 14:06 - move to server

    suspend fun loadCache(player: OfflineCloudPlayer): T
}