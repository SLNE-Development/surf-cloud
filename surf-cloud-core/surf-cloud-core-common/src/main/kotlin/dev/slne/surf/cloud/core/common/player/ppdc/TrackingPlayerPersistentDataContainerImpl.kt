package dev.slne.surf.cloud.core.common.player.ppdc

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcOp
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcPatch
import dev.slne.surf.surfapi.core.api.nbt.FastCompoundBinaryTag
import dev.slne.surf.surfapi.core.api.nbt.fast
import net.kyori.adventure.nbt.CompoundBinaryTag

open class TrackingPlayerPersistentDataContainerImpl(
    tag: FastCompoundBinaryTag = CompoundBinaryTag.empty().fast()
) : PersistentPlayerDataContainerImpl(tag) {
    @Volatile
    private var baseSnapshotReference: CompoundBinaryTag? = null

    fun startTracking() {
        check(baseSnapshotReference == null) { "Tracking already started" }

        baseSnapshotReference = toTagCompound()
    }

    fun clearTracking() {
        baseSnapshotReference = null
    }

    fun getPatchOps(): PdcPatch {
        val base = baseSnapshotReference
        val curr = toTagCompound()
        val ops = diffToOps(base, curr)

        return PdcPatch(ops)
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

    override fun snapshot(): TrackingPlayerPersistentDataContainerImpl {
        return TrackingPlayerPersistentDataContainerImpl(snapshotTag().fast())
    }
}