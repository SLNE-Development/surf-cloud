package dev.slne.surf.cloud.core.common.util.encryption

import java.io.Serial

class CryptException(throwable: Throwable) : Exception(throwable) {
    companion object {
        @Serial
        const val serialVersionUID: Long = 1L
    }
}