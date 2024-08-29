package dev.slne.surf.data;

import dev.slne.surf.data.core.SurfDataCoreInstance;
import dev.slne.surf.data.core.config.SurfDataConfig;
import dev.slne.surf.data.core.config.SurfDataConfig.ConnectionConfig.DatabaseConfig;
import dev.slne.surf.surfapi.core.api.SurfCoreApi;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Flogger
@dev.slne.surf.data.api.SurfDataApplication
public class SurfDataApplication implements AsyncConfigurer {

  @Bean
  public SurfDataConfig surfDataConfig() {
    return SurfCoreApi.getCore().createModernYamlConfig(
        SurfDataConfig.class,
        SurfDataCoreInstance.get().getDataFolder(),
        "config.yml"
    );
  }

  @Bean
  @Primary
  public DataSource dataSource(SurfDataConfig surfDataConfig) {
    final DatabaseConfig databaseConfig = surfDataConfig.connectionConfig.databaseConfig;
    final DriverManagerDataSource dataSource = new DriverManagerDataSource();

    dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
    dataSource.setUsername(databaseConfig.username);
    dataSource.setPassword(databaseConfig.password);
    dataSource.setUrl(databaseConfig.url);

    return dataSource;
  }

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
