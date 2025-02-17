package dev.slne.surf.cloud.api.server

import dev.slne.surf.cloud.api.common.SurfCloudInstance
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

@ApiStatus.NonExtendable
interface SurfCloudServerInstance: SurfCloudInstance {
    suspend fun exportPlayerData(uuid: UUID): String
    suspend fun deleteNotInterestingPlayerData(uuid: UUID)
}

val cloudServerInstance: SurfCloudServerInstance
    get() = SurfCloudInstance.instance as SurfCloudServerInstance