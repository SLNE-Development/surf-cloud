package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.standalone.redis.RedisEvent


class TestPacket : RedisEvent {
    var entity: TestEntity? = null
        private set

    constructor()

    constructor(entity: TestEntity?) : super(CHANNEL) {
        this.entity = entity
    }

    companion object {
        const val CHANNEL: String = "surf:test:channel"
    }
}
