package dev.slne.surf.cloud.standalone.plugin.provider.configuration.serializer

import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

class ComponentSerializer : ScalarSerializer<Component>(Component::class.java) {
    override fun deserialize(
        type: Type?,
        obj: Any
    ): Component? {
        return MiniMessage.miniMessage().deserialize(obj.toString())
    }

    override fun serialize(
        item: Component,
        typeSupported: Predicate<Class<*>?>?
    ): Any? {
        return MiniMessage.miniMessage().serialize(item)
    }
}