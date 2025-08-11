package dev.slne.surf.cloud.api.common.player.whitelist

import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.util.Either
import java.time.ZonedDateTime
import java.util.*

interface WhitelistEntry {
    val uuid: UUID
    val groupOrServerName: Either<String, String>
    val blocked: Boolean

    val createdAt: ZonedDateTime
    val updatedAt: ZonedDateTime
}


interface MutableWhitelistEntry : WhitelistEntry {
    override var blocked: Boolean
    fun forGroup(group: String)
    fun forServer(server: CloudServer)
    fun forServer(serverName: String)
}