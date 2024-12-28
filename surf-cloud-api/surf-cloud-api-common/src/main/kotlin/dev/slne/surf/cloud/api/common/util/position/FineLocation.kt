package dev.slne.surf.cloud.api.common.util.position

import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*
import kotlin.math.sqrt

data class FineLocation(
    val world: UUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0.0f,
    val pitch: Float = 0.0f
) {

    companion object {
        val STREAM_CODEC = streamCodec<SurfByteBuf, FineLocation>({ buf, value ->
            buf.writeUuid(value.world)
            buf.writeDouble(value.x)
            buf.writeDouble(value.y)
            buf.writeDouble(value.z)
            buf.writeFloat(value.yaw)
            buf.writeFloat(value.pitch)
        }, { buf ->
            FineLocation(
                buf.readUuid(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat()
            )
        })
    }

    fun distanceSquared(other: FineLocation): Double { // TODO: Replace with fast math
        if (world != other.world) error("Cannot calculate distance between locations in different worlds")

        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z

        return dx * dx + dy * dy + dz * dz
    }

    fun distance(other: FineLocation): Double {
        return sqrt(distanceSquared(other))
    }
}

fun fineLocation(
    world: UUID,
    x: Double,
    y: Double,
    z: Double,
    yaw: Float = 0.0f,
    pitch: Float = 0.0f
) = FineLocation(world, x, y, z, yaw, pitch)