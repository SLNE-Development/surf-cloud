package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer

/**
 * Represents a list of users currently connected to a server.
 *
 * This list provides a view of the player data and allows snapshots to be created.
 * The list itself does not directly modify the state of the server.
 */
interface UserList : Collection<CloudPlayer> {

    /**
     * Creates a mutable snapshot of the current user list.
     *
     * The snapshot is independent of the original list and can be modified
     * without affecting the server or the original user list.
     *
     * @return A [MutableUserList] containing a copy of the current user list.
     */
    fun snapshot(): MutableUserList
}

/**
 * Represents a mutable list of users on a server.
 *
 * This interface allows modifications to the list, but any changes
 * will not affect the actual server or its state.
 */
interface MutableUserList : UserList, MutableCollection<CloudPlayer>