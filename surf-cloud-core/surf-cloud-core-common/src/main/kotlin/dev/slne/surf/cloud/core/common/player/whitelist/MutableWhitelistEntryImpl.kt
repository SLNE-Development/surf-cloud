package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.util.Either
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class MutableWhitelistEntryImpl(
    override val uuid: @Contextual UUID,
    override var blocked: Boolean,
    override var groupOrServerName: Either<String, String>,
    override val createdAt: @Contextual ZonedDateTime,
    override val updatedAt: @Contextual ZonedDateTime,
) : MutableWhitelistEntry {

    override fun forGroup(group: String) {
        groupOrServerName = Either.left(group)
    }

    override fun forServer(server: CloudServer) {
        forServer(server.name)
    }

    override fun forServer(serverName: String) {
        groupOrServerName = Either.right(serverName)
    }
}