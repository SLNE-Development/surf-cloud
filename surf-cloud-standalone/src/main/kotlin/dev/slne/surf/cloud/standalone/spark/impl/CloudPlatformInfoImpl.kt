package dev.slne.surf.cloud.standalone.spark.impl

import dev.slne.surf.cloud.api.common.version.CloudVersion
import me.lucko.spark.common.platform.PlatformInfo

object CloudPlatformInfoImpl: PlatformInfo {
    override fun getType(): PlatformInfo.Type? {
        return PlatformInfo.Type.APPLICATION
    }

    override fun getName(): String? {
        return "Server"
    }

    override fun getBrand(): String? {
        return "Surf Cloud"
    }

    override fun getVersion(): String? {
        return CloudVersion.fullVersion
    }

    override fun getMinecraftVersion(): String? {
        return CloudVersion.minecraftVersion
    }
}