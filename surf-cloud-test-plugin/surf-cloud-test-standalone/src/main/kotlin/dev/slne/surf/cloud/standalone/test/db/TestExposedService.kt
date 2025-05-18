package dev.slne.surf.cloud.standalone.test.db

import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.cloud.api.server.plugin.utils.currentDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport

@Service
class TestExposedService { // currentDatabase

    @Transactional
    fun createTestEntitySync() {
        val current = TransactionManager.current()
        println("Current transaction: $current")
        println("Current database connection: ${current.connection}")
        println("current database url: ${current.db.url}")
        val transactionInfo =
            TransactionAspectSupport::class.java.getDeclaredMethod("currentTransactionInfo").apply {
                isAccessible = true
            }.invoke(null)
        val transactionManager =
            transactionInfo::class.java.getDeclaredMethod("getTransactionManager").apply {
                isAccessible = true
            }.invoke(transactionInfo) as SpringTransactionManager
        println("Transaction manager: $transactionManager")

        TestExposedEntity.new {
            name = "Test sync"
            description = "Test description"
        }
    }

    @CoroutineTransactional
    suspend fun createTestEntityAsync() = withContext(Dispatchers.IO) {
        println("Inside withContext(Dispatchers.IO)!")
        val current = TransactionManager.current()
        println("Current transaction: $current")
        println("Current database connection: ${current.connection}")
        println("current database url: ${current.db.url}")

        TestExposedEntity.new {
            name = "Test async"
            description = "Test description"
        }
    }

    suspend fun createTestEntityAsyncWithCurrentDbParam() =
        newSuspendedTransaction(db = currentDb) {
            println("Inside newSuspendedTransaction with currentDb!")
            val current = TransactionManager.current()
            println("Current transaction: $current")
            println("Current database connection: ${current.connection}")
            println("current database url: ${current.db.url}")

            TestExposedEntity.new {
                name = "Test async with currentDb"
                description = "Test description"
            }
        }
}

@Component
class TestExposedServiceRunner(private val service: TestExposedService) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        println("Running TestExposedServiceRunner...")
        println("Creating test entity synchronously...")
        service.createTestEntitySync()
        println("Test entity created.")

        println("Creating test entity asynchronously...")
        runBlocking {
            service.createTestEntityAsync()
        }
        println("Test entity created.")

        println("Creating test entity asynchronously with currentDb param...")
        runBlocking {
            service.createTestEntityAsyncWithCurrentDbParam()
        }
        println("Test entity created.")
    }
}