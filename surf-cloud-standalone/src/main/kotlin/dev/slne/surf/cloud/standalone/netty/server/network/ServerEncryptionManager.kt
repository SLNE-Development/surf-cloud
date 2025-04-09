package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.common.netty.network.EncryptionManager
import dev.slne.surf.cloud.core.common.netty.network.HandlerNames
import io.netty.channel.Channel
import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManagerFactory

object ServerEncryptionManager : EncryptionManager() {
    private val serverCertificateFile = certificatesFolder.resolve("server.crt").toFile()
    private val serverKeyFile = certificatesFolder.resolve("server.key").toFile()
    private val clientCertificatesFolder =
        certificatesFolder.resolve("clients").also { it.toFile().mkdirs() }

    override fun setupEncryption(ch: Channel) {
        ch.pipeline().addFirst(
//            HandlerNames.SPLITTER,
            HandlerNames.SSL_HANDLER,
            buildSslContext().newHandler(ch.alloc())
        )
    }

    override suspend fun init() {
        waitForFiles(serverCertificateFile, serverKeyFile)
    }

    private fun buildSslContext(): SslContext {
        return SslContextBuilder
            .forServer(serverCertificateFile, serverKeyFile)
            .trustManager(buildTrustManager())
            .clientAuth(ClientAuth.REQUIRE)
            .build()
    }

    private fun buildTrustManager(): TrustManagerFactory {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }

        clientCertificatesFolder.toFile().listFiles { file -> file.extension == "crt" }
            ?.forEachIndexed { index, certFile ->
                certFile.inputStream().use { inputStream ->
                    val certificate =
                        CertificateFactory.getInstance("X.509").generateCertificate(inputStream)
                    keyStore.setCertificateEntry("client-cert-$index", certificate)
                }
            }

        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        return trustManagerFactory
    }
}