package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.sponge

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.spongepowered.math.vector.Vector3d

typealias SerializableVector3d = @Serializable(with = Vector3iSerializer::class) Vector3d

object Vector3dSerializer : KSerializer<Vector3d> {
    override val descriptor = buildClassSerialDescriptor("Vector3d") {
        element<Double>("x")
        element<Double>("y")
        element<Double>("z")
    }

    override fun serialize(
        encoder: Encoder,
        value: Vector3d
    ) = encoder.encodeStructure(descriptor) {
        encodeDoubleElement(descriptor, 0, value.x())
        encodeDoubleElement(descriptor, 1, value.y())
        encodeDoubleElement(descriptor, 2, value.z())
    }


    override fun deserialize(decoder: Decoder): Vector3d = decoder.decodeStructure(descriptor) {
        Vector3d(
            decodeDoubleElement(descriptor, 0),
            decodeDoubleElement(descriptor, 1),
            decodeDoubleElement(descriptor, 2)
        )
    }
}