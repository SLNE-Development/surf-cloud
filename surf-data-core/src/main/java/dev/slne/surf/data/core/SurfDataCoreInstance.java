package dev.slne.surf.data.core;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.data.SurfDataApplication;
import dev.slne.surf.data.api.SurfDataInstance;
import dev.slne.surf.data.api.util.JoinClassLoader;
import dev.slne.surf.data.core.spring.SurfSpringBanner;
import java.nio.file.Path;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

@ParametersAreNonnullByDefault
public abstract class SurfDataCoreInstance implements SurfDataInstance {

  private ConfigurableApplicationContext dataContext;

  @OverridingMethodsMustInvokeSuper
  public void onLoad() {
    dataContext = SpringApplication.run(SurfDataApplication.class);
  }

  @OverridingMethodsMustInvokeSuper
  public void onEnable() {

  }

  @OverridingMethodsMustInvokeSuper
  public void onDisable() {

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
        ArrayUtils.addFirst(parentClassLoader, getClassLoader()));

    return new SpringApplicationBuilder(applicationClass)
        .resourceLoader(new DefaultResourceLoader(joinClassLoader))
        .bannerMode(Mode.CONSOLE)
        .banner(new SurfSpringBanner())
        .parent(dataContext)
        .run();
  }

  public abstract Path getDataFolder();

  public static SurfDataCoreInstance get() {
    return (SurfDataCoreInstance) SurfDataInstance.get();
  }
}
