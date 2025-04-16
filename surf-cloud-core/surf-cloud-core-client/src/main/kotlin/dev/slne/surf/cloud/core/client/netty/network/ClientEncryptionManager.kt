package dev.slne.surf.cloud.core.client.netty.network

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.core.common.config.cloudConfig
import dev.slne.surf.cloud.core.common.netty.network.EncryptionManager
import dev.slne.surf.cloud.core.common.netty.network.HandlerNames
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.Channel
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import kotlin.io.path.div

@AutoService(EncryptionManager::class)
class ClientEncryptionManager : EncryptionManager() {
    private lateinit var sslContext: SslContext

    private val clientCertificateFile = (certificatesFolder / "client.crt").toFile()
    private val clientKeyFile = (certificatesFolder / "client.key").toFile()
    private val trustManagerFile = (certificatesFolder / "ca.crt").toFile()

    override fun setupEncryption(ch: Channel) {
        val config = cloudConfig.connectionConfig.nettyConfig
        ch.pipeline().addFirst(
            HandlerNames.SSL_HANDLER,
            sslContext.newHandler(ch.alloc(), config.host, config.port)
        )
    }

    override suspend fun init() {
        waitForFiles(clientCertificateFile, clientKeyFile, trustManagerFile)
        sslContext = buildSslContext()
    }

    private fun buildSslContext(): SslContext {
        return SslContextBuilder
            .forClient()
            .keyManager(clientCertificateFile, clientKeyFile)
            .trustManager(trustManagerFile)
            .sslProvider(SslProvider.JDK)
            .build()
    }
}