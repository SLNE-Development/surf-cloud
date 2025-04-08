package dev.slne.surf.cloud.core.client.netty.network

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.core.common.netty.network.EncryptionManager
import dev.slne.surf.cloud.core.common.netty.network.HandlerNames
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.Channel
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder

object ClientEncryptionManager : EncryptionManager() {
    private val log = logger()
    private lateinit var sslContext: SslContext

    private val clientCertificateFile =
        certificatesFolder.resolve("${CloudProperties.SERVER_NAME}.crt").toFile()
    private val clientKeyFile =
        certificatesFolder.resolve("${CloudProperties.SERVER_NAME}.key").toFile()
    private val serverCertificate = certificatesFolder.resolve("server.crt").toFile()

    override fun setupEncryption(ch: Channel) {
        ch.pipeline().addFirst(
            HandlerNames.SSL_HANDLER,
            sslContext.newHandler(ch.alloc())
        )
    }

    override suspend fun init() {
        waitForFiles(clientCertificateFile, clientKeyFile, serverCertificate)
        sslContext = buildSslContext()
    }

    private fun buildSslContext(): SslContext {
        return SslContextBuilder
            .forClient()
            .keyManager(clientCertificateFile, clientKeyFile)
            .trustManager(serverCertificate)
            .build()
    }
}