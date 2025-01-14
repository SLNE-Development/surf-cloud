package dev.slne.surf.cloud.standalone.plugin.provider.source

import dev.slne.surf.cloud.standalone.plugin.entrypoint.EntrypointHandler

interface ProviderSource<I, C> {
    fun prepareContext(context: I): C
    fun registerProviders(entrypointHandler: EntrypointHandler, context: C)
}