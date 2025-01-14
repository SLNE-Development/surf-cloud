package dev.slne.surf.cloud.standalone.plugin.util

import dev.slne.surf.cloud.standalone.plugin.entrypoint.LaunchEntryPointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.source.ProviderSource
import dev.slne.surf.surfapi.core.api.util.logger

object EntrypointUtil {
    private val log = logger()

    fun <I, C> registerProvidersFromSource(source: ProviderSource<I, C>, contextInput: I) {
        try {
            val context = source.prepareContext(contextInput)
            source.registerProviders(LaunchEntryPointHandler, context)
        } catch (e: Throwable) {
            log.atSevere()
                .withCause(e)
                .log(e.message)
        }
    }
}