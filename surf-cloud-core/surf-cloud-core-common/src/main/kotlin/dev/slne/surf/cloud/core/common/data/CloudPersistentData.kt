package dev.slne.surf.cloud.core.common.data

import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.LongBinaryTag


object CloudPersistentData {
    const val SERVER_ID_NOT_SET = -1L

    var SERVER_ID by persistentData(
        "server_id",
        BinaryTagTypes.LONG,
        { value() },
        { LongBinaryTag.longBinaryTag(it) },
        SERVER_ID_NOT_SET
    ).nonNull()
}
