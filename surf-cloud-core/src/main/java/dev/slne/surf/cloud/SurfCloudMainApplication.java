package dev.slne.surf.cloud;

import dev.slne.surf.cloud.api.SurfCloudApplication;
import java.util.concurrent.TimeUnit;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Flogger
@SurfCloudApplication(
    jpaBasePackages = "dev.slne.surf.cloud.core.test",
    redisBasePackages = {"dev.slne.surf.cloud.core.util"}
//    jpaRepositoriesPackages = __JpaRepositoriesMarker.class,
//    redisRepositoriesPackages = __RedisRepositoriesMarker.class
)
public class SurfCloudMainApplication implements AsyncConfigurer {

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) -> log.atSevere()
        .atMostEvery(1, TimeUnit.SECONDS)
        .withCause(ex)
        .log(
            """
                Exception message - %s
                Method name - %s
                ParameterValues - %s
                """,
            ex.getMessage(),
            method.getName(),
            ArrayUtils.toString(params)
        );
  }
}
