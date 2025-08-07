package dev.slne.surf.cloud.api.server.plugin.utils

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.reflection.Field
import dev.slne.surf.surfapi.core.api.reflection.SurfProxy
import dev.slne.surf.surfapi.core.api.reflection.createProxy
import dev.slne.surf.surfapi.core.api.reflection.surfReflection
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database

@InternalApi
object PluginUtilProxies {
    val springTransactionManagerProxy =
        surfReflection.createProxy<SpringTransactionManagerProxy>()

    @InternalApi
    @SurfProxy(SpringTransactionManager::class)
    interface SpringTransactionManagerProxy {
        @Field(name = "_database", type = Field.Type.GETTER)
        fun getCurrentDatabase(transactionManager: SpringTransactionManager): Database
    }
}