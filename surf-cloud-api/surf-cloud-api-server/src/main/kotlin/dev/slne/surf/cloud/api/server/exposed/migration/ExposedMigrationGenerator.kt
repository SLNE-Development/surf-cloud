package dev.slne.surf.cloud.api.server.exposed.migration

import MigrationUtils
import dev.slne.surf.cloud.api.common.config.properties.requiredSystemProperty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction


@OptIn(ExperimentalDatabaseMigrationApi::class)
fun generateSimpleExposedMigration(
    vararg tables: Table,
    scriptName: String,
    scriptDirectory: String = "src/main/resources/db/migration"
) {
    generateExposedMigration {
        transaction(database) {
            MigrationUtils.generateMigrationScript(
                *tables,
                scriptName = scriptName,
                scriptDirectory = scriptDirectory
            )
        }
    }
}

fun generateExposedMigration(generator: ExposedMigrationGenerator.() -> Unit) {
    val database = Database.connect(
        url = requiredSystemProperty("migration", "dbUrl") { it }.value(),
        user = requiredSystemProperty("migration", "dbUser") { it }.value(),
        password = requiredSystemProperty("migration", "dbPassword") { it }.value()
    )

    val migrationGenerator = ExposedMigrationGenerator(database)
    generator(migrationGenerator)
}

class ExposedMigrationGenerator(val database: Database)