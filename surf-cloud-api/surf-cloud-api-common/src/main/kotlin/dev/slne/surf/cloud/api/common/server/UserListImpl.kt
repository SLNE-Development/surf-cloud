package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.playerManager
import dev.slne.surf.cloud.api.common.util.InternalApi
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.synchronize
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

@InternalApi
open class UserListImpl : UserList {
    companion object {
        val STREAM_CODEC = streamCodec<SurfByteBuf, UserListImpl>({ buf, list ->
            buf.writeCollection(list.playerReferences) { buffer, uuid -> buffer.writeUuid(uuid) }
        }, { buf ->
            UserListImpl(buf.readCollection({ mutableObjectSetOf(it) }, { it.readUuid() }))
        })
    }

    internal val playerReferences: ObjectSet<UUID>

    override val size get() = playerReferences.size

    constructor() {
        playerReferences = mutableObjectSetOf<UUID>().synchronize()
    }

    internal constructor(playerReferences: ObjectSet<UUID>) {
        this.playerReferences = playerReferences.synchronize()
    }

    override fun contains(element: CloudPlayer) = playerReferences.contains(element.uuid)
    override fun containsAll(elements: Collection<CloudPlayer>) =
        elements.all { playerReferences.contains(it.uuid) }

    override fun isEmpty() = playerReferences.isEmpty()
    override fun iterator(): Iterator<CloudPlayer> {
        return object : Iterator<CloudPlayer> {
            private val iterator = playerReferences.iterator()
            override fun hasNext() = iterator.hasNext()
            override fun next() =
                iterator.next().let { playerManager.getPlayer(it) } ?: throw NoSuchElementException(
                    "Player not found"
                )
        }
    }

    fun add(playerUuid: UUID): Boolean {
        return playerReferences.add(playerUuid)
    }

    fun remove(playerUuid: UUID): Boolean {
        return playerReferences.remove(playerUuid)
    }

    override fun snapshot(): MutableUserList {
        return MutableUserListImpl().also { it.addAll(this) }
    }

    override fun toString(): String {
        return "UserListImpl(playerReferences=$playerReferences, size=$size)"
    }
}

@InternalApi
class MutableUserListImpl : UserListImpl(), MutableUserList {
    override fun add(element: CloudPlayer): Boolean {
        return add(element.uuid)
    }

    override fun addAll(elements: Collection<CloudPlayer>): Boolean {
        return elements.all { playerReferences.add(it.uuid) }
    }

    override fun clear() {
        playerReferences.clear()
    }

    override fun iterator(): MutableIterator<CloudPlayer> {
        return object : MutableIterator<CloudPlayer> {
            private val iterator = playerReferences.iterator()
            override fun hasNext() = iterator.hasNext()
            override fun next() =
                iterator.next().let { playerManager.getPlayer(it) } ?: throw NoSuchElementException(
                    "Player not found"
                )

            override fun remove() {
                iterator.remove()
            }
        }
    }

    override fun remove(element: CloudPlayer): Boolean {
        return remove(element.uuid)
    }

    override fun removeAll(elements: Collection<CloudPlayer>): Boolean {
        return elements.all { playerReferences.remove(it.uuid) }
    }

    override fun retainAll(elements: Collection<CloudPlayer>): Boolean {
        return playerReferences.retainAll(elements.map { it.uuid })
    }
}