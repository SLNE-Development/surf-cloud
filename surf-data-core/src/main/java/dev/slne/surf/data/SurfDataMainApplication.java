package dev.slne.surf.data;

import dev.slne.surf.data.api.SurfDataApplication;
import java.util.concurrent.TimeUnit;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Flogger
@SurfDataApplication(
    jpaBasePackages = "dev.slne.surf.data.core.test",
    redisBasePackages = {"dev.slne.surf.data.core.util"}
//    jpaRepositoriesPackages = __JpaRepositoriesMarker.class,
//    redisRepositoriesPackages = __RedisRepositoriesMarker.class
)
public class SurfDataMainApplication implements AsyncConfigurer {

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
