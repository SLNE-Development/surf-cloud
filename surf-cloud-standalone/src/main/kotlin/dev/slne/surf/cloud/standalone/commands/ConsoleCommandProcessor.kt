package dev.slne.surf.cloud.standalone.commands

import dev.slne.surf.cloud.api.server.command.AbstractConsoleCommand
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class ConsoleCommandProcessor(private val manager: CommandManagerImpl) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is AbstractConsoleCommand) {
            bean.register(manager.dispatcher)
        }
        return bean
    }
}