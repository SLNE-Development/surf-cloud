package dev.slne.surf.cloud.bukkit;

import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.NestedRuntimeException;

public final class BukkitMain extends JavaPlugin {

  @Override
  public void onLoad() {
    try {
      SurfCloudBukkitInstance.get().onLoad();
    } catch (Throwable t) {
      handleThrowable(t);
    }
  }

  @Override
  public void onEnable() {
    try {
      SurfCloudBukkitInstance.get().onEnable();
    } catch (Throwable t) {
      handleThrowable(t);
    }
  }

  @Override
  public void onDisable() {
    try {
      SurfCloudBukkitInstance.get().onDisable();
    } catch (Throwable t) {
      handleThrowable(t);
    }
  }

  ClassLoader getClassLoader0() {
    return getClassLoader();
  }

  public static @NotNull BukkitMain getInstance() {
    return getPlugin(BukkitMain.class);
  }

  private void handleThrowable(Throwable t) {
    if (t instanceof FatalSurfError fatalError) {
      handleFatalError(fatalError);
    } else if (t instanceof NestedRuntimeException nested
        && nested.getRootCause() instanceof FatalSurfError fatalSurfError) {
      handleFatalError(fatalSurfError);
    } else {
      getComponentLogger().error("An unexpected error occurred", t);
    }
  }

  @SuppressWarnings("CallToPrintStackTrace")
  private void handleFatalError(FatalSurfError fatalError) {
    final ComponentLogger logger = getComponentLogger();
    logger.error("A fatal error occurred: ");
    logger.error(fatalError.buildMessage());
    fatalError.printStackTrace();
    Bukkit.shutdown();
  }
}
