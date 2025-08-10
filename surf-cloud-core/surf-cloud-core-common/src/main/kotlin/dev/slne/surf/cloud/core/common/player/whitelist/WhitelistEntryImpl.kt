package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistEntry
import dev.slne.surf.cloud.api.common.util.Either
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class WhitelistEntryImpl(
    override val uuid: @Contextual UUID,
    override val blocked: Boolean,
    override val groupOrServerName: Either<String, String>,
    override val createdAt: @Contextual ZonedDateTime = ZonedDateTime.now(),
    override val updatedAt: @Contextual ZonedDateTime = ZonedDateTime.now(),
) : WhitelistEntry {

    fun toMutableWhitelistEntry() = MutableWhitelistEntryImpl(
        uuid = uuid,
        blocked = blocked,
        groupOrServerName = groupOrServerName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun createGroupOrServerRaw(
            group: String?,
            serverName: String?
        ): Either<String, String> {
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