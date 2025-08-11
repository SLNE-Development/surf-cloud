package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.sponge

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.spongepowered.math.vector.Vector3i

typealias SerializableVector3i = @Serializable(with = Vector3iSerializer::class) Vector3i

object Vector3iSerializer : KSerializer<Vector3i> {
    override val descriptor = buildClassSerialDescriptor("Vector3i") {
        element<Int>("x")
        element<Int>("y")
        element<Int>("z")
    }

    override fun serialize(
        encoder: Encoder,
        value: Vector3i
    ) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.x())
        encodeIntElement(descriptor, 1, value.y())
        encodeIntElement(descriptor, 2, value.z())
    }


    override fun deserialize(decoder: Decoder): Vector3i = decoder.decodeStructure(descriptor) {
        Vector3i(
            decodeIntElement(descriptor, 0),
            decodeIntElement(descriptor, 1),
            decodeIntElement(descriptor, 2)
        )
    }
}