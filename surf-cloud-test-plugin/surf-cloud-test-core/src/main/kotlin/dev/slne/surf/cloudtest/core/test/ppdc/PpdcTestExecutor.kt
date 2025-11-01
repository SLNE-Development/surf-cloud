package dev.slne.surf.cloudtest.core.test.ppdc

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.audience.Audience
import org.springframework.stereotype.Component

@Component
class PpdcTestExecutor(private val ppdcTest: PpdcTest) {

    suspend fun showPpdcTestData(sender: Audience, player: CloudPlayer) {
        sender.sendText {
            info("Test data for ")
            append(player.displayName())
            info(": ")
            val data = ppdcTest.readTestData(player)
            if (data == null) {
                error("No data found")
            } else {
                append(data)
            }
        }
    }

    suspend fun setRandomPpdcTestData(sender: Audience, player: CloudPlayer) {
        val testData = PpdcTest.TestData(
            test = "TestString_${(1000..9999).random()}",
            testInt = (0..100).random(),
            testBoolean = listOf(true, false).random(),
            testList = List((1..5).random()) { "Item${(1..100).random()}" }
        )
        ppdcTest.saveTestData(player, testData)

        sender.sendText {
            info("Test data for ")
            append(player.displayName())
            info(" set to: ")
            append(testData)
        }
    }
}