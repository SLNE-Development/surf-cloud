package dev.slne.surf.cloud.standalone;

import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import dev.slne.surf.surfapi.standalone.SurfApiStandaloneBootstrap;

public class Bootstrap {

  @SuppressWarnings("CallToPrintStackTrace")
  public static void main(String[] args) {
    try {
      System.err.println("Classloader: " + Bootstrap.class.getClassLoader());

      SurfApiStandaloneBootstrap.bootstrap();
      final SurfCloudStandaloneInstance instance = new SurfCloudStandaloneInstance();
      instance.onLoad();
      instance.onEnable();

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        instance.onDisable();
        SurfApiStandaloneBootstrap.shutdown();
      }));
    } catch (FatalSurfError error) {
      System.err.println(error.buildMessage());
      error.printStackTrace();
      System.exit(error.exitCode());
    }
  }
}