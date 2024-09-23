package dev.slne.surf.cloud;

import dev.slne.surf.cloud.api.SurfCloudApplication;
import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.NestedRuntimeException;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Flogger
@SurfCloudApplication(
    jpaBasePackages = "dev.slne.surf.cloud.core.test",
    redisBasePackages = {"dev.slne.surf.cloud.core.util"}
)
public class SurfCloudMainApplication implements AsyncConfigurer, ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) -> {

      if (ex instanceof FatalSurfError fatalSurfError) {
        handleFatalSurfError(fatalSurfError, method, params);
      } else if (ex instanceof NestedRuntimeException nested
          && nested.getRootCause() instanceof FatalSurfError fatalSurfError) {
        handleFatalSurfError(fatalSurfError, method, params);
      } else {
        log.atSevere()
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
    };
  }


  @Override
  public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  private void handleFatalSurfError(@NotNull FatalSurfError fatalSurfError, @NotNull Method method, Object[] params) {
    log.atSevere()
        .log(
            "Fatal error occurred in method %s with parameters %s",
            method.getName(),
            ArrayUtils.toString(params)
        );

    log.atSevere()
        .log(fatalSurfError.buildMessage());

    if (applicationContext != null) {
      SpringApplication.exit(applicationContext, fatalSurfError);
    }
  }
}
