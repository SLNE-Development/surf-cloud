package dev.slne.surf.cloud.standalone.exposed

import dev.slne.surf.cloud.api.server.plugin.utils.PluginUtilProxies
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.stereotype.Component

@Component
class CurrentDbProvider(transactionManager: SpringTransactionManager) {
    val current =
        PluginUtilProxies.springTransactionManagerProxy.getCurrentDatabase(transactionManager)
}