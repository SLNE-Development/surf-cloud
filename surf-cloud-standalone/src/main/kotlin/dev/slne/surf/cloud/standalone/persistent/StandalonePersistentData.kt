package dev.slne.surf.cloud.standalone.persistent

import dev.slne.surf.cloud.core.data.persistentData
import net.querz.nbt.tag.LongTag

object StandalonePersistentData {
    val SERVER_ID_COUNTER =
        persistentData("server_id_counter", { LongTag(it) }, { asLong() }, 1L).nonNull()
}