package dev.slne.surf.cloud.standalone.commands

import dev.slne.surf.cloud.api.server.command.AbstractConsoleCommand
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Role
import org.springframework.stereotype.Component

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
class ConsoleCommandProcessor(private val managerProvider: ObjectProvider<CommandManagerImpl>) :
    BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is AbstractConsoleCommand) {
            bean.register(managerProvider.`object`.dispatcher)
        }
        return bean
    }
}