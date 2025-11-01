package dev.slne.surf.cloud.api.common.config.properties

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties.SERVER_CATEGORY_NOT_SET


/**
 * Contains predefined cloud properties for Surf Cloud applications.
 */
object CloudProperties {

    /**
     * Default value indicating that the server category is not set.
     */
    const val SERVER_CATEGORY_NOT_SET = "NOT_SET"

    /**
     * System property for the server category. Defaults to [SERVER_CATEGORY_NOT_SET].
     */
    @JvmStatic
    val SERVER_CATEGORY = systemProperty("surf.cloud.serverCategory", SERVER_CATEGORY_NOT_SET)

    /**
     * Required system property for the server name.
     */
    @JvmStatic
    val SERVER_NAME = systemPropertyRequired("surf.cloud.serverName")
}
