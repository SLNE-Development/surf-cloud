package dev.slne.surf.cloud.api.common.util.ssl

import io.netty.buffer.Unpooled
import io.netty.handler.codec.base64.Base64
import io.netty.util.CharsetUtil
import io.netty.util.internal.PlatformDependent
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

/**
 * Copied from [io.netty.handler.ssl.util.SelfSignedCertificate] because classloader issues
 *
 * @see io.netty.handler.ssl.util.SelfSignedCertificate
 * @author Netty
 */
class SelfSignedCertificateGenerator(
    fqdn: String,
    random: SecureRandom = dev.slne.surf.surfapi.core.api.util.random,
    bits: Int = 2048,
    notBefore: Date = Date(),
    notAfter: Date = Date(notBefore.time + 365L * 24 * 60 * 60 * 1000),
    algorithm: String = "RSA"
) {
    val certificate: File
    val privateKey: File
    val key: PrivateKey
    val cert: X509Certificate


    init {
        require(algorithm == "RSA" || algorithm == "EC") { "algorithm: $algorithm (expected: RSA or EC)" }

        val keyGen = KeyPairGenerator.getInstance(algorithm)
        keyGen.initialize(bits, random)
        val keypair = keyGen.generateKeyPair()

        val paths = BouncyCastleSelfSignedCertGenerator.generate(
            fqdn,
            keypair,
            random,
            notBefore,
            notAfter,
            algorithm
        )

        certificate = File(paths.first)
        privateKey = File(paths.second)
        key = keypair.private

        val certificateInput = certificate.inputStream()
        try {
            cert = CertificateFactory.getInstance("X509").generateCertificate(certificateInput) as X509Certificate
        } finally {
            certificateInput.close()
        }
    }

    fun delete() {
        certificate.delete()
        privateKey.delete()
    }
}

internal fun newSelfSignedCertificate(
    fqdn: String,
    key: PrivateKey,
    cert: X509Certificate
): Pair<String, String> {
    var fqdn = fqdn

    // Encode the private key into a file.
    var wrappedBuf = Unpooled.wrappedBuffer(key.encoded)
    var encodedBuf = Base64.encode(wrappedBuf, true)
    val keyText = buildString {
        append("-----BEGIN PRIVATE KEY-----\n")
        append(encodedBuf.toString(CharsetUtil.US_ASCII))
        append("\n-----END PRIVATE KEY-----\n")
    }

    encodedBuf.release()
    wrappedBuf.release()

    // Change all asterisk to 'x' for file name safety.
    fqdn = fqdn.replace(Regex("[^\\w.-]"), "x")

    val keyFile = PlatformDependent.createTempFile("keyutil_${fqdn}_", ".key", null)
    keyFile.deleteOnExit()

    var keyOut: FileOutputStream? = keyFile.outputStream()
    try {
        keyOut!!.write(keyText.toByteArray(CharsetUtil.US_ASCII))
        keyOut.close()
        keyOut = null
    } finally {
        if (keyOut != null) {
            keyOut.close()
            keyFile.delete()
        }
    }

    wrappedBuf = Unpooled.wrappedBuffer(cert.encoded)
    encodedBuf = Base64.encode(wrappedBuf, true)
    val certText = buildString {
        // Encode the certificate into a CRT file.
        append("-----BEGIN CERTIFICATE-----\n")
        append(encodedBuf.toString(CharsetUtil.US_ASCII))
        append("\n-----END CERTIFICATE-----\n")
    }
    encodedBuf.release()
    wrappedBuf.release()

    val certFile = PlatformDependent.createTempFile("keyutil_${fqdn}_", ".crt", null)
    certFile.deleteOnExit()

    var certOut: FileOutputStream? = certFile.outputStream()
    try {
        certOut!!.write(certText.toByteArray(CharsetUtil.US_ASCII))
        certOut.close()
        certOut = null
    } finally {
        if (certOut != null) {
            certOut.close()
            certFile.delete()
            keyFile.delete()
        }
    }

    return certFile.path to keyFile.path
}

internal object BouncyCastleSelfSignedCertGenerator {
    private val provider = BouncyCastleProvider()

    fun generate(
        fqdn: String,
        keyPair: KeyPair,
        random: SecureRandom,
        notBefore: Date,
        notAfter: Date,
        algorithm: String
    ): Pair<String, String> {
        val key = keyPair.private

        // Prepare the information required for generating an X.509 certificate.
        val owner = X500Name("CN=$fqdn")
        val builder = JcaX509v3CertificateBuilder(
            owner,
            BigInteger(64, random),
            notBefore,
            notAfter,
            owner,
            keyPair.public
        )

        val signer = JcaContentSignerBuilder(
            if (algorithm.equals("EC", true)) "SHA256withECDSA" else "SHA256WithRSAEncryption"
        ).build(key)
        val certHolder = builder.build(signer)
        val cert = JcaX509CertificateConverter().setProvider(provider).getCertificate(certHolder)
        cert.verify(keyPair.public)

        return newSelfSignedCertificate(fqdn, key, cert)
    }
}