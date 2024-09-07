package dev.slne.surf.data.core;

import static com.google.common.base.Preconditions.checkNotNull;

import dev.slne.surf.data.SurfDataApplication;
import dev.slne.surf.data.api.SurfDataInstance;
import dev.slne.surf.data.api.redis.RedisEvent;
import dev.slne.surf.data.api.util.JoinClassLoader;
import dev.slne.surf.data.core.spring.SurfSpringBanner;
import dev.slne.surf.data.core.util.Util;
import java.nio.file.Path;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.util.Logger;
import reactor.util.Loggers;

@Getter
@ParametersAreNonnullByDefault
public abstract class SurfDataCoreInstance implements SurfDataInstance {

  private static final Logger redisEventLog = Loggers.getLogger("RedisEvent");

  private ConfigurableApplicationContext dataContext;

  @OverridingMethodsMustInvokeSuper
  public void onLoad() {
    Util.tempChangeSystemClassLoader(getClassLoader(), () -> {
      dataContext = startSpringApplication(SurfDataApplication.class);
    });
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

    final SpringApplicationBuilder builder = new SpringApplicationBuilder(applicationClass)
        .resourceLoader(new DefaultResourceLoader(joinClassLoader))
        .bannerMode(Mode.CONSOLE)
        .banner(new SurfSpringBanner());

    if (dataContext != null) {
      builder.parent(dataContext);
    }

    return builder.run();
  }

  @Override
  public void callRedisEvent(RedisEvent event) {
    checkNotNull(event, "event");

    final ReactiveRedisTemplate<String, Object> template = dataContext.getBean(
        ReactiveRedisTemplate.class);
    for (final String channel : event.getChannels()) {
      template.convertAndSend(channel, event).log(redisEventLog).subscribe();
    }
  }

  public abstract Path getDataFolder();

  public static SurfDataCoreInstance get() {
    return (SurfDataCoreInstance) SurfDataInstance.get();
  }
}
