package dev.slne.surf.cloud.standalone;

import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import dev.slne.surf.surfapi.standalone.SurfApiStandaloneBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.NestedRuntimeException;

public class Bootstrap {

  @SuppressWarnings("CallToPrintStackTrace")
  public static void main(String[] args) {
    try {
      System.err.println("Classloader: " + Bootstrap.class.getClassLoader());

      SurfApiStandaloneBootstrap.bootstrap();
      final SurfCloudStandaloneInstance instance = SurfCloudStandaloneInstance.get();
      instance.onLoad();
      instance.onEnable();

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        instance.onDisable();
        SurfApiStandaloneBootstrap.shutdown();
      }));
    } catch (NestedRuntimeException e) {
      System.err.println("Root cause " + e.getRootCause());
      if (e.getRootCause() instanceof FatalSurfError fatalError) {
        handleFatalError(fatalError);
      } else {
        throw e;
      }
    } catch (FatalSurfError error) {
      handleFatalError(error);
    }
  }

  private static void handleFatalError(FatalSurfError error) {
    System.err.println(error.buildMessage());
    final Throwable cause = error.getCause();

    if (cause != null) {
      cause.printStackTrace();
    }

    final ConfigurableApplicationContext context = SurfCloudStandaloneInstance.get().getDataContext();

    if (context != null && context.isActive()) {
      SpringApplication.exit(context, error);
    } else {
      System.exit(error.exitCode());
    }
  }
}