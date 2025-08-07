package dev.slne.surf.cloud.api.common.player.whitelist

import dev.slne.surf.cloud.api.common.server.CloudServer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface CloudPlayerWhitelistManager {
    suspend fun whitelistStatusForGroup(group: WhitelistEntry.Group): WhitelistStatus
    suspend fun whitelistStatusForServer(server: CloudServer): WhitelistStatus
    suspend fun whitelistStatusForServer(serverName: WhitelistEntry.ServerName): WhitelistStatus

    suspend fun whitelistForGroup(group: WhitelistEntry.Group): WhitelistEntry?
    suspend fun whitelistForServer(server: CloudServer): WhitelistEntry?
    suspend fun whitelistForServer(serverName: WhitelistEntry.ServerName): WhitelistEntry?


    suspend fun editWhitelistForGroup(
        group: WhitelistEntry.Group,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean

    suspend fun editWhitelistForServer(
        server: CloudServer,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean

    suspend fun editWhitelistForServer(
        serverName: WhitelistEntry.ServerName,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean

    suspend fun createWhitelistForGroup(blocked: Boolean, group: WhitelistEntry.Group): WhitelistEntry?
    suspend fun createWhitelistForServer(blocked: Boolean, server: CloudServer): WhitelistEntry?
    suspend fun createWhitelistForServer(blocked: Boolean, serverName: WhitelistEntry.ServerName): WhitelistEntry?
}