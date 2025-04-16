package dev.slne.surf.cloud.standalone.netty.server.network

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.core.common.netty.network.EncryptionManager
import dev.slne.surf.cloud.core.common.netty.network.HandlerNames
import io.netty.channel.Channel
import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import kotlin.io.path.div

@AutoService(EncryptionManager::class)
class ServerEncryptionManager : EncryptionManager() {
    private val serverCertificateFile = (certificatesFolder / "server.crt").toFile()
    private val serverKeyFile = (certificatesFolder / "server.key").toFile()
    private val trustManagerFile = (certificatesFolder / "ca.crt").toFile()

    override fun setupEncryption(ch: Channel) {
        ch.pipeline().addFirst(
//            HandlerNames.SPLITTER,
            HandlerNames.SSL_HANDLER,
            buildSslContext().newHandler(ch.alloc())
        )
    }

    override suspend fun init() {
        waitForFiles(serverCertificateFile, serverKeyFile, trustManagerFile)
    }

    private fun buildSslContext(): SslContext {
        return SslContextBuilder
            .forServer(serverCertificateFile, serverKeyFile)
            .trustManager(trustManagerFile)
            .clientAuth(ClientAuth.REQUIRE)
            .build()
    }

//    private fun buildTrustManager(): TrustManagerFactory {
//        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
//
//        clientCertificatesFolder.toFile().listFiles { file -> file.extension == "crt" }
//            ?.forEachIndexed { index, certFile ->
//                certFile.inputStream().use { inputStream ->
//                    val certificate =
//                        CertificateFactory.getInstance("X.509").generateCertificate(inputStream)
//                    keyStore.setCertificateEntry("client-cert-$index", certificate)
//                }
//            }
//
//        val trustManagerFactory =
//            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//        trustManagerFactory.init(keyStore)
//        return trustManagerFactory
//    }
}