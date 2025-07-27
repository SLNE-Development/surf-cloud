package dev.slne.surf.cloud.standalone.plugin.provider.configuration.serializer

import dev.slne.surf.surfapi.core.api.util.logger
import io.leangen.geantyref.GenericTypeReflector
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.util.EnumLookup
import java.lang.reflect.Type
import java.util.function.Predicate

class EnumValueSerializer : ScalarSerializer<Enum<*>>(object : TypeToken<Enum<*>>() {}) {
    private val log = logger()

    override fun deserialize(type: Type, obj: Any): Enum<*>? {
        val enumConstant = obj.toString()
        val typeClass = GenericTypeReflector.erase(type).asSubclass(Enum::class.java)
        val ret = EnumLookup.lookupEnum(typeClass, enumConstant)
            ?: EnumLookup.lookupEnum(typeClass, enumConstant.replace('-', '_'))

        if (ret == null) {
            val longer = typeClass.enumConstants.size > 10
            val optionSample = typeClass.enumConstants.take(10).map { it.name }
            log.atSevere()
                .log(
                    "Failed to deserialize enum value '$enumConstant' for type $typeClass. " +
                            "Available options: ${optionSample.joinToString()}" +
                            if (longer) "..." else ""
                )
        }

        return ret
    }

    override fun serialize(item: Enum<*>, typeSupported: Predicate<Class<*>?>?): Any? {
        return item.name
    }
}