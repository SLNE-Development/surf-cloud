package dev.slne.surf.cloud.core.common.player.cache

sealed interface CacheOperationResult {
    data class Success(val newVersion: Long) : CacheOperationResult
    data object VersionMismatch : CacheOperationResult
}