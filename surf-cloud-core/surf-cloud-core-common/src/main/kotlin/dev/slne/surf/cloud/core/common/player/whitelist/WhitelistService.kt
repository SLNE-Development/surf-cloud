package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import dev.slne.surf.cloud.api.common.util.Either
import org.springframework.stereotype.Component
import java.util.*

@Component
interface WhitelistService {
    suspend fun whitelistStatus(uuid: UUID, groupOrServer: Either<String, String>): WhitelistStatus
    suspend fun getWhitelist(uuid: UUID, groupOrServer: Either<String, String>): WhitelistEntryImpl?
    suspend fun createWhitelist(whitelist: WhitelistEntryImpl): WhitelistEntryImpl?
    suspend fun editWhitelist(
        uuid: UUID,
        groupOrServer: Either<String, String>,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean

    suspend fun updateWhitelist(updated: MutableWhitelistEntryImpl): Boolean
}