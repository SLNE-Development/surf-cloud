package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistEntry
import dev.slne.surf.cloud.api.common.util.Either
import java.time.ZonedDateTime
import java.util.*

data class WhitelistEntryImpl(
    override val uuid: UUID,
    override val blocked: Boolean,
    override val groupOrServerName: Either<String, String>,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val updatedAt: ZonedDateTime = ZonedDateTime.now(),
) : WhitelistEntry {

    fun toMutableWhitelistEntry() = MutableWhitelistEntryImpl(
        uuid = uuid,
        blocked = blocked,
        groupOrServerName = groupOrServerName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        val GROUP_OR_SERVER_STREAM_CODEC = ByteBufCodecs.either(
            ByteBufCodecs.STRING_CODEC,
            ByteBufCodecs.STRING_CODEC
        )

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            WhitelistEntryImpl::uuid,
            ByteBufCodecs.BOOLEAN_CODEC,
            WhitelistEntryImpl::blocked,
            GROUP_OR_SERVER_STREAM_CODEC,
            WhitelistEntryImpl::groupOrServerName,
            ByteBufCodecs.ZONED_DATE_TIME_CODEC,
            WhitelistEntryImpl::createdAt,
            ByteBufCodecs.ZONED_DATE_TIME_CODEC,
            WhitelistEntryImpl::updatedAt,
            ::WhitelistEntryImpl
        )


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

        fun fromMutableWhitelistEntry(mutableWhitelistEntry: MutableWhitelistEntryImpl): WhitelistEntryImpl {
            return WhitelistEntryImpl(
                uuid = mutableWhitelistEntry.uuid,
                blocked = mutableWhitelistEntry.blocked,
                groupOrServerName = mutableWhitelistEntry.groupOrServerName,
                createdAt = mutableWhitelistEntry.createdAt,
                updatedAt = mutableWhitelistEntry.updatedAt
            )
        }
    }
}