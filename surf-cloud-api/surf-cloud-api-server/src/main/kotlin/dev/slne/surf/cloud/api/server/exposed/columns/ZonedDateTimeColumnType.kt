package dev.slne.surf.cloud.api.server.exposed.columns

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.vendors.*
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

private val SQLITE_AND_ORACLE_DATE_TIME_STRING_FORMATTER by lazy {
    DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS",
        Locale.ROOT
    ).withZone(ZoneOffset.UTC)
}

private val MYSQL_FRACTION_DATE_TIME_STRING_FORMATTER by lazy {
    DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        Locale.ROOT
    ).withZone(ZoneOffset.UTC)
}

private val MYSQL_DATE_TIME_STRING_FORMATTER by lazy {
    DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss",
        Locale.ROOT
    ).withZone(ZoneOffset.UTC)
}

private val DEFAULT_DATE_TIME_STRING_FORMATTER by lazy {
    DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(Locale.ROOT).withZone(ZoneOffset.UTC)
}

private fun oracleDateTimeLiteral(instant: Instant) =
    "TO_TIMESTAMP('${SQLITE_AND_ORACLE_DATE_TIME_STRING_FORMATTER.format(instant)}', 'YYYY-MM-DD HH24:MI:SS.FF3')"

private fun formatterForDateString(date: String) = dateTimeWithFractionFormat(
    date.substringAfterLast('.', "").length
)

private fun dateTimeWithFractionFormat(fraction: Int): DateTimeFormatter {
    val baseFormat = "yyyy-MM-dd HH:mm:ss"
    val newFormat = if (fraction in 1..9) {
        (1..fraction).joinToString(prefix = "$baseFormat.", separator = "") { "S" }
    } else {
        baseFormat
    }
    return DateTimeFormatter.ofPattern(newFormat).withLocale(Locale.ROOT).withZone(ZoneOffset.UTC)
}

class ZonedDateTimeColumnType : ColumnType<ZonedDateTime>(), IDateColumnType {
    override val hasTimePart: Boolean = true
    override fun sqlType(): String = currentDialect.dataTypeProvider.timestampType()

    override fun nonNullValueToString(value: ZonedDateTime): String {
        val instant = value.withZoneSameInstant(ZoneOffset.UTC).toInstant()

        return when (val dialect = currentDialect) {
            is SQLiteDialect -> "'${SQLITE_AND_ORACLE_DATE_TIME_STRING_FORMATTER.format(instant)}'"
            is OracleDialect -> oracleDateTimeLiteral(instant)
            is MysqlDialect -> {
                val formatter =
                    if (dialect.isFractionDateTimeSupported()) MYSQL_FRACTION_DATE_TIME_STRING_FORMATTER else MYSQL_DATE_TIME_STRING_FORMATTER
                "'${formatter.format(instant)}'"
            }

            else -> "'${DEFAULT_DATE_TIME_STRING_FORMATTER.format(instant)}'"
        }
    }

    override fun valueFromDB(value: Any): ZonedDateTime = when (value) {
        is ZonedDateTime -> value
        is OffsetDateTime -> value.toLocalDateTime().atZone(ZoneId.systemDefault())
        is LocalDateTime -> value.atZone(ZoneId.systemDefault())
        is Timestamp -> value.toLocalDateTime().atZone(ZoneId.systemDefault())
        is java.util.Date -> value.toInstant().atZone(ZoneId.systemDefault())
        is Instant -> value.atZone(ZoneId.systemDefault())
        is Int -> longToZonedDateTime(value.toLong())
        is Long -> longToZonedDateTime(value)
        is String -> runCatching { ZonedDateTime.parse(value, formatterForDateString(value)) }
            .getOrElse {
                runCatching { Instant.parse(value).atZone(ZoneOffset.UTC) }
                    .getOrElse { error("Cannot parse ZonedDateTime from String: $value") }
            }

        else -> error("Unexpected value of type ZonedDateTime: $value of ${value::class.qualifiedName}")
    }


    override fun notNullValueToDB(value: ZonedDateTime): Any = when {
        currentDialect is SQLiteDialect -> SQLITE_AND_ORACLE_DATE_TIME_STRING_FORMATTER.format(
            LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC)
        )

        else -> {
            val datetime = LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC)
            Timestamp.valueOf(datetime)
        }
    }

    override fun readObject(rs: ResultSet, index: Int): Any? {
        return if (currentDialect is OracleDialect) {
            rs.getObject(index, Timestamp::class.java)
        } else {
            super.readObject(rs, index)
        }
    }

    override fun nonNullValueAsDefaultString(value: ZonedDateTime): String {
        val dialect = currentDialect
        return when {
            dialect is PostgreSQLDialect ->
                "'${
                    SQLITE_AND_ORACLE_DATE_TIME_STRING_FORMATTER.format(
                        value.withZoneSameInstant(
                            ZoneOffset.UTC
                        )
                    ).trimEnd('0').trimEnd('.')
                }'::timestamp without time zone"

            (dialect as? H2Dialect)?.h2Mode == H2Dialect.H2CompatibilityMode.Oracle ->
                "'${
                    SQLITE_AND_ORACLE_DATE_TIME_STRING_FORMATTER.format(
                        value.withZoneSameInstant(
                            ZoneOffset.UTC
                        )
                    ).trimEnd('0').trimEnd('.')
                }'"

            else -> super.nonNullValueAsDefaultString(value)
        }
    }

    private fun longToZonedDateTime(value: Long) =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())

    companion object {
        internal val INSTANCE = ZonedDateTimeColumnType()
    }
}

fun Table.zonedDateTime(name: String): Column<ZonedDateTime> =
    registerColumn(name, ZonedDateTimeColumnType())


open class CurrentTimestampBase<T>(columnType: IColumnType<T & Any>) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        +when {
            (currentDialect as? MysqlDialect)?.isFractionDateTimeSupported() == true -> "CURRENT_TIMESTAMP(6)"
            else -> "CURRENT_TIMESTAMP"
        }
    }
}

object CurrentZonedDateTime : CurrentTimestampBase<ZonedDateTime>(ZonedDateTimeColumnType.INSTANCE)