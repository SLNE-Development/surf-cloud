package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx

import dev.slne.surf.bytebufserializer.Buf
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure.AdventureComponentSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure.AdventureKeySerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure.AdventureSoundSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.cloud.CloudPlayerSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.cloud.OfflineCloudPlayerSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.*
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.kotlin.DurationSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.nbt.CompoundTagSerializer
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

@InternalApi
object SurfCloudBufSerializer {
    val serializerModule = SerializersModule {
        // Adventure
        contextual(AdventureKeySerializer)
        contextual(AdventureSoundSerializer)
        contextual(AdventureComponentSerializer)

        // Java
        contextual(UUIDSerializer)
        contextual(BitSetSerializer)
        contextual(UtfStringSerializer)
        contextual(URISerializer)
        contextual(InetSocketAddressSerializer)
        contextual(ZonedDateTimeSerializer)
        contextual(Inet4AddressSerializer)

        // Kotlin
        contextual(DurationSerializer)

        // NBT
        contextual(CompoundTagSerializer)

        // Cloud
        contextual(CloudPlayerSerializer)
        contextual(OfflineCloudPlayerSerializer)
    }

    val serializer = Buf(serializerModule)
}