package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.util.Either
import java.time.ZonedDateTime
import java.util.*

data class MutableWhitelistEntryImpl(
    override val uuid: UUID,
    override var blocked: Boolean,
    override var groupOrServerName: Either<String, String>,
    override val createdAt: ZonedDateTime,
    override val updatedAt: ZonedDateTime,
) : MutableWhitelistEntry {

    companion object {
        val STREAM_CODEC = WhitelistEntryImpl.STREAM_CODEC.map(
            WhitelistEntryImpl::toMutableWhitelistEntry,
            WhitelistEntryImpl::fromMutableWhitelistEntry
        )
    }

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