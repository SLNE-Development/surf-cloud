package dev.slne.surf.data;

import java.util.concurrent.TimeUnit;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Flogger
@dev.slne.surf.data.api.SurfDataApplication
public class SurfDataApplication implements AsyncConfigurer {

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
