package dev.slne.surf.cloud.core.common.player.cache.type

import dev.slne.surf.cloud.api.common.netty.network.codec.encodeToByteArrayDynamic
import dev.slne.surf.cloud.api.common.player.cache.CacheKey
import dev.slne.surf.cloud.api.common.player.cache.CacheSet
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.PlayerCacheSetDeltaPacket.SetOp
import dev.slne.surf.cloud.core.common.player.cache.CloudPlayerCacheImpl
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import java.util.concurrent.ConcurrentHashMap

class CacheSetImpl<E : Any>(
    private val owner: CloudPlayerCacheImpl,
    private val key: CacheKey.Set<E>,
) : CacheSet<E> {
    private val set = ConcurrentHashMap.newKeySet<E>()
    override var suppressOutbound: Boolean = false

    override val size get() = set.size
    override fun isEmpty() = set.isEmpty()
    override fun contains(element: E) = set.contains(element)
    override fun containsAll(elements: Collection<E>) = set.containsAll(elements)

    override fun add(element: E): Boolean {
        val ok = set.add(element)
        if (ok) sendOps(SetOp.Add(key.elementCodec.encodeToByteArrayDynamic(element)))
        return ok
    }

    override fun addAll(elements: Collection<E>): Boolean {
        if (elements.isEmpty()) return false
        var changed = false
        val ops = ObjectArrayList<SetOp>(elements.size)
        for (e in elements) {
            if (set.add(e)) {
                changed = true
                ops += SetOp.Add(key.elementCodec.encodeToByteArrayDynamic(e))
            }
        }
        if (changed) sendOps(*ops.toTypedArray())
        return changed
    }

    override fun remove(element: E): Boolean {
        val ok = set.remove(element)
        if (ok) sendOps(SetOp.Remove(key.elementCodec.encodeToByteArrayDynamic(element)))
        return ok
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        if (elements.isEmpty()) return false
        var changed = false
        val ops = ObjectArrayList<SetOp>(elements.size)
        for (e in elements) {
            if (set.remove(e)) {
                changed = true
                ops += SetOp.Remove(key.elementCodec.encodeToByteArrayDynamic(e))
            }
        }
        if (changed) sendOps(*ops.toTypedArray())
        return changed
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        val target = elements.toHashSet()
        if (set == target) return false
        if (set.isEmpty() && target.isEmpty()) return false

        set.clear()
        sendOps(SetOp.Clear)
        if (target.isNotEmpty()) {
            val ops = target.map { SetOp.Add(key.elementCodec.encodeToByteArrayDynamic(it)) }
            set.addAll(target)
            sendOps(*ops.toTypedArray())
        }
        return true
    }

    override fun clear() {
        if (set.isEmpty()) return
        set.clear()
        sendOps(SetOp.Clear)
    }

    override fun iterator() = snapshot().iterator()

    override fun snapshot() = ObjectOpenHashSet(set)

    private fun sendOps(vararg ops: SetOp) {
        if (suppressOutbound) return
        owner.sendSetDelta(key, ops)
    }
}