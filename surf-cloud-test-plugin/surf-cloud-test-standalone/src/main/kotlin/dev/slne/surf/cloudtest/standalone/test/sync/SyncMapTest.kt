package dev.slne.surf.cloudtest.standalone.test.sync

import dev.slne.surf.cloud.api.common.sync.SyncMap
import kotlinx.serialization.Serializable
import org.springframework.stereotype.Component

@Component
class SyncMapTest {
    val syncMap = SyncMap<String, PlayerData>("player_data_map")

    fun test() {
        syncMap.subscribe { key, oldValue, newValue ->
            println("SyncMap changed for key '$key': $oldValue -> $newValue")
        }

        // Add some initial data
        syncMap["player1"] = PlayerData(
            uuid = "uuid-1",
            name = "Player1",
            score = 100
        )

        syncMap["player2"] = PlayerData(
            uuid = "uuid-2",
            name = "Player2",
            score = 200
        )

        println("Current map size: ${syncMap.size}")
        println("Player1 data: ${syncMap["player1"]}")
        println("Player2 data: ${syncMap["player2"]}")

        // Update existing entry
        syncMap["player1"] = PlayerData(
            uuid = "uuid-1",
            name = "Player1",
            score = 150
        )

        // Remove entry
        syncMap.remove("player2")
        println("Map size after removal: ${syncMap.size}")

        // Iterate over entries
        println("All players:")
        for ((key, value) in syncMap) {
            println("  $key -> $value")
        }

        // Create snapshot for read-only access
        val snapshot = syncMap.snapshot()
        println("Snapshot size: ${snapshot.size}")
    }

    @Serializable
    data class PlayerData(
        val uuid: String,
        val name: String,
        val score: Int
    )
}
