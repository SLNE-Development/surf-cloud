package dev.slne.surf.cloud.standalone.commands;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.stereotype.Component;

@CommandScan
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class __CommandScan {

}
