package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import io.netty.buffer.ByteBuf
import kotlin.reflect.full.companionObjectInstance

typealias SealedVariantCodecProviderBufComplex<T> = SealedVariantCodecProvider.Complex<ByteBuf, T>
typealias SealedVariantCodecProviderBufSimple<T> = SealedVariantCodecProvider.Simple<ByteBuf, T>

sealed interface SealedVariantCodecProvider<B, T : Any> {
    val id: Int

    interface Complex<B, T : Any> : SealedVariantCodecProvider<B, T> {
        fun localCodec(parent: StreamCodec<B, T>): StreamCodec<B, out T>
    }

    interface Simple<B, T : Any> : SealedVariantCodecProvider<B, T> {
        @Suppress("PropertyName")
        val STREAM_CODEC: StreamCodec<B, out T>
    }

    companion object {
        @InternalApi
        data class Provider<B, T : Any>(
            val id: Int,
            val codecSupplier: (parent: StreamCodec<B, T>) -> StreamCodec<B, out T>
        )

        @InternalApi
        fun <B, T : Any> findProvider(klass: Class<out T>): Provider<B, T>? {
            val kClass = klass.kotlin
            fun providerFromInstance(instance: Any): Provider<B, T>? {
                if (instance is Simple<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val simpleInstance = instance as Simple<B, T>
                    return Provider(simpleInstance.id) { simpleInstance.STREAM_CODEC }
                } else if (instance is Complex<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val complexInstance = instance as Complex<B, T>
                    return Provider(complexInstance.id, complexInstance::localCodec)
                }

                return null
            }


            kClass.objectInstance?.let { providerFromInstance(it) }?.let { return it }
            kClass.companionObjectInstance?.let { providerFromInstance(it) }?.let { return it }

            return null
        }
    }
}