package dev.slne.surf.cloud.api.common.config.properties


object CloudProperties {

    const val SERVER_CATEGORY_NOT_SET = "NOT_SET"

    @JvmStatic
    val SERVER_CATEGORY = systemProperty("surf.cloud.serverCategory", SERVER_CATEGORY_NOT_SET)

    @JvmStatic
    val SERVER_NAME = systemPropertyRequired("surf.cloud.serverName")
}
