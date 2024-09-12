package dev.slne.surf.cloud.core.spring.config;

import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.config.SurfCloudConfig.ConnectionConfig.DatabaseConfig;
import dev.slne.surf.surfapi.core.api.SurfCoreApi;
import javax.sql.DataSource;
import lombok.extern.flogger.Flogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@Flogger
public class SurfCloudDataConfig {

  @Bean
  public SurfCloudConfig surfDataConfig() {

    return SurfCoreApi.getCore().createModernYamlConfig(
        SurfCloudConfig.class,
        SurfCloudCoreInstance.get().getDataFolder(),
        "config.yml"
    );
  }

  @Bean
  @Primary
  public DataSource dataSource(SurfCloudConfig surfCloudConfig) {
    final DatabaseConfig databaseConfig = surfCloudConfig.connectionConfig.databaseConfig;
    final DriverManagerDataSource dataSource = new DriverManagerDataSource();

//    Util.tempChangeSystemClassLoader(getClass().getClassLoader(), () -> {
    dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
//    });

    dataSource.setUsername(databaseConfig.username);
    dataSource.setPassword(databaseConfig.password);
    dataSource.setUrl(databaseConfig.url);

    return dataSource;
  }
}
