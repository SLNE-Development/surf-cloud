package dev.slne.surf.cloud.api.server

import dev.slne.surf.cloud.api.common.CloudInstance
import org.jetbrains.annotations.ApiStatus
import java.util.*

@ApiStatus.NonExtendable
interface CloudServerInstance : CloudInstance {
    suspend fun exportPlayerData(uuid: UUID): String
    suspend fun deleteNotInterestingPlayerData(uuid: UUID)

    companion object :
        CloudServerInstance by CloudInstance.instance as CloudServerInstance
}