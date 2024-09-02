package dev.slne.surf.data.api.config.auto;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MiniMessage.class)
public class AdventureAutoConfiguration {

  @Bean
  public MiniMessage miniMessage() {
    return MiniMessage.miniMessage();
  }
}
