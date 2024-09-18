package dev.slne.surf.cloud.standalone;

import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import lombok.extern.flogger.Flogger;

@Flogger
public class Bootstrap {

  public static void main(String[] args) {
    try {
      System.err.println("Classloader: " + Bootstrap.class.getClassLoader());

      final SurfCloudStandaloneInstance instance = new SurfCloudStandaloneInstance();
      instance.onLoad();
      instance.onEnable();

      Runtime.getRuntime().addShutdownHook(new Thread(instance::onDisable));
    } catch (FatalSurfError error) {
      log.atSevere()
          .withCause(error)
          .log(error.buildMessage());
      System.exit(error.exitCode());
    }
  }
}