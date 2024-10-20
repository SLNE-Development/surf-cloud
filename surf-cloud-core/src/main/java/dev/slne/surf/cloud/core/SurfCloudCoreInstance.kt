package dev.slne.surf.cloud.core;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.cloud.SurfCloudMainApplication;
import dev.slne.surf.cloud.api.SurfCloudInstance;
import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import dev.slne.surf.cloud.api.exceptions.FatalSurfError.ExitCodes;
import dev.slne.surf.cloud.api.util.JoinClassLoader;
import dev.slne.surf.cloud.core.spring.SurfSpringBanner;
import dev.slne.surf.cloud.core.util.Util;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Getter;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.DefaultResourceLoader;

@Getter
@ParametersAreNonnullByDefault
@Flogger
public abstract class SurfCloudCoreInstance implements SurfCloudInstance {

  private volatile ConfigurableApplicationContext dataContext;

  public SurfCloudCoreInstance() throws IllegalAccessException {
    final Class<?> caller = Util.getCallerClass();
    if (!caller.getName().startsWith("java.util.ServiceLoader")) {
      throw new IllegalAccessException("Cannot instantiate instance directly");
    }
  }

  @OverridingMethodsMustInvokeSuper
  public void onLoad() {

    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      log.atSevere()
          .withCause(e)
          .log(
              """
              An uncaught exception occurred in thread %s
              Exception type: %s
              Exception message: %s
              """,
              t.getName(), e.getClass().getName(), e.getMessage()
          );
    });

    try {
      dataContext = startSpringApplication(SurfCloudMainApplication.class);
    } catch (Throwable e) {
      if (e instanceof FatalSurfError fatal) {
        // Re-throw FatalSurfError directly
        throw fatal;
      } else if (e instanceof NestedRuntimeException nested
          && nested.getRootCause() instanceof FatalSurfError fatal) {
        // Re-throw FatalSurfError if it is wrapped inside NestedRuntimeException
        throw fatal;
      } else {
        // Build and throw a new FatalSurfError for any other unexpected errors
        throw FatalSurfError.builder()
            .simpleErrorMessage("An unexpected error occurred during the onLoad process.")
            .detailedErrorMessage(
                "An error occurred while starting the Spring application during the onLoad phase.")
            .cause(e)
            .additionalInformation("Error occurred in: " + this.getClass().getName())
            .additionalInformation(
                "Root cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown"))
            .additionalInformation("Exception type: " + e.getClass().getName())
            .possibleSolution("Check the logs for more detailed error information.")
            .possibleSolution("Ensure that the application configurations are correct.")
            .possibleSolution(
                "Make sure that all dependencies are correctly initialized before loading.")
            .exitCode(ExitCodes.UNKNOWN_ERROR)
            .build();
      }
    }
  }

  @OverridingMethodsMustInvokeSuper
  public void onEnable() {
  }

  @OverridingMethodsMustInvokeSuper
  public void onDisable() {
    if (dataContext != null && dataContext.isActive()) {
      dataContext.close();
    }
  }

  @Override
  public ConfigurableApplicationContext startSpringApplication(
      Class<?> applicationClass,
      ClassLoader classLoader,
      ClassLoader... parentClassLoader
  ) {
    checkNotNull(applicationClass, "applicationClass");
    checkNotNull(classLoader, "classLoader");
    checkNotNull(parentClassLoader, "parentClassLoader");

    final JoinClassLoader joinClassLoader = new JoinClassLoader(classLoader,
        ArrayUtils.addFirst(parentClassLoader, classLoader));

    final Supplier<ConfigurableApplicationContext> run = () -> {
      final SpringApplicationBuilder builder = new SpringApplicationBuilder(applicationClass)
          .resourceLoader(new DefaultResourceLoader(joinClassLoader))
          .bannerMode(Mode.CONSOLE)
          .banner(new SurfSpringBanner())
          .profiles(getSpringProfile())
          .listeners();

      if (dataContext != null) {
        builder.parent(dataContext);
      }

      return builder.run();
    };

    return Util.tempChangeSystemClassLoader(joinClassLoader, run);
  }

  public abstract Path getDataFolder();

  protected String getSpringProfile() {
    return "client";
  }

  public static SurfCloudCoreInstance get() {
    return (SurfCloudCoreInstance) SurfCloudInstance.get();
  }
}
