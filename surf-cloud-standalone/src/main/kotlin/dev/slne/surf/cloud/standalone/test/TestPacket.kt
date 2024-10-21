package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.standalone.redis.RedisEvent
import lombok.Getter


class TestPacket : RedisEvent {
    @Getter
    private var entity: TestEntity? = null

    constructor()

    constructor(entity: TestEntity?) : super(CHANNEL) {
        this.entity = entity
    }

    companion object {
        const val CHANNEL: String = "surf:test:channel"
    }
}
