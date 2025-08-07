package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistEntry
import dev.slne.surf.cloud.api.common.util.Either
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class WhitelistEntryImpl(
    override val uuid: @Contextual UUID,
    override val blocked: Boolean,
    val group: String?,
    val serverName: String?,
    override val createdAt: @Contextual ZonedDateTime = ZonedDateTime.now(),
    override val updatedAt: @Contextual ZonedDateTime = ZonedDateTime.now(),
) : WhitelistEntry {

    @Transient
    override val groupOrServerName = createGroupOrServerRaw(group, serverName)

    fun toMutableWhitelistEntry() = MutableWhitelistEntryImpl(
        uuid = uuid,
        blocked = blocked,
        group = group,
        serverName = serverName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun createGroupOrServerRaw(
            group: String?,
            serverName: String?
        ): Either<WhitelistEntry.Group, WhitelistEntry.ServerName> {
            var found = false
            var either: Either<String, String>? = null

            if (group != null) {
                found = true
                either = Either.left(group)
            }

            if (serverName != null) {
                require(!found) { "Either group or serverName must be provided, not both." }
                either = Either.right(serverName)
            }

            return either ?: error("Either group or serverName must be provided.")
        }
    }
}