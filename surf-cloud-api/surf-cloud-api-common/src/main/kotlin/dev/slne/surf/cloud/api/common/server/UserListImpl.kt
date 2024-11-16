package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.playerManager
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.synchronize
import java.util.*

open class UserListImpl : UserList {
    internal val playerReferences = mutableObjectSetOf<UUID>().synchronize()

    override val size get() = playerReferences.size
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

    fun add(player: CloudPlayer): Boolean {
        return playerReferences.add(player.uuid)
    }

    fun remove(player: CloudPlayer): Boolean {
        return playerReferences.remove(player.uuid)
    }

    override fun snapshot(): MutableUserList {
        return MutableUserListImpl().also { it.addAll(this) }
    }
}

class MutableUserListImpl: UserListImpl(), MutableUserList {
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

    override fun removeAll(elements: Collection<CloudPlayer>): Boolean {
        return elements.all { playerReferences.remove(it.uuid) }
    }

    override fun retainAll(elements: Collection<CloudPlayer>): Boolean {
        return playerReferences.retainAll(elements.map { it.uuid })
    }
}