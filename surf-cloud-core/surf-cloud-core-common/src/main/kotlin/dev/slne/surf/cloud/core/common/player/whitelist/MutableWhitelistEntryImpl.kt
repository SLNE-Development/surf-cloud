package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.util.Either
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class MutableWhitelistEntryImpl(
    override val uuid: @Contextual UUID,
    override var blocked: Boolean,
    var group: String?,
    var serverName: String?,
    override val createdAt: @Contextual ZonedDateTime,
    override val updatedAt: @Contextual ZonedDateTime,
) : MutableWhitelistEntry {
    @Transient
    override var groupOrServerName = WhitelistEntryImpl.createGroupOrServerRaw(group, serverName)

    override fun forGroup(group: String) {
        this.group = group
        this.serverName = null
        groupOrServerName = Either.left(group)
    }

    override fun forServer(server: CloudServer) {
        forServer(server.name)
    }

    override fun forServer(serverName: String) {
        this.serverName = serverName
        this.group = null
        groupOrServerName = Either.right(serverName)
    }
}