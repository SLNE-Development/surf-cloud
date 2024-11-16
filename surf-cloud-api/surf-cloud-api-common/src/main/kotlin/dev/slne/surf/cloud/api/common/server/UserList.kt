package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer

/**
 * A list of users on a server. This list is basically just a reference to the player uuids on the server.
 */
interface UserList : Collection<CloudPlayer> {
    fun snapshot(): MutableUserList
}

/**
 * A mutable list of users on a server. Any changes to this list will take no effect on the server.
 */
interface MutableUserList : UserList, MutableCollection<CloudPlayer>