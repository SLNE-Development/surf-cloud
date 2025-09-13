package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import java.util.*

typealias SerializableUUID = @Serializable(with = UUIDSerializer::class) UUID

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = buildClassSerialDescriptor("UUID") {
        element<Long>("mostSignificantBits")
        element<Long>("leastSignificantBits")
    }

    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeStructure(descriptor) {
        encodeLongElement(descriptor, 0, value.mostSignificantBits)
        encodeLongElement(descriptor, 1, value.leastSignificantBits)
    }

    override fun deserialize(decoder: Decoder): UUID = decoder.decodeStructure(descriptor) {
        var mostSigBits = -1L
        var leastSigBits = -1L
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> mostSigBits = decodeLongElement(descriptor, 0)
                1 -> leastSigBits = decodeLongElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }

        require(mostSigBits != -1L && leastSigBits != -1L) { "Missing UUID components: mostSigBits=$mostSigBits, leastSigBits=$leastSigBits" }
        UUID(mostSigBits, leastSigBits)
    }
}