package dev.slne.surf.cloud.core.common.data

import net.querz.nbt.tag.LongTag


object CloudPersistentData {
    const val SERVER_ID_NOT_SET = -1L

    val SERVER_ID = persistentData(
        "server_id",
        { LongTag(it) },
        { asLong() },
        SERVER_ID_NOT_SET
    ).nonNull()
}
