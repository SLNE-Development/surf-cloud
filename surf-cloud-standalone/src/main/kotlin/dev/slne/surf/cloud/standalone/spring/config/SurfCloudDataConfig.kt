package dev.slne.surf.cloud.standalone.spring.config

import com.zaxxer.hikari.HikariDataSource
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.core.common.config.cloudConfig
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.repository.query.QueryByExampleExecutor
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import java.sql.SQLException
import javax.sql.DataSource

@Configuration
//@Import(DataSourcePoolMetadataProvidersConfiguration::class)
@Import(TransactionAutoConfiguration::class)
class SurfCloudDataConfig {
    companion object {
        private val log = logger()
    }

//    @Bean
//    fun transactionManager(): TransactionManager {
//        JpaTransactionManager
//    }

    @Bean
    fun connectionDetails(): JdbcConnectionDetails {
//        JpaTransactionManager()

        val databaseConfig = cloudConfig.connectionConfig.databaseConfig

        return object : JdbcConnectionDetails {
            override fun getUsername(): String? {
                return databaseConfig.username
            }

            override fun getPassword(): String? {
                return databaseConfig.password
            }

            override fun getJdbcUrl(): String? {
                return databaseConfig.url
            }
        }
    }

//    @Configuration(proxyBeanMethods = false)
//    class DataSourceConfig {
//
//        @Configuration(proxyBeanMethods = false)
//        @ConditionalOnClass(HikariDataSource::class)
//        class Hikari {
//
//            @Bean
//            fun dataSource(connectionDetails: JdbcConnectionDetails): HikariDataSource {
//                return createDataSource<HikariDataSource>(connectionDetails, null)
//            }
//        }
//
//        companion object {
//            inline fun <reified T: DataSource> createDataSource(
//                connectionDetails: JdbcConnectionDetails,
//                classLoader: ClassLoader?
//            ): T {
//                return connectionDetails.run {
//                    DataSourceBuilder.create(classLoader)
//                        .type(T::class.java)
//                        .driverClassName(driverClassName)
//                        .url(jdbcUrl)
//                        .username(username)
//                        .password(password)
//                        .build() as T
//                }
//            }
//        }
//
//        @Bean
//        @Primary
//        fun dataSource(): DataSource {
//            val databaseConfig = cloudConfig.connectionConfig.databaseConfig
//            val dataSource = DriverManagerDataSource()
//
//            with(dataSource) {
////            val driverClassName = databaseConfig.type.driver
////            setDriverClassName("org.mariadb.jdbc.Driver")
////            println("Driver class name: $driverClassName")
//                username = databaseConfig.username
//                password = databaseConfig.password
//                url = databaseConfig.url
//            }
//
//            validateDatasource(dataSource)
//
//            return dataSource
//        }
//
//
//        private fun validateDatasource(dataSource: DriverManagerDataSource) {
//            try {
//                dataSource.connection.use {
//                    log.atFine()
//                        .log("Successfully connected to the database.")
//                }
//            } catch (e: SQLException) {
//                throw FatalSurfError {
//                    simpleErrorMessage("Failed to tryEstablishConnection0 to the database.")
//                    detailedErrorMessage("The database connection could not be established using the provided configuration.")
//                    cause(e)
//                    additionalInformation("Database URL: ${dataSource.url}")
//                    additionalInformation("Username: ${dataSource.username}")
//                    additionalInformation("Password set: ${dataSource.password != null}")
//                    possibleSolution("Check if the database server is running and accessible.")
//                    possibleSolution("Verify that the database URL, username, and password are correct.")
//                    possibleSolution("Ensure that the database driver (org.mariadb.jdbc.Driver) is compatible.")
//                    exitCode(ExitCodes.UNABLE_TO_CONNECT_TO_DATABASE)
//                }
//            }
//        }
//    }
}
