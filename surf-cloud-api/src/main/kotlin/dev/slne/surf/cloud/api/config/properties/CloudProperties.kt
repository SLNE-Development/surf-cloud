package dev.slne.surf.cloud.api.config.properties


object CloudProperties {
    @JvmStatic
    val SERVER_CATEGORY = systemProperty("serverCategory", { it }, "default")
}
