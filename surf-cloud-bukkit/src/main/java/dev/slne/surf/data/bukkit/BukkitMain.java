package dev.slne.surf.data.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class BukkitMain extends JavaPlugin {

  @Override
  public void onLoad() {
    SurfDataBukkitInstance.get().onLoad();
  }

  @Override
  public void onEnable() {
    SurfDataBukkitInstance.get().onEnable();
  }

  @Override
  public void onDisable() {
    SurfDataBukkitInstance.get().onDisable();
  }

  ClassLoader getClassLoader0() {
    return getClassLoader();
  }

  public static @NotNull BukkitMain getInstance() {
    return getPlugin(BukkitMain.class);
  }
}
