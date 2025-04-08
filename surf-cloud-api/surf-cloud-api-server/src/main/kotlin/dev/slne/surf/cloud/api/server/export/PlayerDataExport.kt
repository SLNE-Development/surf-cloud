package dev.slne.surf.cloud.api.server.export

import kotlinx.serialization.Serializable

interface PlayerDataExport

@Serializable
object PlayerDataExportEmpty : PlayerDataExport