package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readEnum
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerDataPacket.DataRequestType
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerDataResponse.*
import net.kyori.adventure.text.Component
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*
import dev.slne.surf.cloud.api.common.player.name.NameHistory as ApiNameHistory

@SurfNettyPacket("cloud:request:player_data", PacketFlow.SERVERBOUND)
class ServerboundRequestPlayerDataPacket(val uuid: UUID, val type: DataRequestType) :
    RespondingNettyPacket<ServerboundRequestPlayerDataResponse>() {

    companion object {
        val STREAM_CODEC =
            packetCodec(
                ServerboundRequestPlayerDataPacket::write,
                ::ServerboundRequestPlayerDataPacket
            )
    }

    private constructor(buf: SurfByteBuf) : this(
        buf.readUuid(),
        buf.readEnum()
    )

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeEnum(type)
    }

    enum class DataRequestType(val reader: StreamDecoder<SurfByteBuf, DataResponse>) {
        LATEST_IP_ADDRESS(::IpAddress) {
            override suspend fun readData(player: OfflineCloudPlayer): DataResponse {
                return IpAddress(player.latestIpAddress())
            }
        },
        LAST_SERVER(::LastServer) {
            override suspend fun readData(player: OfflineCloudPlayer): DataResponse {
                return LastServer(player.lastServerRaw())
            }
        },
        LAST_SEEN(::LastSeen) {
            override suspend fun readData(player: OfflineCloudPlayer): DataResponse {
                return LastSeen(player.lastSeen())
            }
        },
        DISPLAY_NAME(::DisplayName) {
            override suspend fun readData(player: OfflineCloudPlayer): DataResponse {
                return DisplayName(player.displayName())
            }
        },
        NAME(::Name) {
            override suspend fun readData(player: OfflineCloudPlayer): DataResponse {
                return Name(player.name())
            }
        },
        NAME_HISTORY(::NameHistory) {
            override suspend fun readData(player: OfflineCloudPlayer): DataResponse {
                return NameHistory(player.nameHistory())
            }
        };

        abstract suspend fun readData(player: OfflineCloudPlayer): DataResponse
    }
}

@SurfNettyPacket("cloud:response:player_data", PacketFlow.CLIENTBOUND)
class ServerboundRequestPlayerDataResponse(val data: DataResponse) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC =
            packetCodec(
                ServerboundRequestPlayerDataResponse::write,
                ::ServerboundRequestPlayerDataResponse
            )
    }

    private constructor(buf: SurfByteBuf) : this(
        buf.readEnum<DataRequestType>().reader.decode(buf)
    )

    abstract class DataResponse(val type: DataRequestType) {
        abstract fun write(buf: SurfByteBuf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeEnum(data.type)
        data.write(buf)
    }


    class IpAddress(val ip: Inet4Address?) : DataResponse(DataRequestType.LATEST_IP_ADDRESS) {
        constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readInet4Address() })

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(ip) { buf, ip -> buf.writeInet4Address(ip) }
        }
    }

    class LastServer(val server: String?) : DataResponse(DataRequestType.LAST_SERVER) {
        constructor(buf: SurfByteBuf) : this(buf.readNullableString())

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(server)
        }
    }

    class LastSeen(val lastSeen: ZonedDateTime?) : DataResponse(DataRequestType.LAST_SEEN) {
        constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readZonedDateTime() })

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(lastSeen) { buf, dateTime -> buf.writeZonedDateTime(dateTime) }
        }
    }

    class DisplayName(val displayName: Component?) : DataResponse(DataRequestType.DISPLAY_NAME) {
        constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readComponent() })

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(displayName) { buf, name -> buf.writeComponent(name) }
        }
    }

    class Name(val name: String?) : DataResponse(DataRequestType.NAME) {
        constructor(buf: SurfByteBuf) : this(buf.readNullableString())

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(name)
        }
    }

    class NameHistory(val history: ApiNameHistory) : DataResponse(DataRequestType.NAME_HISTORY) {
        constructor(buf: SurfByteBuf) : this(ApiNameHistory.readFromByteBuf(buf))

        override fun write(buf: SurfByteBuf) {
            history.writeToByteBuf(buf)
        }
    }
}

inline fun <reified T> DataResponse.getGenericValue(): T = when (this) {
    is IpAddress -> check(T::class == Inet4Address::class) { "Expected Inet4Address" }.let { ip as T }
    is LastServer -> check(T::class == String::class) { "Expected String" }.let { server as T }
    is LastSeen -> check(T::class == ZonedDateTime::class) { "Expected ZonedDateTime" }.let { lastSeen as T }
    is DisplayName -> check(T::class == Component::class) { "Expected Component" }.let { displayName as T }
    is Name -> check(T::class == String::class) { "Expected String" }.let { name as T }
    is NameHistory -> check(T::class == ApiNameHistory::class) { "Expected ApiNameHistory" }.let { history as T }
    else -> error("Unknown DataResponse type: ${this::class.simpleName}")
}