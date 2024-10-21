package dev.slne.surf.cloud.core.netty.common.source.tracker

import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.api.netty.source.tracker.NettyClientTracker
import dev.slne.surf.cloud.api.util.freeze
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.api.util.objectSetOf
import dev.slne.surf.cloud.api.util.toObjectSet
import dev.slne.surf.cloud.core.netty.AbstractNettyBase

open class NettyClientTrackerImpl<Client : ProxiedNettySource<Client>>(private val nettyBase: AbstractNettyBase<*, *, Client>) :
    NettyClientTracker<Client> {
    private val log = logger()
    protected val _clients = objectSetOf<Client>()
    override val clients get() = _clients.freeze()

    override fun findByGroupId(groupId: String) = _clients.asSequence()
        .filter { it.cloudServer?.groupId == groupId }
        .toObjectSet()

    override fun findByServerGuid(serverGuid: Long) =
        _clients.firstOrNull { it.cloudServer?.serverGuid == serverGuid }

    fun addClient(client: Client): Client {
        if (!_clients.add(client)) {
            log.atWarning()
                .withStackTrace(StackSize.MEDIUM)
                .log("Client %s already exists in the list", client)
        }

        return client
    }

    fun removeClient(client: Client) = _clients.remove(client)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NettyClientTrackerImpl<*>) return false

        if (nettyBase != other.nettyBase) return false
        if (_clients != other._clients) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nettyBase.hashCode()
        result = 31 * result + _clients.hashCode()
        return result
    }

    override fun toString(): String {
        return "NettyClientTrackerImpl(nettyBase=$nettyBase, _clients=$_clients)"
    }
}
