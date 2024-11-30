package dev.slne.surf.cloud.api.common.server

import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface CloudServer : CommonCloudServer {
    /**
     * Indicates whether the server has allowlist enabled. (Normal vanilla allowlist)
     */
    val whitelist: Boolean
}