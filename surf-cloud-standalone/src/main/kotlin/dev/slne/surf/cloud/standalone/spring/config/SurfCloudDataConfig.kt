package dev.slne.surf.cloud.standalone.spring.config

import dev.slne.surf.cloud.api.exceptions.ExitCodes
import dev.slne.surf.cloud.api.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.config.cloudConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.sql.SQLException
import javax.sql.DataSource

@Configuration
class SurfCloudDataConfig {
    private val log = logger()

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val databaseConfig = cloudConfig.connectionConfig.databaseConfig
        val dataSource = DriverManagerDataSource()

        with(dataSource) {
            setDriverClassName("org.mariadb.jdbc.Driver")
            username = databaseConfig.username
            password = databaseConfig.password
            url = databaseConfig.url
        }

        validateDatasource(dataSource)

        return dataSource
    }

    private fun validateDatasource(dataSource: DriverManagerDataSource) {
        try {
            dataSource.connection.use {
                log.atFine()
                    .log("Successfully connected to the database.")
            }
        } catch (e: SQLException) {
            throw FatalSurfError {
                simpleErrorMessage("Failed to tryEstablishConnection0 to the database.")
                detailedErrorMessage("The database connection could not be established using the provided configuration.")
                cause(e)
                additionalInformation("Database URL: ${dataSource.url}")
                additionalInformation("Username: ${dataSource.username}")
                additionalInformation("Password set: ${dataSource.password != null}")
                possibleSolution("Check if the database server is running and accessible.")
                possibleSolution("Verify that the database URL, username, and password are correct.")
                possibleSolution("Ensure that the database driver (org.mariadb.jdbc.Driver) is compatible.")
                exitCode(ExitCodes.UNABLE_TO_CONNECT_TO_DATABASE)
            }
        }
    }
}
