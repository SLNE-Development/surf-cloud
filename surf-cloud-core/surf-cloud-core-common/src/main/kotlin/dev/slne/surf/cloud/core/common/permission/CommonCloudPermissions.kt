package dev.slne.surf.cloud.core.common.permission

object CommonCloudPermissions { // TODO: Auto register to luckperms?
    const val PREFIX = "surfcloud"
    private const val ANNOUNCE_PREFIX = "$PREFIX.announce"
    private const val QUEUE_PREFIX = "$PREFIX.queue"


    const val ANNOUNCE_BANNED_TRIED_TO_JOIN = "$ANNOUNCE_PREFIX.punish.banned-join"

    const val QUEUE_BYPASS = "$QUEUE_PREFIX.bypass"
    const val QUEUE_BYPASS_FULL = "$QUEUE_PREFIX.bypass.full"
}