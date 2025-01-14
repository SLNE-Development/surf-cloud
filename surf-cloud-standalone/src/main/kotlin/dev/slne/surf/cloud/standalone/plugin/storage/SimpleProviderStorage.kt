package dev.slne.surf.cloud.standalone.plugin.storage

import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.PluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.SimplePluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.PluginGraphCycleException
import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.ProviderLoadingStrategy
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import it.unimi.dsi.fastutil.objects.ObjectList

abstract class SimpleProviderStorage<T>(
    protected val strategy: ProviderLoadingStrategy<T>
) : ProviderStorage<T> {
    protected val providers = mutableObjectListOf<PluginProvider<T>>()
    private val log = logger()

    override fun register(provider: PluginProvider<T>) {
        providers.add(provider)
    }

    override suspend fun enter() {
        val mutableProviders = mutableObjectListOf(providers)
        filterLoadingProviders(mutableProviders)

        try {
            val loadedProvides = strategy.loadProviders(mutableProviders, createDependencyTree())
            for ((provider, provided) in loadedProvides) {
                processProvided(provider, provided)
            }
        } catch (e: PluginGraphCycleException) {
            handleCycle(e)
        }
    }

    override fun createDependencyTree(): PluginMetaDependencyTree = SimplePluginMetaDependencyTree()
    override fun getRegisteredProviders() = providers

    open suspend fun processProvided(provider: PluginProvider<T>, provided: T) {
    }

    protected open fun filterLoadingProviders(provides: ObjectList<PluginProvider<T>>) {
    }

    protected fun handleCycle(exception: PluginGraphCycleException) {
        val logMessages =
            exception.cycles.mapTo(mutableObjectListOf()) { it.joinToString(" -> ") + " -> ${it.first()}" }

        log.atSevere().log("Circular plugin loading detected!")
        log.atSevere().log("Circular load order:")
        logMessages.forEach { log.atSevere().log("  $it") }
        log.atSevere()
            .log("Please report this to the plugin authors of the first plugin of each loop.")

        if (throwOnCycle()) {
            error("Circular plugin loading from plugins ${exception.cycles.joinToString(", ") { it.first() }}")
        }
    }

    open fun throwOnCycle() = true

    override fun toString(): String {
        return "SimpleProviderStorage(providers=$providers, strategy=$strategy)"
    }
}