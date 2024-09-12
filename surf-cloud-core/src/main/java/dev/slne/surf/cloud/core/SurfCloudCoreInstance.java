package dev.slne.surf.cloud.core;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import dev.slne.surf.cloud.SurfCloudMainApplication;
import dev.slne.surf.cloud.api.SurfCloudInstance;
import dev.slne.surf.cloud.api.redis.RedisEvent;
import dev.slne.surf.cloud.api.util.JoinClassLoader;
import dev.slne.surf.cloud.core.spring.SurfSpringBanner;
import dev.slne.surf.cloud.core.test.TestEntity;
import dev.slne.surf.cloud.core.test.TestPacket;
import dev.slne.surf.cloud.core.util.Util;
import java.nio.file.Path;
import java.util.UUID;
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
public abstract class SurfCloudCoreInstance implements SurfCloudInstance {

  private static final Logger redisEventLog = Loggers.getLogger("RedisEvent");

  private ConfigurableApplicationContext dataContext;

  @OverridingMethodsMustInvokeSuper
  public void onLoad() {
    dataContext = startSpringApplication(SurfCloudMainApplication.class);
    for (int i = 0; i < 10; i++) {
      final TestEntity entity = new TestEntity(true, 25, 100.0, UUID.randomUUID(), "Test", false);
      TestPacket packet = new TestPacket(entity);
      packet.send();
    }
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

    return Util.tempChangeSystemClassLoader(joinClassLoader, () -> {
      final SpringApplicationBuilder builder = new SpringApplicationBuilder(applicationClass)
          .resourceLoader(new DefaultResourceLoader(joinClassLoader))
          .bannerMode(Mode.CONSOLE)
          .banner(new SurfSpringBanner());

      if (dataContext != null) {
        builder.parent(dataContext);
      }

      return builder.run();
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public void callRedisEvent(RedisEvent event) {
    checkNotNull(event, "event");
    checkState(dataContext != null, "Event called before onLoad");

    final ReactiveRedisTemplate<String, Object> template = dataContext.getBean("reactiveRedisTemplate", ReactiveRedisTemplate.class);
    for (final String channel : event.getChannels()) {
      template.convertAndSend(channel, event).log(redisEventLog).subscribe();
    }
  }

  public abstract Path getDataFolder();

  public static SurfCloudCoreInstance get() {
    return (SurfCloudCoreInstance) SurfCloudInstance.get();
  }
}
