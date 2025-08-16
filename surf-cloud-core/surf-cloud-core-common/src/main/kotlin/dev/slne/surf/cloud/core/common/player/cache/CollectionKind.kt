package dev.slne.surf.cloud.core.common.player.cache

import kotlinx.serialization.Serializable

@Serializable
enum class CollectionKind {
    LIST,
    SET,
    MAP
}