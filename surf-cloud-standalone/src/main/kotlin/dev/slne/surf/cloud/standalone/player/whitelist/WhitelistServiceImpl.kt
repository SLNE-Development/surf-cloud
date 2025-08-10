package dev.slne.surf.cloud.standalone.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.player.whitelist.MutableWhitelistEntryImpl
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistEntryImpl
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class WhitelistServiceImpl(private val service: WhitelistRepository) : WhitelistService {
    override suspend fun whitelistStatus(
        uuid: UUID,
        groupOrServer: Either<String, String>
    ): WhitelistStatus {
        val status = service.whitelistStatus(uuid, groupOrServer)
        return when {
            status == null -> WhitelistStatus.NONE
            status.blocked -> WhitelistStatus.BLOCKED
            else -> WhitelistStatus.ACTIVE
        }
    }

    override suspend fun getWhitelist(
        uuid: UUID,
        groupOrServer: Either<String, String>
    ): WhitelistEntryImpl? {
        return service.getWhitelist(uuid, groupOrServer)
    }

    override suspend fun createWhitelist(whitelist: WhitelistEntryImpl): WhitelistEntryImpl? {
        return service.createWhitelist(whitelist)
    }

    override suspend fun editWhitelist(
        uuid: UUID,
        groupOrServer: Either<String, String>,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean {
        return service.editWhitelist(uuid, groupOrServer, edit)
    }

    override suspend fun updateWhitelist(updated: MutableWhitelistEntryImpl): Boolean {
        return service.updateWhitelist(updated)
    }

    /**
     * Converts the raw database results returned by [WhitelistRepository.whitelistStatusBatched]
     * into a compact, single [WhitelistStatus] per player.
     *
     * Input  : `(UUID, (serverName, group))`
     * Output : `(UUID, WhitelistStatus)`
     */
    suspend fun whitelistStatusFor(players: Flow<Pair<UUID, Pair< /*server name*/ String, /*group*/ String>>>): Flow<Pair<UUID, WhitelistStatus>> =
        service.whitelistStatusBatched(players)
            .mapNotNull { (uuid, list) ->
                val present = list.filterNotNull()
                uuid to when {
                    present.isEmpty() -> WhitelistStatus.NONE
                    present.any { it.blocked } && present.any { !it.blocked } -> WhitelistStatus.UNKNOWN
                    present.any { it.blocked } -> WhitelistStatus.BLOCKED
                    else -> WhitelistStatus.ACTIVE
                }
            }
}