package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.sponge

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.spongepowered.math.vector.Vector3f

typealias SerializableVector3f = @Serializable(with = Vector3iSerializer::class) Vector3f

object Vector3fSerializer : KSerializer<Vector3f> {
    override val descriptor = buildClassSerialDescriptor("Vector3f") {
        element<Float>("x")
        element<Float>("y")
        element<Float>("z")
    }

    override fun serialize(
        encoder: Encoder,
        value: Vector3f
    ) = encoder.encodeStructure(descriptor) {
        encodeFloatElement(descriptor, 0, value.x())
        encodeFloatElement(descriptor, 1, value.y())
        encodeFloatElement(descriptor, 2, value.z())
    }


    override fun deserialize(decoder: Decoder): Vector3f = decoder.decodeStructure(descriptor) {
        Vector3f(
            decodeFloatElement(descriptor, 0),
            decodeFloatElement(descriptor, 1),
            decodeFloatElement(descriptor, 2)
        )
    }
}