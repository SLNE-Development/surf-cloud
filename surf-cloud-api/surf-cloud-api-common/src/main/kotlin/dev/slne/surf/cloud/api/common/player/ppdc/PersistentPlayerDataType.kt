package dev.slne.surf.cloud.api.common.player.ppdc

import kotlin.reflect.KClass

interface PersistentPlayerDataType<P : Any, C> {
    val primitiveType: KClass<P>

    fun fromPrimitive(primitive: P, context: PersistentPlayerDataAdapterContext): C
    fun toPrimitive(complex: C, context: PersistentPlayerDataAdapterContext): P
}