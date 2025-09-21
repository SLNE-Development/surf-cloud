package dev.slne.surf.cloud.core.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress

class ServerAddressTest {

    @Test
    fun `parses host and port from string`() {
        val serverAddress = "example.com:1234".toServerAddress()

        assertEquals("example.com", serverAddress.host)
        assertEquals(1234, serverAddress.port)
    }

    @Test
    fun `uses default port when not provided`() {
        val serverAddress = "example.com".toServerAddress()

        assertEquals("example.com", serverAddress.host)
        assertEquals(25566, serverAddress.port)
    }

    @Test
    fun `converts unicode host to ascii`() {
        val serverAddress = "☃.com:4040".toServerAddress()

        assertEquals("xn--n3h.com", serverAddress.host)
        assertEquals(4040, serverAddress.port)
    }

    @Test
    fun `throws when parsing invalid server address`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            "invalid-port:abc".toServerAddress()
        }

        assertEquals("Invalid server address: invalid-port:abc", exception.message)
    }

    @Test
    fun `host falls back to empty string when ascii conversion fails`() {
        val host = "a".repeat(64)
        val serverAddress = ServerAddress(host, 25565)

        assertEquals("", serverAddress.host)
        assertEquals(25565, serverAddress.port)
    }

    @Test
    fun `creates InetSocketAddress`() {
        val serverAddress = "example.net:1337".toServerAddress()

        val socketAddress = InetSocketAddress(serverAddress)

        assertEquals("example.net", socketAddress.hostString)
        assertEquals(1337, socketAddress.port)
        assertEquals(InetSocketAddress("example.net", 1337), socketAddress)
    }
}