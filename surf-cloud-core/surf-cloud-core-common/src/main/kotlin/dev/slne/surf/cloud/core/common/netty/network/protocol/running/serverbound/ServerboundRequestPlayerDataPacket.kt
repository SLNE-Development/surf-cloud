package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readEnum
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound.ServerboundRequestPlayerDataResponse.*
import dev.slne.surf.cloud.core.common.player.playtime.PlaytimeImpl
import net.kyori.adventure.text.Component
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration
import dev.slne.surf.cloud.api.common.player.name.NameHistory as ApiNameHistory
import dev.slne.surf.cloud.api.common.player.playtime.Playtime as ApiPlaytime

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

    enum class DataRequestType(val reader: StreamDecoder<SurfByteBuf, ServerboundRequestPlayerDataResponse.DataResponse>) {
        LATEST_IP_ADDRESS(ServerboundRequestPlayerDataResponse::IpAddress) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.IpAddress(player.latestIpAddress())
            }
        },
        LAST_SERVER(ServerboundRequestPlayerDataResponse::LastServer) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.LastServer(player.lastServerRaw())
            }
        },
        LAST_SEEN(ServerboundRequestPlayerDataResponse::LastSeen) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.LastSeen(player.lastSeen())
            }
        },
        FIRST_SEEN(ServerboundRequestPlayerDataResponse::FirstSeen) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.FirstSeen(player.firstSeen())
            }
        },
        DISPLAY_NAME(ServerboundRequestPlayerDataResponse::DisplayName) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.DisplayName(player.displayName())
            }
        },
        NAME(ServerboundRequestPlayerDataResponse::Name) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.Name(player.name())
            }
        },
        NAME_HISTORY(ServerboundRequestPlayerDataResponse::NameHistory) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.NameHistory(player.nameHistory())
            }
        },
        PLAYTIME(ServerboundRequestPlayerDataResponse::Playtime) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                return ServerboundRequestPlayerDataResponse.Playtime(player.playtime())
            }
        },
        IS_AFK(ServerboundRequestPlayerDataResponse::IsAFK) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                val player = player.player ?: error("Player is not online")
                return ServerboundRequestPlayerDataResponse.IsAFK(player.isAfk())
            }
        },
        PLAYTIME_SESSION(ServerboundRequestPlayerDataResponse::PlaytimeSession) {
            override suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse {
                val player = player.player ?: error("Player is not online")
                return ServerboundRequestPlayerDataResponse.PlaytimeSession(player.currentSessionDuration())
            }
        };

        abstract suspend fun readData(player: OfflineCloudPlayer): ServerboundRequestPlayerDataResponse.DataResponse
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
        buf.readEnum<ServerboundRequestPlayerDataPacket.DataRequestType>().reader.decode(buf)
    )

    abstract class DataResponse(val type: ServerboundRequestPlayerDataPacket.DataRequestType) {
        abstract fun write(buf: SurfByteBuf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeEnum(data.type)
        data.write(buf)
    }


    class IpAddress(val ip: Inet4Address?) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.LATEST_IP_ADDRESS) {
        constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readInet4Address() })

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(ip) { buf, ip -> buf.writeInet4Address(ip) }
        }
    }

    class LastServer(val server: String?) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.LAST_SERVER) {
        constructor(buf: SurfByteBuf) : this(buf.readNullableString())

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(server)
        }
    }

    class LastSeen(val lastSeen: ZonedDateTime?) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.LAST_SEEN) {
        constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readZonedDateTime() })

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(lastSeen) { buf, dateTime -> buf.writeZonedDateTime(dateTime) }
        }
    }

    class FirstSeen(val firstSeen: ZonedDateTime?) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.FIRST_SEEN) {
        constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readZonedDateTime() })

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(firstSeen) { buf, dateTime -> buf.writeZonedDateTime(dateTime) }
        }
    }

    class DisplayName(val displayName: Component?) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.DISPLAY_NAME) {
        constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readComponent() })

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(displayName) { buf, name -> buf.writeComponent(name) }
        }
    }

    class Name(val name: String?) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.NAME) {
        constructor(buf: SurfByteBuf) : this(buf.readNullableString())

        override fun write(buf: SurfByteBuf) {
            buf.writeNullable(name)
        }
    }

    class NameHistory(val history: ApiNameHistory) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.NAME_HISTORY) {
        constructor(buf: SurfByteBuf) : this(ApiNameHistory.readFromByteBuf(buf))

        override fun write(buf: SurfByteBuf) {
            history.writeToByteBuf(buf)
        }
    }

    class Playtime(val playtime: ApiPlaytime) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.PLAYTIME) {
        constructor(buf: SurfByteBuf) : this(PlaytimeImpl.readFromByteBuf(buf))

        override fun write(buf: SurfByteBuf) {
            playtime.writeToByteBuf(buf)
        }
    }

    class IsAFK(val isAfk: Boolean) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.IS_AFK) {
        constructor(buf: SurfByteBuf) : this(buf.readBoolean())

        override fun write(buf: SurfByteBuf) {
            buf.writeBoolean(isAfk)
        }
    }

    class PlaytimeSession(val playtime: Duration) :
        DataResponse(ServerboundRequestPlayerDataPacket.DataRequestType.PLAYTIME_SESSION) {
        constructor(buf: SurfByteBuf) : this(buf.readDuration())

        override fun write(buf: SurfByteBuf) {
            buf.writeDuration(playtime)
        }
    }
}

inline fun <reified T> ServerboundRequestPlayerDataResponse.DataResponse.getGenericValue(): T =
    when (this) {
        is ServerboundRequestPlayerDataResponse.IpAddress -> check(T::class == Inet4Address::class) { "Expected Inet4Address" }.let { ip as T }
        is ServerboundRequestPlayerDataResponse.LastServer -> check(T::class == String::class) { "Expected String" }.let { server as T }
        is ServerboundRequestPlayerDataResponse.LastSeen -> check(T::class == ZonedDateTime::class) { "Expected ZonedDateTime" }.let { lastSeen as T }
        is ServerboundRequestPlayerDataResponse.FirstSeen -> check(T::class == ZonedDateTime::class) { "Expected ZonedDateTime" }.let { firstSeen as T }
        is ServerboundRequestPlayerDataResponse.DisplayName -> check(T::class == Component::class) { "Expected Component" }.let { displayName as T }
        is ServerboundRequestPlayerDataResponse.Name -> check(T::class == String::class) { "Expected String" }.let { name as T }
        is ServerboundRequestPlayerDataResponse.NameHistory -> check(T::class == ApiNameHistory::class) { "Expected ApiNameHistory" }.let { history as T }
        is ServerboundRequestPlayerDataResponse.Playtime -> check(T::class == ApiPlaytime::class) { "Expected ApiPlaytime" }.let { playtime as T }
        is ServerboundRequestPlayerDataResponse.IsAFK -> check(T::class == Boolean::class) { "Expected Boolean" }.let { isAfk as T }
        is ServerboundRequestPlayerDataResponse.PlaytimeSession -> check(T::class == Duration::class) { "Expected Duration" }.let { playtime as T }
        else -> error("Unknown DataResponse type: ${this::class.simpleName}")
    }