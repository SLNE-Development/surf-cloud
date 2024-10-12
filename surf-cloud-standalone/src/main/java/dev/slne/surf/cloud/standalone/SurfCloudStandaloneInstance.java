package dev.slne.surf.cloud.standalone;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.auto.service.AutoService;
import dev.slne.surf.cloud.api.SurfCloudInstance;
import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import dev.slne.surf.cloud.standalone.redis.RedisEvent;
import java.nio.file.Path;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.util.Logger;
import reactor.util.Loggers;

@AutoService(SurfCloudInstance.class)
public class SurfCloudStandaloneInstance extends SurfCloudCoreInstance {

  private static final Logger redisEventLog = Loggers.getLogger("RedisEvent");

  public SurfCloudStandaloneInstance() throws IllegalAccessException {
  }

  public void callRedisEvent(RedisEvent event) {
    checkNotNull(event, "event");
    checkState(getDataContext() != null, "Event called before onLoad");

    final ReactiveRedisTemplate<String, Object> template = getDataContext().getBean(
        "reactiveRedisTemplate", ReactiveRedisTemplate.class);
    for (final String channel : event.getChannels()) {
      template.convertAndSend(channel, event).log(redisEventLog).subscribe();
    }
  }

  @Override
  public void onEnable() {
    super.onEnable();

//    for (int i = 0; i < 10; i++) {
//      final TestEntity entity = new TestEntity(true, 25, 100.0, UUID.randomUUID(), "Test", false);
//      TestPacket packet = new TestPacket(entity);
//      packet.send();
//    }

    System.out.println("ready");
  }

  @Override
  public Path getDataFolder() {
    return Path.of("");
  }

  @Override
  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  @Override
  protected String getSpringProfile() {
    return "independent";
  }

  public static SurfCloudStandaloneInstance get() {
    return (SurfCloudStandaloneInstance) SurfCloudInstance.get();
  }
}
