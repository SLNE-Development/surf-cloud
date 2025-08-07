package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.CloudPlayerWhitelistManager
import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistEntry
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.util.bean
import java.util.*

class CloudPlayerWhitelistManagerImpl(private val uuid: UUID) : CloudPlayerWhitelistManager {

    override suspend fun whitelistStatusForGroup(group: WhitelistEntry.Group): WhitelistStatus {
        return bean<WhitelistService>().whitelistStatus(uuid, Either.left(group))
    }

    override suspend fun whitelistStatusForServer(server: CloudServer): WhitelistStatus {
        val groupStatus = whitelistStatusForGroup(server.group)
        if (groupStatus != WhitelistStatus.NONE) {
            return groupStatus
        }

        return whitelistStatusForServer(server.name)
    }

    override suspend fun whitelistStatusForServer(serverName: WhitelistEntry.ServerName): WhitelistStatus {
        return bean<WhitelistService>().whitelistStatus(uuid, Either.right(serverName))
    }

    override suspend fun whitelistForGroup(group: WhitelistEntry.Group): WhitelistEntry? {
        return bean<WhitelistService>().getWhitelist(uuid, Either.left(group))
    }

    override suspend fun whitelistForServer(server: CloudServer): WhitelistEntry? {
        return bean<WhitelistService>().getWhitelist(uuid, Either.right(server.name))
    }

    override suspend fun whitelistForServer(serverName: WhitelistEntry.ServerName): WhitelistEntry? {
        return bean<WhitelistService>().getWhitelist(uuid, Either.right(serverName))
    }

    override suspend fun createWhitelistForGroup(blocked: Boolean, group: String): WhitelistEntry? {
        return createWhitelist(blocked, group, null)
    }

    override suspend fun createWhitelistForServer(
        blocked: Boolean,
        server: CloudServer
    ): WhitelistEntry? {
        return createWhitelist(blocked, null, server.name)
    }

    override suspend fun createWhitelistForServer(
        blocked: Boolean,
        serverName: WhitelistEntry.ServerName
    ): WhitelistEntry? {
        return createWhitelist(blocked, null, serverName)
    }

    private suspend fun createWhitelist(
        blocked: Boolean,
        group: WhitelistEntry.Group?,
        server: WhitelistEntry.ServerName?,
    ): WhitelistEntry? {
        val entry = WhitelistEntryImpl(
            uuid = uuid,
            blocked = blocked,
            group = group,
            serverName = server
        )

        return bean<WhitelistService>().createWhitelist(entry)
    }

    override suspend fun editWhitelistForGroup(
        group: WhitelistEntry.Group,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean {
        return bean<WhitelistService>().editWhitelist(uuid, Either.left(group), edit)
    }

    override suspend fun editWhitelistForServer(
        server: CloudServer,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean {
        return editWhitelistForServer(server.name, edit)
    }

    override suspend fun editWhitelistForServer(
        serverName: WhitelistEntry.ServerName,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean {
        return bean<WhitelistService>().editWhitelist(uuid, Either.right(serverName), edit)
    }
}