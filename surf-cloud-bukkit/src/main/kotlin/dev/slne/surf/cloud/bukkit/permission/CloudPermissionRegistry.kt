package dev.slne.surf.cloud.bukkit.permission

import dev.slne.surf.cloud.core.common.permission.CommonCloudPermissions
import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object CloudPermissionRegistry: PermissionRegistry() {
    private const val COMMAND_PREFIX = "${CommonCloudPermissions.PREFIX}.command"

    val FIND_COMMAND = create("$COMMAND_PREFIX.find")
    val FIND_COMMAND_TELEPORT = create("$COMMAND_PREFIX.find.teleport")
    val SERVER_COMMAND = create("$COMMAND_PREFIX.server")
    const val SPECIFIC_SERVER_PERMISSION_PREFIX = "$COMMAND_PREFIX.server."
    const val ALL_SERVER_PERMISSION = "$COMMAND_PREFIX.server.*"
    val PLAYTIME_COMMAND = create("$COMMAND_PREFIX.playtime")
    val PLAYTIME_COMMAND_OTHER = create("$COMMAND_PREFIX.playtime.other")
    val LAST_SEEN_COMMAND = create("$COMMAND_PREFIX.lastseen")
    val BROADCAST_COMMAND = create("$COMMAND_PREFIX.broadcast")
    val GLIST_COMMAND = create("$COMMAND_PREFIX.glist")
    val TIMEOUT_COMMAND = create("$COMMAND_PREFIX.timeout")
    val ANTICHEAT_BAN_COMMAND = create("$COMMAND_PREFIX.acban")
}