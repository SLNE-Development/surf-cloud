package dev.slne.surf.cloud.bukkit.processor;

import dev.slne.surf.cloud.api.lifecycle.SurfLifecycle;
import dev.slne.surf.cloud.core.processors.AbstractLifecycleProcessor;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.stereotype.Component;

@Component
public class BukkitLifecycleProcessor extends AbstractLifecycleProcessor {

  @Override
  protected Class<?> getProvidingClass(SurfLifecycle lifecycle) {
    return JavaPlugin.getProvidingPlugin(lifecycle.getClass()).getClass();
  }
}
