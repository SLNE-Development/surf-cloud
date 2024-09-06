package dev.slne.surf.data.core.spring.config;

import dev.slne.surf.data.core.SurfDataCoreInstance;
import dev.slne.surf.data.core.config.SurfDataConfig;
import dev.slne.surf.data.core.config.SurfDataConfig.ConnectionConfig.DatabaseConfig;
import dev.slne.surf.surfapi.core.api.SurfCoreApi;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SurfDataDataConfig {

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
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
}
