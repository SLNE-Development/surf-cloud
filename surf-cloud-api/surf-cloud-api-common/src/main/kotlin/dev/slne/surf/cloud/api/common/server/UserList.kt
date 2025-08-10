package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Represents the *current* set of users connected to a server.
 *
 * ### Thread-safety and iteration semantics
 * Implementations of this interface are designed to be used concurrently.
 * The default implementation ([UserListImpl]) is backed by a
 * lock-free, weakly-consistent iterator (via a `ConcurrentHashMap`-based key set),
 * which means iteration does not block writes and will not throw [java.util.ConcurrentModificationException].
 * However, a running iteration may or may not reflect concurrent additions/removals.
 * If you require a stable, point-in-time view, call {@link #uuidSnapshot()} or {@link #snapshot()}.
 *
 * ### Mutability
 * This is a read-only view. To obtain a mutable copy that you can modify freely
 * (without affecting the underlying server state), use [snapshot] which returns
 * a [MutableUserList].
 */
@ApiStatus.NonExtendable
interface UserList : Collection<CloudPlayer> {

    /**
     * Creates a mutable, point-in-time copy of the current user list.
     *
     * The returned snapshot is independent from the live list; modifying the snapshot
     * has no effect on the server or the original list.
     *
     * @return a new [MutableUserList] containing a copy of the current users.
     */
    fun snapshot(): MutableUserList

    /**
     * Returns a snapshot of all player UUIDs as a contiguous array-backed list.
     *
     * The returned list is safe to iterate without blocking writers and remains
     * stable even if the underlying live set changes after the call.
     *
     *
     * **Performance:** This method allocates a new list and copies all
     * UUIDs (O(n)). Prefer this when you need a consistent view across a longer
     * operation or when you plan to iterate multiple times.
     *
     * @return an [ObjectArrayList] containing the UUIDs at the time of the call.
     */
    fun uuidSnapshot(): ObjectArrayList<UUID>
}

/**
 * A mutable user list.
 *
 * This type is intended for working with detached snapshots obtained via
 * [UserList.snapshot].
 * Mutating a [MutableUserList] does not affect
 * the server or the original live list.
 */
@ApiStatus.NonExtendable
interface MutableUserList : UserList, MutableCollection<CloudPlayer>

/**
 * Performs a fast, lock-free traversal over the UUIDs of this list using the
 * default implementation's weakly-consistent iterator.
 *
 * **Important:** This extension relies on the instance being backed by
 * [UserListImpl]. If you pass a custom implementation, a [ClassCastException]
 * may occur. Use [UserList.uuidSnapshot] for a portable, implementation-agnostic
 * traversal.
 *
 * @receiver the user list to traverse (must be [UserListImpl]).
 * @param action callback invoked for each UUID visible to the iterator at traversal time.
 */
inline fun UserList.forEachWeakUuid(action: (UUID) -> Unit) {
    (this as UserListImpl).forEachWeakUuid(action)
}