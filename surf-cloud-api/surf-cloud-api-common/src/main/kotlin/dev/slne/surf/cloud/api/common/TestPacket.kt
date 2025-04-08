package dev.slne.surf.cloud.api.common

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket("cloud:serverbound:test", PacketFlow.SERVERBOUND)
data class TestPacket(
    val data: String,
    val number: Int,
    val boolean: Boolean,
    val list: List<String>,
    val map: Map<String, String>,
    val nullable: String?,
    val nullableList: List<String>?,
    val nullableMap: Map<String, String>?,
) : NettyPacket() {
    companion object {
        fun random() = TestPacket(
            data = "test",
            number = 42,
            boolean = true,
            list = listOf("one", "two", "three"),
            map = mapOf("key1" to "value1", "key2" to "value2"),
            nullable = null,
            nullableList = null,
            nullableMap = null
        )
    }
}