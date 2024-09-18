package dev.slne.surf.cloud.standalone.plugin;

import dev.slne.surf.cloud.standalone.launcher.Launcher;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import jakarta.annotation.PreDestroy;
import java.nio.file.Path;
import lombok.extern.flogger.Flogger;
import org.springframework.stereotype.Component;

@Component
@Flogger
public class StandalonePluginManager {

  public static final Path PLUGIN_DIRECTORY = Launcher.PLUGIN_DIRECTORY;
  private final ObjectSet<StandalonePlugin> plugins = ObjectSets.synchronize(new ObjectOpenHashSet<>());

  void addPlugin(StandalonePlugin plugin) {
    plugins.add(plugin);
  }

  @PreDestroy
  protected void stopPlugins() {
    for (final StandalonePlugin plugin : plugins) {
      try {
        plugin.stop();
      } catch (Exception e) {
        log.atSevere()
            .withCause(e)
            .log("Failed to stop plugin '%s'", plugin.id());
      }
    }
  }
}
