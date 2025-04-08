package dev.slne.surf.cloud.api.server.export

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class RootExportPlayerData(@Contextual val data: List<PlayerDataExport>) : PlayerDataExport