package dev.slne.surf.data.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Plugin(
    id = "surf-data-velocity",
    name = "Surf Data Velocity",
    version = "1.21.1-1.0.0-SNAPSHOT",
    description = "A data plugin for Velocity",
    authors = {"twisti"}
)
@Getter
public class VelocityMain {

  @Getter
  private static VelocityMain instance;
  private final ProxyServer server;
  private final PluginManager pluginManager;
  private final EventManager eventManager;
  private final Path dataPath;

  @Inject
  public VelocityMain(
      @NotNull ProxyServer server,
      @NotNull PluginManager pluginManager,
      @NotNull EventManager eventManager,
      @DataDirectory @NotNull Path dataPath
  ) {
    this.server = server;
    this.pluginManager = pluginManager;
    this.eventManager = eventManager;
    this.dataPath = dataPath;
    instance = this;

    SurfDataVelocityInstance.get().onLoad();
    eventManager.register(this, this);
  }

  @Subscribe
  public void onProxyInitialize(ProxyInitializeEvent ignored) {
    SurfDataVelocityInstance.get().onEnable();
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent ignored) {
    SurfDataVelocityInstance.get().onDisable();
  }
}
