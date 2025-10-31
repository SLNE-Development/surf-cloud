package dev.slne.surf.cloud.api.common.internal

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.reflection.*
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.BinaryTagType

@InternalApi
@SurfProxy(BinaryTagType::class)
internal interface BinaryTagTypeProxy {

    @Static
    @Field("TYPES", Field.Type.GETTER)
    fun getTypes(): List<BinaryTagType<out BinaryTag>>

    companion object {
        internal val instance = surfReflection.createProxy<BinaryTagTypeProxy>()
    }
}