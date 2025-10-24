package dev.slne.surf.cloud.standalone.player.pdc

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.player.ppdc.PersistentPlayerDataContainerImpl
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcOp
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcPatch
import dev.slne.surf.surfapi.core.api.nbt.FastCompoundBinaryTag
import dev.slne.surf.surfapi.core.api.nbt.fast
import net.kyori.adventure.nbt.CompoundBinaryTag

class TrackingPlayerPersistentDataContainerImpl(
    tag: FastCompoundBinaryTag = CompoundBinaryTag.empty().fast()
) : PersistentPlayerDataContainerImpl() {
    @Volatile
    private var baseSnapshot: CompoundBinaryTag? = null

    fun startTracking() {
        require(baseSnapshot == null) { "Tracking already started" }

        baseSnapshot = toTagCompound()
    }

    fun getPatchOps(): PdcPatch {
        val base = baseSnapshot
        val curr = toTagCompound()
        val ops = diffToOps(base, curr)

        return PdcPatch(ops)
    }

    fun clearTracking() {
        baseSnapshot = null
    }

    private fun diffToOps(
        base: CompoundBinaryTag?,
        curr: CompoundBinaryTag?,
        prefix: List<String> = emptyList()
    ): MutableList<PdcOp> {
        val ops = mutableObjectListOf<PdcOp>()

        if (base == null && curr != null) {
            ops += PdcOp.Put(prefix, curr)
            return ops
        }

        if (base != null && curr == null) {
            ops += PdcOp.Remove(prefix)
            return ops
        }

        if (base == null && curr == null) return ops

        val baseKeys = base!!.keySet()
        val currKeys = curr!!.keySet()

        for (k in baseKeys - currKeys) {
            ops += PdcOp.Remove(prefix + k)
        }

        for (k in currKeys) {
            val b = base.get(k)
            val c = curr.get(k)

            if (b == c) continue

            when {
                b is CompoundBinaryTag && c is CompoundBinaryTag -> {
                    ops += diffToOps(b, c, prefix + k)
                }

                else -> {
                    ops += PdcOp.Put(prefix + k, c!!)
                }
            }
        }
        return ops
    }
}