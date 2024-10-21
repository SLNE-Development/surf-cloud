package dev.slne.surf.cloud.standalone.redis

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.slne.surf.cloud.standalone.independentCloudInstance
import lombok.AccessLevel
import lombok.NoArgsConstructor
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.*

@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class RedisEvent {

    @get:JsonIgnore
    val eventName: String by lazy { this.javaClass.simpleName }

    @get:JsonIgnore
    lateinit var channels: Array<String>
        private set

    @get:JsonIgnore
    val channelList: List<String> by lazy { channels.toList() }

    lateinit var packetId: UUID
        private set

    internal constructor()
    constructor(firstChannel: String, vararg channels: String) {
        this.channels = ArrayUtils.add(channels, firstChannel)
        this.packetId = UUID.randomUUID()
    }

    fun send() {
        independentCloudInstance.callRedisEvent(this)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
