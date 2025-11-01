package dev.slne.surf.cloudtest.core.test.ppdc

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class PpdcTest {

    fun readTestData(player: CloudPlayer): TestData? {
        val ppdc = player.persistentData.get(TEST_DATA_KEY, PersistentPlayerDataType.TAG_CONTAINER)
            ?: return null

        return TestData.fromPpdc(ppdc)
    }

    fun saveTestData(player: CloudPlayer, testData: TestData) {
        player.editPdc {
            val ppdc = get(TEST_DATA_KEY, PersistentPlayerDataType.TAG_CONTAINER)
                ?: adapterContext.newPersistentDataContainer()
            testData.saveToPdc(ppdc)
            set(TEST_DATA_KEY, PersistentPlayerDataType.TAG_CONTAINER, ppdc)
        }
    }

    data class TestData(
        val test: String,
        val testInt: Int? = null,
        val testBoolean: Boolean? = null,
        val testList: List<String>? = null,
    ) : ComponentLike {

        fun saveToPdc(ppdc: PersistentPlayerDataContainer) {
            ppdc.setString(TEST_KEY, test)
            ppdc.setInt(TEST_KEY_INT, testInt)
            ppdc.setBoolean(TEST_KEY_BOOLEAN, testBoolean)
            if (testList == null) {
                ppdc.remove(TEST_KEY_LIST)
            } else {
                ppdc.set(TEST_KEY_LIST, PersistentPlayerDataType.LIST.strings(), testList)
            }
        }

        override fun asComponent() = buildText {
            appendMap(
                mapOf(
                    TEST_KEY.asString() to test,
                    TEST_KEY_INT.asString() to testInt?.toString(),
                    TEST_KEY_BOOLEAN.asString() to testBoolean?.toString(),
                    TEST_KEY_LIST.asString() to testList?.joinToString()
                ),
                {
                    Component.text(it, Colors.VARIABLE_KEY)
                },
                {
                    if (it == null) {
                        Component.text("N/A", Colors.VARIABLE_VALUE)
                    } else {
                        Component.text(it, Colors.VARIABLE_VALUE)
                    }
                },
                Component.empty()
            )
        }

        companion object {
            val TEST_KEY = key("test", "test_key")
            val TEST_KEY_INT = key("test", "test_key_int")
            val TEST_KEY_BOOLEAN = key("test", "test_key_boolean")
            val TEST_KEY_LIST = key("test", "test_key_list")


            fun fromPpdc(ppdc: PersistentPlayerDataContainerView): TestData? {
                val test = ppdc.getString(TEST_KEY) ?: return null
                val testInt = ppdc.getInt(TEST_KEY_INT)
                val testBoolean = ppdc.getBoolean(TEST_KEY_BOOLEAN)
                val testKeyList = ppdc.get(TEST_KEY_LIST, PersistentPlayerDataType.LIST.strings())

                return TestData(
                    test = test,
                    testInt = testInt,
                    testBoolean = testBoolean,
                    testList = testKeyList
                )
            }
        }
    }

    companion object {
        val TEST_DATA_KEY = key("test", "test_data")
    }
}