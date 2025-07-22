package dev.slne.surf.cloud.standalone.persistent

import dev.slne.surf.cloud.core.common.data.persistentData
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.LongBinaryTag

object StandalonePersistentData {
    val SERVER_ID_COUNTER =
        persistentData(
            "server_id_counter",
            BinaryTagTypes.LONG,
            { value() },
            { LongBinaryTag.longBinaryTag(it) },
            1L
        ).nonNull()
}