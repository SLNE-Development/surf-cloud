package dev.slne.surf.cloud.standalone.test.sync

import dev.slne.surf.cloud.api.common.sync.SyncValue
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class SyncValueTest: ApplicationRunner {
    val syncValue = SyncValue("basic_sync_value", BasicSyncValue.DEFAULT)
    var syncValueDelegate by syncValue

    val rateLimitedSyncValue = SyncValue("rate_limited_sync_value", BasicSyncValue.DEFAULT)
        .rateLimited(5.seconds)
    var rateLimitedSyncValueDelegate by rateLimitedSyncValue

    override fun run(args: ApplicationArguments?) {
        syncValue.subscribe { old, new ->
            println("SyncValue changed from $old to $new")
        }

        syncValueDelegate = BasicSyncValue(
            id = "new_id",
            name = "new_name",
            value = "new_value"
        )

        println("Current SyncValue: $syncValueDelegate")

        rateLimitedSyncValue.subscribe { old, new ->
            println("RateLimitedSyncValue changed from $old to $new")
        }

        rateLimitedSyncValueDelegate = BasicSyncValue(
            id = "rate_limited_new_id",
            name = "rate_limited_new_name",
            value = "rate_limited_new_value"
        )

        println("Current RateLimitedSyncValue: $rateLimitedSyncValueDelegate")

        for (i in 1..5) {
            rateLimitedSyncValueDelegate = BasicSyncValue(
                id = "rate_limited_id_$i",
                name = "rate_limited_name_$i",
                value = "rate_limited_value_$i"
            )
        }

        println("Current RateLimitedSyncValue after multiple updates: $rateLimitedSyncValueDelegate")
    }


    data class BasicSyncValue(
        val id: String,
        val name: String,
        val value: String
    ) {
        companion object {
            val DEFAULT = BasicSyncValue(
                id = "default_id",
                name = "default_name",
                value = "default_value"
            )
        }
    }
}