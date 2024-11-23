package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer

/**
 * Represents a list of users on a server.
 *
 * This list acts as a reference to the player UUIDs on the server
 * and does not directly modify the server state.
 */
interface UserList : Collection<CloudPlayer> {

    /**
     * Creates a mutable snapshot of the current user list.
     *
     * Changes made to the snapshot will not affect the original user list or the server.
     *
     * @return A [MutableUserList] representing a snapshot of the current user list.
     */
    fun snapshot(): MutableUserList
}

/**
 * Represents a mutable list of users on a server.
 *
 * Any changes made to this list will have no effect on the server or its actual state.
 */
interface MutableUserList : UserList, MutableCollection<CloudPlayer>