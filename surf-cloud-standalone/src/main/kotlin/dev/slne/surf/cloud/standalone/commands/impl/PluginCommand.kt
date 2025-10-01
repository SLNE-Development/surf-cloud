package dev.slne.surf.cloud.standalone.commands.impl

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.server.command.AbstractConsoleCommand
import dev.slne.surf.cloud.api.server.command.CommandSource
import dev.slne.surf.cloud.api.server.command.ConsoleCommand
import dev.slne.surf.cloud.api.server.command.literal
import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.LaunchEntryPointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.ProviderStatus
import dev.slne.surf.cloud.standalone.plugin.provider.ProviderStatusHolder
import dev.slne.surf.cloud.standalone.plugin.provider.impl.StandalonePluginParent
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.lang.reflect.Type
import java.util.*

@ConsoleCommand
class PluginCommand : AbstractConsoleCommand() {

    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        val root = dispatcher.register(literal<CommandSource>("plugins") {
            executes { ctx ->
                val plugins =
                    TreeMap<String, PluginProvider<StandalonePlugin>>(String.CASE_INSENSITIVE_ORDER)
                val providers = LaunchEntryPointHandler.get(Entrypoint.SPRING_PLUGIN)
                    ?.getRegisteredProviders()
                    ?: error("No storage for entrypoint ${Entrypoint.SPRING_PLUGIN}")

                for (provider in providers) {
                    val meta = provider.meta
                    plugins.put(meta.displayName, provider)
                }

                val infoMessage = buildText {
                    info("ℹ ")
                    text("Plugins (${plugins.size}): ", NamedTextColor.WHITE)
                    append(
                        Component.join(
                            JoinConfiguration.newlines(),
                            formatProviders(plugins)
                        )
                    )
                }

                ctx.source.sendMessage(infoMessage)
                Command.SINGLE_SUCCESS
            }
        })

        dispatcher.register(literal("pl") { redirect(root) })
    }

    private fun <T> formatProviders(plugins: TreeMap<String, PluginProvider<T>>): List<Component> {
        val components = mutableObjectListOf<Component>(plugins.size)
        for (entry in plugins.values) {
            components.add(formatProvider(entry))
        }

        var isFirst = true
        val formattedSubLists = mutableObjectListOf<Component>()

        for (componentSublist in components.chunked(10)) {
            var component = Component.space()
            if (isFirst) {
                component =
                    component.append(text("— ", NamedTextColor.DARK_GRAY))
                isFirst = false
            } else {
                component = Component.space()
            }

            formattedSubLists.add(
                component.append(
                    Component.join(
                        JoinConfiguration.commas(true),
                        componentSublist
                    )
                )
            )
        }

        return formattedSubLists
    }


    private fun formatProvider(provider: PluginProvider<*>): Component {
        return text(provider.meta.name, fromStatus(provider))
    }

    private fun fromStatus(provider: PluginProvider<*>): TextColor {
        when (provider) {
            is ProviderStatusHolder -> {
                val status = provider.status

                if (status == ProviderStatus.INITIALIZED && GenericTypeReflector.isSuperType(
                        STANDALONE_PLUGIN_PROVIDER_TYPE,
                        provider.javaClass
                    )
                ) {
                    val plugin = PluginManager.instance.getPlugin(provider.meta.name)
                    if (plugin == null) {
                        return NamedTextColor.RED
                    }

                    return if (plugin.enabled) NamedTextColor.GREEN else NamedTextColor.RED
                }

                return when (status) {
                    ProviderStatus.INITIALIZED -> NamedTextColor.GREEN
                    ProviderStatus.ERRORED -> NamedTextColor.RED
                    ProviderStatus.UNINITIALIZED -> NamedTextColor.YELLOW
                }
            }

            is StandalonePluginParent.StandalonePluginProvider if provider.shouldSkipCreation() -> {
                return NamedTextColor.RED
            }

            else -> {
                return NamedTextColor.RED
            }
        }
    }

    companion object {
        private val STANDALONE_PLUGIN_PROVIDER_TYPE: Type =
            object : TypeToken<PluginProvider<StandalonePlugin>>() {}.type
    }
}