package dev.slne.surf.cloud.api.common.config.properties


object CloudProperties {

    const val SERVER_CATEGORY_NOT_SET = "NOT_SET"

    @JvmStatic
    val SERVER_CATEGORY = systemProperty("serverCategory", { it }, SERVER_CATEGORY_NOT_SET)

    @JvmStatic
    val SERVER_NAME = requiredSystemProperty("serverName") { it }
}
