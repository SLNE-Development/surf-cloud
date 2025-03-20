package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestOfflinePlayerData.RequestType
import net.kyori.adventure.text.Component
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*

@SurfNettyPacket("cloud:serverbound:request_offline_player_data", PacketFlow.SERVERBOUND)
class ServerboundRequestOfflinePlayerData<T>(
    val uuid: UUID,
    val requestType: RequestType<T>
) : RespondingNettyPacket<ResponseRequestOfflinePlayerData<T>>() {
    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundRequestOfflinePlayerData<*>::write
        ) { buf ->
            val uuid = buf.readUuid()
            val requestType = buf.readSingleton() as RequestType<*>
            ServerboundRequestOfflinePlayerData(uuid, requestType)
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeSingleton(requestType)
    }

    @Suppress("ClassName")
    sealed class RequestType<T> {
        object LAST_SERVER : RequestType<String?>()
        object LAST_SEEN : RequestType<ZonedDateTime?>()
        object LATEST_IP_ADDRESS : RequestType<Inet4Address?>()
        object DISPLAY_NAME : RequestType<Component?>()
    }
}

@SurfNettyPacket("cloud:response:request_offline_player_data_response", PacketFlow.CLIENTBOUND)
sealed class ResponseRequestOfflinePlayerData<T>(val value: T) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(
            ResponseRequestOfflinePlayerData<*>::encode,
        ) { buf ->
            val requestType = buf.readSingleton() as RequestType<*>
            decode(buf, requestType)
        }

        fun <T> decode(
            buf: SurfByteBuf,
            requestType: RequestType<T>
        ): ResponseRequestOfflinePlayerData<T> {
            val type = when (requestType) {
                is RequestType.LAST_SERVER -> LastServer(buf.readNullableString())
                is RequestType.LAST_SEEN -> LastSeen(buf.readNullable { it.readZonedDateTime() })
                is RequestType.LATEST_IP_ADDRESS -> LatestIpAddress(buf.readNullable { it.readInet4Address() })
                is RequestType.DISPLAY_NAME -> DisplayName(buf.readNullable { it.readComponent() })
            }

            return type as ResponseRequestOfflinePlayerData<T>
        }
    }

    class LastServer(server: String?) : ResponseRequestOfflinePlayerData<String?>(server)
    class LastSeen(time: ZonedDateTime?) : ResponseRequestOfflinePlayerData<ZonedDateTime?>(time)
    class LatestIpAddress(ip: Inet4Address?) : ResponseRequestOfflinePlayerData<Inet4Address?>(ip)
    class DisplayName(name: Component?) : ResponseRequestOfflinePlayerData<Component?>(name)

    fun encode(buf: SurfByteBuf) {
        when (this) {
            is LastServer -> buf.writeNullable(value)
            is LastSeen -> buf.writeNullable(value) { buf, value -> buf.writeZonedDateTime(value) }
            is LatestIpAddress -> buf.writeNullable(value) { buf, value -> buf.writeInet4Address(value) }
            is DisplayName -> buf.writeNullable(value) { buf, value -> buf.writeComponent(value) }
        }
    }
}