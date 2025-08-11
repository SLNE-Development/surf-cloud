package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.sponge

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.spongepowered.math.vector.Vector3l

typealias SerializableVector3l = @Serializable(with = Vector3lSerializer::class) Vector3l

object Vector3lSerializer : KSerializer<Vector3l> {
    override val descriptor = buildClassSerialDescriptor("Vector3l") {
        element<Long>("x")
        element<Long>("y")
        element<Long>("z")
    }

    override fun serialize(
        encoder: Encoder,
        value: Vector3l
    ) = encoder.encodeStructure(descriptor) {
        encodeLongElement(descriptor, 0, value.x())
        encodeLongElement(descriptor, 1, value.y())
        encodeLongElement(descriptor, 2, value.z())
    }


    override fun deserialize(decoder: Decoder): Vector3l = decoder.decodeStructure(descriptor) {
        Vector3l(
            decodeLongElement(descriptor, 0),
            decodeLongElement(descriptor, 1),
            decodeLongElement(descriptor, 2)
        )
    }
}