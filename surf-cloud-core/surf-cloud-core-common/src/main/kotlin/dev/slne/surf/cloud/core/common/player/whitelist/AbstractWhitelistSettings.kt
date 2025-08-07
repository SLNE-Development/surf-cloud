package dev.slne.surf.cloud.core.common.player.whitelist

import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistSettings
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.sync.SyncSet

abstract class AbstractWhitelistSettings : WhitelistSettings {
    protected val enforcedGroups = SyncSet<String>("whitelist:enforcedGroups")
    protected val enforcedServers = SyncSet<String>("whitelist:enforcedServers")

    override fun enforcedGroups() = enforcedGroups.snapshot()
    override fun enforcedServers() = enforcedServers.snapshot()

    override fun isWhitelistEnforcedFor(server: CloudServer) =
        isWhitelistEnforcedForGroup(server.group) || isWhitelistEnforcedForServer(server.name)

    override fun isWhitelistEnforcedForGroup(group: String): Boolean {
        return group.lowercase() in enforcedGroups
    }

    override fun isWhitelistEnforcedForServer(server: String): Boolean {
        return server.lowercase() in enforcedServers
    }

    override fun enforceWhitelistForGroup(group: String) {
        enforcedGroups += group.lowercase()
    }

    override fun enforceWhitelistForServer(server: String) {
        enforcedServers += server.lowercase()
    }

    override fun enforceWhitelistForServer(server: CloudServer) {
        enforceWhitelistForServer(server.name)
    }

    override fun disableWhitelistForGroup(group: String) {
        enforcedGroups -= group.lowercase()
    }

    override fun disableWhitelistForServer(server: String) {
        enforcedServers -= server.lowercase()
    }

    override fun disableWhitelistForServer(server: CloudServer) {
        disableWhitelistForServer(server.name)
    }
}