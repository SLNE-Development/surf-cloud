package dev.slne.surf.cloudtest.standalone.test.redis

import dev.slne.surf.cloud.api.server.redis.RedisEvent
import dev.slne.surf.cloud.api.server.redis.RedisEventMeta
import kotlinx.serialization.Serializable

@RedisEventMeta("test:test_event")
@Serializable
data class TestRedisEvent(
    val message: String,
    val number: Int,
    val flag: Boolean,
    val optional: String? = null,
    val list: List<String> = emptyList(),
    val map: Map<String, Int> = emptyMap(),
) : RedisEvent() {

    companion object {
        fun createSample(): TestRedisEvent {
            return TestRedisEvent(
                message = "Hello, Redis!",
                number = 42,
                flag = true,
                optional = "OptionalValue",
                list = listOf("one", "two", "three"),
                map = mapOf("key1" to 1, "key2" to 2)
            )
        }
    }
}