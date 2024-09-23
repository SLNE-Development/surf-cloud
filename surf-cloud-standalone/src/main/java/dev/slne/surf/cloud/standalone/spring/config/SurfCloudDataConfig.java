package dev.slne.surf.cloud.standalone.spring.config;

import dev.slne.surf.cloud.api.exceptions.FatalSurfError;
import dev.slne.surf.cloud.api.exceptions.FatalSurfError.ExitCodes;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.config.SurfCloudConfig.ConnectionConfig.DatabaseConfig;
import java.sql.Connection;
import java.sql.SQLException;
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
  @Primary
  public DataSource dataSource(SurfCloudConfig surfCloudConfig) {
    final DatabaseConfig databaseConfig = surfCloudConfig.connectionConfig.databaseConfig;
    final DriverManagerDataSource dataSource = new DriverManagerDataSource();

    dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
    dataSource.setUsername(databaseConfig.username);
    dataSource.setPassword(databaseConfig.password);
    dataSource.setUrl(databaseConfig.url);

    validateDatasource(dataSource);

    return dataSource;
  }

  private static void validateDatasource(DriverManagerDataSource dataSource) {
    try (final Connection ignored = dataSource.getConnection()) {
      log.atFine()
          .log("Successfully connected to the database.");
    } catch (SQLException e) {
      throw FatalSurfError.builder()
          .simpleErrorMessage("Failed to connect to the database.")
          .detailedErrorMessage("The database connection could not be established using the provided configuration.")
          .cause(e)
          .additionalInformation("Database URL: " + dataSource.getUrl())
          .additionalInformation("Username: " + dataSource.getUsername())
          .additionalInformation("Password set: " + (dataSource.getPassword() != null))
          .possibleSolution("Check if the database server is running and accessible.")
          .possibleSolution("Verify that the database URL, username, and password are correct.")
          .possibleSolution("Ensure that the database driver (org.mariadb.jdbc.Driver) is compatible.")
          .exitCode(ExitCodes.UNABLE_TO_CONNECT_TO_DATABASE)
          .build();
    }
  }
}
