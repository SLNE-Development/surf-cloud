package dev.slne.surf.cloud.api.server.plugin

import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.*
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.util.TxUtils
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.aspectj.AnnotationTransactionAspect
import org.springframework.transaction.support.TransactionOperations
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.*
import javax.sql.DataSource

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EnableJpaRepositories
@Configuration
@Import(TransactionConfiguration::class, JpaAuditingConfiguration::class)
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
@EnableAsync(mode = AdviceMode.ASPECTJ)
@EnableCaching(mode = AdviceMode.ASPECTJ)
annotation class AdditionalStandaloneConfiguration

@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@Configuration
class JpaAuditingConfiguration {
    @Bean
    fun auditingDateTimeProvider() = DateTimeProvider { Optional.of(ZonedDateTime.now()) }
}

@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@Configuration
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
class TransactionConfiguration {

    @Bean(TxUtils.DEFAULT_TRANSACTION_MANAGER)
    @Primary
    fun txManager(dataSource: DataSource) = DataSourceTransactionManager(dataSource).also {
        AnnotationTransactionAspect.aspectOf().transactionManager = it
    }

    @Bean
    fun transactionOperations(txManager: PlatformTransactionManager): TransactionOperations {
        return TransactionTemplate(txManager)
    }
}
