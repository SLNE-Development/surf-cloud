package dev.slne.surf.cloud.velocity.permission

import dev.slne.surf.cloud.core.common.permission.CommonCloudPermissions

object VelocityPermissionRegistry {
    private const val COMMAND_PREFIX = "${CommonCloudPermissions.PREFIX}.command"

    const val CLOUD_COMMAND = "$COMMAND_PREFIX.vcloud"
    const val CLOUD_COMMAND_RELOAD = "$CLOUD_COMMAND.reload"
}