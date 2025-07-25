package dev.slne.surf.cloud.api.common.config.auto

import net.kyori.adventure.text.minimessage.MiniMessage
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(MiniMessage::class)
class AdventureAutoConfiguration {
    @Bean
    fun miniMessage(): MiniMessage {
        return MiniMessage.miniMessage()
    }
}
