package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.CloudBufSerializer
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import net.kyori.adventure.text.Component

typealias SerializableComponent = @Serializable(with = AdventureComponentSerializer::class) Component

object AdventureComponentSerializer : CloudBufSerializer<Component>() {
    override val descriptor = PrimitiveSerialDescriptor("Component", PrimitiveKind.STRING)

    override fun serialize0(
        buf: SurfByteBuf,
        value: Component
    ) {
        buf.writeComponent(value)
    }

    override fun deserialize0(buf: SurfByteBuf): Component {
        return buf.readComponent()
    }
}