package dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx

import dev.slne.surf.bytebufserializer.Buf
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure.AdventureComponentSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure.AdventureKeySerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.adventure.AdventureSoundSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.BitSetSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.Inet4AddressSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.InetSocketAddressSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.URISerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.UUIDSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.UtfStringSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.ZonedDateTimeSerializer
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.nbt.CompoundTagSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

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

        // NBT
        contextual(CompoundTagSerializer)
    }

    val serializer = Buf(serializerModule)
}