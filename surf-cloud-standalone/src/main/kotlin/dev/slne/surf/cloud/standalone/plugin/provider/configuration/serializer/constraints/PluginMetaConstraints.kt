package dev.slne.surf.cloud.standalone.plugin.provider.configuration.serializer.constraints

import org.spongepowered.configurate.objectmapping.meta.Constraint
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

class PluginMetaConstraints {

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class PluginName {
        class Factory : Constraint.Factory<PluginName, String> {
            companion object {
                private val VALID_NAME = Regex("^[A-Za-z\\d _.-]+$")
            }

            override fun make(
                data: PluginName,
                type: Type?
            ) = Constraint<String> { value ->
                if (value != null) {
                    if (!VALID_NAME.matches(value)) {
                        throw SerializationException("name '$value' contains invalid characters")
                    }
                }
            }
        }
    }
}
