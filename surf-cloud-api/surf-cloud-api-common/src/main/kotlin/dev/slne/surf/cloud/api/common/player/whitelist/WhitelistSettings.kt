package dev.slne.surf.cloud.api.common.player.whitelist

import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.server.CloudServer
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.Unmodifiable

interface WhitelistSettings {
    fun enforcedGroups(): @Unmodifiable ObjectSet<String>
    fun enforcedServers(): @Unmodifiable ObjectSet<String>

    fun isWhitelistEnforcedFor(server: CloudServer): Boolean
    fun isWhitelistEnforcedForGroup(group: String): Boolean
    fun isWhitelistEnforcedForServer(server: String): Boolean

    fun enforceWhitelistForGroup(group: String)
    fun enforceWhitelistForServer(server: String)
    fun enforceWhitelistForServer(server: CloudServer)

    fun disableWhitelistForGroup(group: String)
    fun disableWhitelistForServer(server: String)
    fun disableWhitelistForServer(server: CloudServer)


    /**
     * Refreshes the whitelist-enforcement state for all online players
     * and disconnects anyone that is not permitted to stay.
     */
    fun refresh()

    companion object : WhitelistSettings by CloudInstance.getBean(WhitelistSettings::class.java)
}