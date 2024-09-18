package dev.slne.surf.cloud.standalone.plugin;

import java.nio.file.Path;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.annotation.AnnotationUtils;


public abstract class StandalonePlugin {

  @Delegate
  private final StandalonePluginMeta meta;

  protected StandalonePlugin() {
    this.meta = AnnotationUtils.findAnnotation(AopProxyUtils.ultimateTargetClass(this),
        StandalonePluginMeta.class);

    if (this.meta == null) {
      throw new IllegalStateException("Plugin class must be annotated with @StandalonePluginMeta");
    }
  }

  public abstract void start();

  public abstract void stop();

  public final @NotNull Path getDataFolder() {
    return StandalonePluginManager.PLUGIN_DIRECTORY.resolve(meta.id());
  }
}
