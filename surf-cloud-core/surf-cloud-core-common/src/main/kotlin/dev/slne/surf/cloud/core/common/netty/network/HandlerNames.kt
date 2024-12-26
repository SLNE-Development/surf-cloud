package dev.slne.surf.cloud.core.common.netty.network

object HandlerNames {
    const val SSL_HANDLER: String = "ssl_handler"
    const val DECOMPRESS: String = "decompress"
    const val COMPRESS: String = "compress"
    const val DECODER: String = "decoder"
    const val ENCODER: String = "encoder"
    const val INBOUND_CONFIG: String = "inbound_config"
    const val OUTBOUND_CONFIG: String = "outbound_config"
    const val SPLITTER: String = "splitter"
    const val PREPENDER: String = "prepender"
    const val DECRYPT: String = "decrypt"
    const val ENCRYPT: String = "encrypt"
    const val UNBUNDLER: String = "unbundler"
    const val BUNDLER: String = "bundler"
    const val PACKET_HANDLER: String = "packet_handler"
    const val TIMEOUT: String = "timeout"
    const val LEGACY_QUERY: String = "legacy_query"
    const val LATENCY: String = "latency"
    const val RESPONDING_PACKET_SEND: String = "responding_packet_send"
}