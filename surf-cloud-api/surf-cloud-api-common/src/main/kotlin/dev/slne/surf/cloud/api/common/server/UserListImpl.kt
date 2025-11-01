package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.server.UserListImpl.Companion.STREAM_CODEC
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Default, high-throughput implementation of [UserList] optimized for
 * concurrent reads and frequent iteration in large Minecraft networks.
 *
 * ### Design rationale
 *
 * - **Concurrency**: Backed by [ConcurrentHashMap.newKeySet], providing
 * fine-grained striping/CAS and weakly-consistent iterators. Readers never block writers, which
 * is crucial when plugins may traverse thousands of players while joins/leaves occur.
 *
 * - **Stable wire format**: Serialization via [STREAM_CODEC] uses a
 * snapshot array to ensure a consistent view on the wire even during concurrent updates.
 *
 * - **Snapshot cache**: A lightweight modification counter ([modSeq]) invalidates a
 * cached array snapshot to avoid repeated allocations when the set is unchanged (e.g., multiple
 * serializations per tick).
 *
 * ### Iteration semantics
 * Iteration is weakly consistent: it never throws [java.util.ConcurrentModificationException],
 * but may omit or not yet include elements added/removed concurrently. Use [uuidSnapshot] for
 * a stable, point-in-time collection.
 */
@InternalApi
open class UserListImpl : UserList {
    companion object {

        /**
         * Binary codec for streaming a [UserListImpl] over [SurfByteBuf].
         *
         * **Consistency:** The codec writes a snapshot array captured at the start of encoding,
         * ensuring readers see a coherent list of UUIDs regardless of concurrent modifications.
         */
        val STREAM_CODEC = streamCodec<SurfByteBuf, UserListImpl>({ buf, list ->
            val snapshot = list.snapshotArray()
            buf.writeArray(snapshot) { buffer, uuid -> buffer.writeUuid(uuid) }
        }, { buf ->
            val uuids = buf.readArray { buf.readUuid() }
            UserListImpl(ObjectArrayList(uuids))
        })


        /**
         * Creates a [UserListImpl] from a preexisting set of UUIDs.
         *
         * @param uuids UUIDs to initialize the list with.
         * @return a new [UserListImpl] containing the given UUIDs.
         */
        fun of(uuids: Set<UUID>): UserListImpl {
            return UserListImpl(ObjectArrayList(uuids))
        }
    }

    /** Backing set: scalable, lock-free key set. */
    @PublishedApi
    internal val uuids: ConcurrentHashMap.KeySetView<UUID, Boolean>

    /** Modification counter used to invalidate the cached snapshot array. */
    protected val modSeq = AtomicLong(0)

    @Volatile
    private var cachedSnapshot: Array<UUID> = emptyArray()

    @Volatile
    private var cachedModSeq: Long = -1

    /** Creates an empty user list. */
    constructor() {
        uuids = ConcurrentHashMap.newKeySet<UUID>()
    }

    /**
     * Creates a user list preloaded with UUIDs.
     *
     * @param initial UUIDs to preload into the set.
     */
    internal constructor(initial: ObjectArrayList<UUID>) {
        uuids = ConcurrentHashMap.newKeySet<UUID>(initial.size)
        uuids.addAll(initial)
    }

    override val size
        get() = uuids.size

    override fun isEmpty() = uuids.isEmpty()
    override fun contains(element: CloudPlayer) = uuids.contains(element.uuid)
    override fun containsAll(elements: Collection<CloudPlayer>): Boolean {
        for (e in elements) if (!uuids.contains(e.uuid)) return false
        return true
    }

    /**
     * Returns an iterator over players resolved on demand from [CloudPlayerManager].
     *
     * **Note:** If a UUID in the set cannot be resolved to a [CloudPlayer]
     * (e.g., the player disconnected between reading the UUID and resolution),
     * a [NoSuchElementException] is thrown from [java.util.Iterator.next] to signal the race.
     *
     * @throws NoSuchElementException if a UUID cannot be resolved to a player at iteration time.
     */
    override fun iterator() = object : Iterator<CloudPlayer> {
        private val iterator = uuids.iterator()
        override fun hasNext() = iterator.hasNext()
        override fun next() = CloudPlayerManager.getPlayer(iterator.next())
            ?: throw NoSuchElementException("Player not found")
    }

