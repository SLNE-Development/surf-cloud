package dev.slne.surf.cloud.standalone.plugin.provider.configuration.type

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class DependencyConfiguration(
    val load: LoadOrder,
    val required: Boolean = true,
    val joinClasspath: Boolean = true
) {

    @ConfigSerializable
    enum class LoadOrder {
        BEFORE,
        AFTER,
        OMIT
    }
}