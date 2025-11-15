package dev.slne.surf.cloud.api.common.player.teleport

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.SerializableUUID
import kotlinx.serialization.Serializable
import org.spongepowered.math.GenericMath
import org.spongepowered.math.vector.Vector3d
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Serializable
data class WorldLocation(
    val world: SerializableUUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0.0f,
    val pitch: Float = 0.0f
) {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            WorldLocation::world,
            ByteBufCodecs.DOUBLE_CODEC,
            WorldLocation::x,
            ByteBufCodecs.DOUBLE_CODEC,
            WorldLocation::y,
            ByteBufCodecs.DOUBLE_CODEC,
            WorldLocation::z,
            ByteBufCodecs.FLOAT_CODEC,
            WorldLocation::yaw,
            ByteBufCodecs.FLOAT_CODEC,
            WorldLocation::pitch,
            ::WorldLocation
        )

        private const val DEG2RAD = PI / 180.0

        @Suppress("NOTHING_TO_INLINE")
        private inline fun toBlockCoord(value: Double) = GenericMath.floor(value)
    }

    val blockX get() = toBlockCoord(x)
    val blockY get() = toBlockCoord(y)
    val blockZ get() = toBlockCoord(z)

    fun direction(): Vector3d {
        val yawRad = yaw * DEG2RAD
        val pitchRad = pitch * DEG2RAD

        val sinPitch = sin(pitchRad)
        val cosPitch = cos(pitchRad)
        val sinYaw = sin(yawRad)
        val cosYaw = cos(yawRad)

        return Vector3d(-cosPitch * sinYaw, -sinPitch, cosPitch * cosYaw)
    }

    operator fun plus(other: WorldLocation): WorldLocation {
        require(world == other.world) { "Cannot add locations from different worlds" }

        return copy(
            x = x + other.x,
            y = y + other.y,
            z = z + other.z
        )
    }

    operator fun plus(other: Vector3d) = copy(
        x = x + other.x(),
        y = y + other.y(),
        z = z + other.z()
    )

    fun add(dx: Double, dy: Double, dz: Double) = copy(
        x = x + dx,
        y = y + dy,
        z = z + dz
    )

    fun addRotation(dYaw: Float, dPitch: Float) = copy(
        yaw = (yaw + dYaw) % 360.0f,
        pitch = (pitch + dPitch).coerceIn(-90.0f, 90.0f)
    )

    operator fun minus(other: WorldLocation): WorldLocation {
        require(world == other.world) { "Cannot subtract locations from different worlds" }

        return copy(
            x = x - other.x,
            y = y - other.y,
            z = z - other.z
        )
    }

    operator fun minus(other: Vector3d) = copy(
        x = x - other.x(),
        y = y - other.y(),
        z = z - other.z()
    )

    fun subtract(dx: Double, dy: Double, dz: Double) = copy(
        x = x - dx,
        y = y - dy,
        z = z - dz
    )

    fun subtractRotation(dYaw: Float, dPitch: Float) = copy(
        yaw = (yaw - dYaw) % 360.0f,
        pitch = (pitch - dPitch).coerceIn(-90.0f, 90.0f)
    )

    operator fun times(factor: Double) = copy(
        x = x * factor,
        y = y * factor,
        z = z * factor
    )

    operator fun div(divisor: Double) = copy(
        x = x / divisor,
        y = y / divisor,
        z = z / divisor
    )

    fun toBlockLocation() = copy(
        x = blockX.toDouble(),
        y = blockY.toDouble(),
        z = blockZ.toDouble()
    )

    fun toCenterLocation() = copy(
        x = blockX + 0.5,
        y = blockY + 0.5,
        z = blockZ + 0.5
    )

    fun distanceSquared(other: WorldLocation): Double { // TODO: Replace with fast math
        if (world != other.world) error("Cannot calculate distance between locations in different worlds")

        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z

        return dx * dx + dy * dy + dz * dz
    }

    fun lengthSquared(): Double {
        return x * x + y * y + z * z
    }

    fun length(): Double {
        return sqrt(lengthSquared())
    }

    fun distance(other: WorldLocation): Double {
        return sqrt(distanceSquared(other))
    }

    fun toVector3d() = Vector3d(x, y, z)
}

fun fineLocation(
    world: UUID,
    x: Double,
    y: Double,
    z: Double,
    yaw: Float = 0.0f,
    pitch: Float = 0.0f
) = WorldLocation(world, x, y, z, yaw, pitch)