    /**
     * Implementation-specific fast traversal over UUIDs using the weakly-consistent iterator.
     *
     * @param action callback invoked for each UUID visible to the iterator.
     */
    inline fun forEachWeakUuid(action: (UUID) -> Unit) {
        for (u in uuids) action(u)
    }

    override fun snapshot(): MutableUserList {
        return MutableUserListImpl(uuidSnapshot())
    }

    override fun uuidSnapshot() = ObjectArrayList(snapshotArray())

    /**
     * Returns a cached snapshot array of UUIDs, invalidated whenever the set changes.
     *
     * **Performance:** Avoids repeated allocations between unchanged serializations.
     * The snapshot is coherent at the time of creation and unaffected by later mutations.
     *
     * @return array of UUIDs representing a point-in-time view.
     */
    fun snapshotArray(): Array<UUID> {
        val m = modSeq.get()
        if (m == cachedModSeq) return cachedSnapshot
        val snap = uuids.toTypedArray()
        cachedSnapshot = snap
        cachedModSeq = m
        return snap
    }


    /**
     * Adds a player UUID to the live set.
     *
     * @param playerUuid the UUID to add.
     * @return `true` if the UUID was not present and has been added; `false` otherwise.
     */
    fun add(playerUuid: UUID): Boolean {
        val added = uuids.add(playerUuid)
        if (added) {
            modSeq.incrementAndGet()
        }
        return added
    }

    /**
     * Removes a player UUID from the live set.
     *
     * @param playerUuid the UUID to remove.
     * @return `true` if the UUID was present and has been removed; `false` otherwise.
     */
    fun remove(playerUuid: UUID): Boolean {
        val removed = uuids.remove(playerUuid)
        if (removed) {
            modSeq.incrementAndGet()
        }
        return removed
    }

    override fun toString(): String {
        return "UserListImpl(size=$size)"
    }
}


/**
 * Mutable snapshot implementation that can be freely modified without impacting the live set.
 *
 * ### Notes
 *
 * - [clear] increments [modSeq] to invalidate the internal snapshot cache.
 * - Mutating via the iterator's [MutableIterator.remove] also bumps [modSeq]
 * to ensure consistent snapshot invalidation.
 * - [retainAll] builds a temporary [ObjectOpenHashSet] of UUIDs to perform the set
 * operation efficiently.
 */
@InternalApi
class MutableUserListImpl(initial: ObjectArrayList<UUID>) : UserListImpl(initial), MutableUserList {
    override fun add(element: CloudPlayer): Boolean {
        return add(element.uuid)
    }

    override fun addAll(elements: Collection<CloudPlayer>): Boolean {
        var changed = false
        for (e in elements) if (add(e.uuid)) changed = true
        return changed
    }

    override fun clear() {
        if (uuids.isNotEmpty()) {
            uuids.clear()
            modSeq.incrementAndGet()
        }
    }

    /**
     * Iterator over players resolved on demand. Removal through the iterator
     * invalidates the snapshot cache to keep [snapshotArray] fresh.
     */
    override fun iterator() = object : MutableIterator<CloudPlayer> {
        private val iterator = uuids.iterator()
        override fun hasNext() = iterator.hasNext()
        override fun next() = CloudPlayerManager.getPlayer(iterator.next())
            ?: throw NoSuchElementException("Player not found")

        override fun remove() {
            iterator.remove()
            modSeq.incrementAndGet()
        }
    }


    override fun remove(element: CloudPlayer): Boolean {
        return remove(element.uuid)
    }

    override fun removeAll(elements: Collection<CloudPlayer>): Boolean {
        var changed = false
        for (e in elements) if (remove(e.uuid)) changed = true
        return changed
    }

    override fun retainAll(elements: Collection<CloudPlayer>): Boolean {
        val keep = ObjectOpenHashSet<UUID>(elements.size)
        for (e in elements) keep.add(e.uuid)
        val changed = uuids.retainAll(keep)
        return changed
    }
}