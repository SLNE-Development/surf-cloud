package dev.slne.surf.data.bukkit;

import com.google.auto.service.AutoService;
import dev.slne.surf.data.api.SurfDataInstance;
import dev.slne.surf.data.core.SurfDataCoreInstance;
import java.nio.file.Path;

@AutoService(SurfDataInstance.class)
public final class SurfDataBukkitInstance extends SurfDataCoreInstance {

  @Override
  public Path getDataFolder() {
    return BukkitMain.getInstance().getDataPath();
  }

  @Override
  public ClassLoader getClassLoader() {
    return BukkitMain.getInstance().getClassLoader0();
  }
}
