package dev.slne.surf.cloud.core.common.util.encryption

import com.google.common.primitives.Longs
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.getOrMapAndThrow
import dev.slne.surf.cloud.core.common.util.random
import net.kyori.adventure.chat.SignedMessage.signature
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object Crypt {
    private const val SYMMETRIC_ALGORITHM = "AES"
    private const val SYMMETRIC_BITS = 128
    private const val ASYMMETRIC_ALGORITHM = "RSA"
    private const val ASYMMETRIC_BITS = 1024
    private const val BYTE_ENCODING = "ISO_8859_1"
    private const val HASH_ALGORITHM = "SHA-1"
    const val SIGNING_ALGORITHM = "SHA256withRSA"
    const val SIGNATURE_BYTES = 256
    private const val PEM_RSA_PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----"
    private const val PEM_RSA_PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----"
    const val RSA_PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----"
    private const val RSA_PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----"
    const val MIME_LINE_SEPARATOR = "\n"
    val mimeEncoder = Base64.getMimeEncoder(76, MIME_LINE_SEPARATOR.toByteArray(Charsets.UTF_8))

    fun generateSecretKey(): SecretKey = safeCrypto {
        val keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM)
        keyGenerator.init(SYMMETRIC_BITS)
        keyGenerator.generateKey()
    }

    fun generateKeyPair(): KeyPair = safeCrypto {
        val keyPairGenerator = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM)
        keyPairGenerator.initialize(ASYMMETRIC_BITS)
        keyPairGenerator.generateKeyPair()
    }

    fun digestData(baseServerId: String, publicKey: PublicKey, secretKey: SecretKey): ByteArray =
        safeCrypto {
            digestData(
                baseServerId.toByteArray(Charsets.ISO_8859_1),
                secretKey.encoded,
                publicKey.encoded
            )
        }

    private fun digestData(vararg bytes: ByteArray): ByteArray {
        val messageDigest = MessageDigest.getInstance(HASH_ALGORITHM)
        bytes.forEach { messageDigest.update(it) }

        return messageDigest.digest()
    }

    private fun <T : Key> rsaStringToKey(
        key: String,
        prefix: String,
        suffix: String,
        decoder: ByteArrayToKeyFunction<T>
    ): T = safeCrypto {
        var key = key

        var i = key.indexOf(prefix)
        if (i != -1) {
            i += prefix.length
            val j = key.indexOf(suffix, i)
            key = key.substring(i, j + 1)
        }

        decoder(Base64.getMimeDecoder().decode(key))
    }

    fun stringToPemRsaPrivateKey(key: String): PrivateKey = rsaStringToKey(
        key,
        PEM_RSA_PRIVATE_KEY_HEADER,
        PEM_RSA_PRIVATE_KEY_FOOTER
    ) { byteToPrivateKey(it) }

    fun stringToRsaPublicKey(key: String): PublicKey = rsaStringToKey(
        key,
        RSA_PUBLIC_KEY_HEADER,
        RSA_PUBLIC_KEY_FOOTER
    ) { byteToPublicKey(it) }

    fun rsaPublicKeyToString(key: PublicKey): String {
        check(key.algorithm == ASYMMETRIC_ALGORITHM) { "Public key must be $ASYMMETRIC_ALGORITHM" }
        return RSA_PUBLIC_KEY_HEADER + MIME_LINE_SEPARATOR + mimeEncoder.encodeToString(key.encoded) + MIME_LINE_SEPARATOR + RSA_PUBLIC_KEY_FOOTER + MIME_LINE_SEPARATOR
    }

    fun pemRsaPrivateKeyToString(key: PrivateKey): String {
        check(key.algorithm == ASYMMETRIC_ALGORITHM) { "Private key must be $ASYMMETRIC_ALGORITHM" }
        return PEM_RSA_PRIVATE_KEY_HEADER + MIME_LINE_SEPARATOR + mimeEncoder.encodeToString(key.encoded) + MIME_LINE_SEPARATOR + PEM_RSA_PRIVATE_KEY_FOOTER + MIME_LINE_SEPARATOR
    }

    private fun byteToPrivateKey(key: ByteArray): PrivateKey = safeCrypto {
        val encodedKeySpec = PKCS8EncodedKeySpec(key)
        val keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM)
        keyFactory.generatePrivate(encodedKeySpec)
    }

    fun byteToPublicKey(key: ByteArray): PublicKey = safeCrypto {
        val encodedKeySpec = X509EncodedKeySpec(key)
        val keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM)
        keyFactory.generatePublic(encodedKeySpec)
    }

    //     public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] encryptedSecretKey) throws CryptException {
    //        byte[] bs = decryptUsingKey(privateKey, encryptedSecretKey);
    //
    //        try {
    //            return new SecretKeySpec(bs, "AES");
    //        } catch (Exception var4) {
    //            throw new CryptException(var4);
    //        }
    //    }

    fun decryptByteToSecretKey(privateKey: PrivateKey, encryptedSecretKey: ByteArray): SecretKey =
        safeCrypto {
            val bs = decryptUsingKey(privateKey, encryptedSecretKey)
            SecretKeySpec(bs, "AES")
        }

    fun encryptUsingKey(key: Key, data: ByteArray): ByteArray {
        return cipherData(1, key, data)
    }

    fun decryptUsingKey(key: Key, data: ByteArray): ByteArray {
        return cipherData(2, key, data)
    }

    fun cipherData(opMode: Int, key: Key, data: ByteArray): ByteArray = safeCrypto {
        setupCipher(opMode, key.algorithm, key).doFinal(data)
    }

    fun setupCipher(opMode: Int, algorithm: String, key: Key): Cipher {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(opMode, key)
        return cipher
    }

    fun getCipher(opMode: Int, key: Key): Cipher = safeCrypto {
        val cipher = Cipher.getInstance("$SYMMETRIC_ALGORITHM/CFB8/NoPadding")
        cipher.init(opMode, key)
        cipher
    }

    private inline fun <R> safeCrypto(crypto: () -> R): R =
        runCatching(crypto).getOrMapAndThrow { CryptException(it) }

    @JvmRecord
    data class SaltSignaturePair(val salt: Long, val signature: ByteArray) {
        companion object {
            val EMPTY = SaltSignaturePair(0L, byteArrayOf())

            fun write(buf: SurfByteBuf, signatureData: SaltSignaturePair) {
                buf.writeLong(signatureData.salt)
                buf.writeByteArray(signatureData.signature)
            }
        }

        constructor(buf: SurfByteBuf) : this(buf.readLong(), buf.readByteArray())

        val isValid: Boolean
            get() = signature.isNotEmpty()

        fun saltAsBytes(): ByteArray = Longs.toByteArray(salt)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SaltSignaturePair) return false

            if (salt != other.salt) return false
            if (!signature.contentEquals(other.signature)) return false
            if (isValid != other.isValid) return false

            return true
        }

        override fun hashCode(): Int {
            var result = salt.hashCode()
            result = 31 * result + signature.contentHashCode()
            result = 31 * result + isValid.hashCode()
            return result
        }
    }

    object SaltSupplier {
        fun getLong() = random.nextLong()
    }
}

fun interface ByteArrayToKeyFunction<T : Key> {
    operator fun invoke(bytes: ByteArray): T
}