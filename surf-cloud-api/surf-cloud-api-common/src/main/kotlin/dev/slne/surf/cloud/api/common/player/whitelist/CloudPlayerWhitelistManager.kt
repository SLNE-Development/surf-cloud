package dev.slne.surf.cloud.api.common.player.whitelist

import dev.slne.surf.cloud.api.common.server.CloudServer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface CloudPlayerWhitelistManager {
    suspend fun whitelistStatusForGroup(group: String): WhitelistStatus
    suspend fun whitelistStatusForServer(server: CloudServer): WhitelistStatus
    suspend fun whitelistStatusForServer(serverName: String): WhitelistStatus

    suspend fun whitelistForGroup(group: String): WhitelistEntry?
    suspend fun whitelistForServer(server: CloudServer): WhitelistEntry?
    suspend fun whitelistForServer(serverName: String): WhitelistEntry?


    suspend fun editWhitelistForGroup(
        group: String,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean

    suspend fun editWhitelistForServer(
        server: CloudServer,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean

    suspend fun editWhitelistForServer(
        serverName: String,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean

    suspend fun createWhitelistForGroup(blocked: Boolean, group: String): WhitelistEntry?
    suspend fun createWhitelistForServer(blocked: Boolean, server: CloudServer): WhitelistEntry?
    suspend fun createWhitelistForServer(blocked: Boolean, serverName: String): WhitelistEntry?
}