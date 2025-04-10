package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import net.kyori.adventure.sound.Sound

typealias SerializableSound = @Serializable(with = AdventureSoundSerializer::class) Sound

object AdventureSoundSerializer : CloudBufSerializer<Sound>() {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Sound") {
        element<Sound.Source>("source")
        element<Float>("volume")
        element<Float>("pitch")
        element<Long?>("seed")
        element<String>("name")
    }

    override fun serialize0(
        buf: SurfByteBuf,
        value: Sound
    ) {
        buf.writeSound(value)
    }

    override fun deserialize0(buf: SurfByteBuf): Sound {
        return buf.readSound()
    }
}