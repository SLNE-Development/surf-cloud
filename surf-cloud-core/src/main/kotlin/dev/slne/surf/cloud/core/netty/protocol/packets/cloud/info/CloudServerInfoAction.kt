package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info

enum class CloudServerInfoAction {
    /**
     * Updates properties of a [dev.slne.surf.cloud.api.server.CloudServer]
     */
    UPDATE_SERVER_INFO,

    /**
     * Removes a [dev.slne.surf.cloud.api.server.CloudServer]
     */
    REMOVE_SERVER_INFO,

    /**
     * Adds a [dev.slne.surf.cloud.api.server.CloudServer]
     */
    ADD_SERVER_INFO
}
