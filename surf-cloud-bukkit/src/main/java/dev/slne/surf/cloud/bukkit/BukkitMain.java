package dev.slne.surf.cloud.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class BukkitMain extends JavaPlugin {

  @Override
  public void onLoad() {
    SurfCloudBukkitInstance.get().onLoad();
  }

  @Override
  public void onEnable() {
    SurfCloudBukkitInstance.get().onEnable();
  }

  @Override
  public void onDisable() {
    SurfCloudBukkitInstance.get().onDisable();
  }

  ClassLoader getClassLoader0() {
    return getClassLoader();
  }

  public static @NotNull BukkitMain getInstance() {
    return getPlugin(BukkitMain.class);
  }
}
