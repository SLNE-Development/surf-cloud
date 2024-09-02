package dev.slne.surf.data.bukkit.processor;

import dev.slne.surf.data.api.lifecycle.SurfLifecycle;
import dev.slne.surf.data.core.processors.AbstractLifecycleProcessor;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.stereotype.Component;

@Component
public class BukkitLifecycleProcessor extends AbstractLifecycleProcessor {

  @Override
  protected Class<?> getProvidingClass(SurfLifecycle lifecycle) {
    return JavaPlugin.getProvidingPlugin(lifecycle.getClass()).getClass();
  }
}
