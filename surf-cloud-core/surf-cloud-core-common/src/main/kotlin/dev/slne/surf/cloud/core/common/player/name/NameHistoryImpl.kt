package dev.slne.surf.cloud.core.common.player.name

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readCollection
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeCollection
import dev.slne.surf.cloud.api.common.player.name.NameEntry
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.api.common.player.name.NameHistoryFactory
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectLists

class NameHistoryImpl(private val entries: ObjectList<NameEntry>) : NameHistory {
    override val nameChanges = entries.size

    override fun names(): ObjectList<NameEntry> = ObjectLists.unmodifiable(entries)
    override fun lastKnownName(): NameEntry? = entries.firstOrNull()
    override fun getNameAtTime(time: Long): NameEntry? = entries.find { it.timestamp <= time }
    override fun wasNameUsed(name: String): Boolean = entries.any { it.name == name }
    override fun firstUsedName(): NameEntry? = entries.lastOrNull()

    override fun timeSinceLastChange(): Long =
        entries.firstOrNull()?.let { System.currentTimeMillis() - it.timestamp } ?: 0

    override fun writeToByteBuf(buf: ByteBuf) {
        buf.writeCollection(entries) { buf, entry -> entry.writeToByteBuf(buf) }
    }
}

@AutoService(NameHistoryFactory::class)
class NameHistoryFactoryImpl : NameHistoryFactory {
    private val emptyNameHistory = NameHistoryImpl(ObjectLists.emptyList())

    override fun create(entries: ObjectList<NameEntry>): NameHistory {
        return NameHistoryImpl(entries)
    }

    override fun createFromByteBuf(buf: ByteBuf): NameHistory {
        val entries =
            buf.readCollection({ mutableObjectListOf(it) }, { NameEntry.readFromByteBuf(it) })
        return NameHistoryImpl(entries)
    }

    override fun empty() = emptyNameHistory
}