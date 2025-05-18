package dev.slne.surf.cloud.api.server.plugin.utils

import dev.slne.surf.surfapi.core.api.reflection.Field
import dev.slne.surf.surfapi.core.api.reflection.SurfProxy
import dev.slne.surf.surfapi.core.api.reflection.createProxy
import dev.slne.surf.surfapi.core.api.reflection.surfReflection
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database

internal object PluginUtilProxies {
    val springTransactionManagerProxy =
        surfReflection.createProxy<SpringTransactionManagerProxy>()

    @SurfProxy(SpringTransactionManager::class)
    internal interface SpringTransactionManagerProxy {
        @Field(name = "_database", type = Field.Type.GETTER)
        fun getCurrentDatabase(transactionManager: SpringTransactionManager): Database
    }
}