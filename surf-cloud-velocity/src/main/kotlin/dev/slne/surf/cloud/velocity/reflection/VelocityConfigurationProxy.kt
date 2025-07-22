package dev.slne.surf.cloud.velocity.reflection

import com.velocitypowered.api.proxy.config.ProxyConfig
import dev.slne.surf.surfapi.core.api.reflection.Field
import dev.slne.surf.surfapi.core.api.reflection.SurfProxy
import dev.slne.surf.surfapi.core.api.reflection.createProxy
import dev.slne.surf.surfapi.core.api.reflection.surfReflection

@SurfProxy(qualifiedName = "com.velocitypowered.proxy.config.VelocityConfiguration")
interface VelocityConfigurationProxy {

    @Field(name = "forwardingSecret", type = Field.Type.GETTER)
    fun getForwardingSecret(instance: ProxyConfig): ByteArray

    @Field(name = "forwardingSecret", type = Field.Type.SETTER)
    fun setForwardingSecret(instance: ProxyConfig, value: ByteArray)

    companion object {
        val instance = surfReflection.createProxy<VelocityConfigurationProxy>()
    }
}