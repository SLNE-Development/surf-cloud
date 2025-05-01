package dev.slne.surf.cloud.api.common.util

import io.netty.channel.unix.DomainSocketAddress
import io.netty.util.NetUtil
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Creates InetSocketAddress instance. Numeric IP addresses will be detected and
 * resolved without doing reverse DNS lookups.
 *
 * @param hostname ip-address or hostname
 * @param port port number
 * @param resolve when true, resolve given hostname at instance creation time
 * @return InetSocketAddress for given parameters
 */
fun createInetSocketAddress(hostname: String, port: Int, resolve: Boolean): InetSocketAddress {
    val inetAddressForIpString = createForIpString(hostname, port)
    if (inetAddressForIpString != null) {
        return inetAddressForIpString
    }
    return if (resolve) InetSocketAddress(hostname, port)
    else InetSocketAddress.createUnresolved(hostname, port)
}

/**
 * Creates InetSocketAddress that is always resolved. Numeric IP addresses will be
 * detected and resolved without doing reverse DNS lookups.
 *
 * @param hostname ip-address or hostname
 * @param port port number
 * @return InetSocketAddress for given parameters
 */
fun createResolvedInetSocketAddress(hostname: String, port: Int): InetSocketAddress =
    createInetSocketAddress(hostname, port, true)

/**
 * Creates unresolved InetSocketAddress. Numeric IP addresses will be detected and
 * resolved.
 *
 * @param hostname ip-address or hostname
 * @param port port number
 * @return InetSocketAddress for given parameters
 */
fun createUnresolvedInetSocketAddress(hostname: String, port: Int): InetSocketAddress =
    createInetSocketAddress(hostname, port, false)

/**
 * Parse unresolved InetSocketAddress. Numeric IP addresses will be detected and resolved.
 *
 * @param address ip-address or hostname
 * @param defaultPort is used if the address does not contain a port,
 * or if the port cannot be parsed in non-strict mode
 * @param strict if true throws an exception when the address cannot be parsed,
 * otherwise an unresolved {@link InetSocketAddress} is returned. It can include the case of the host
 * having been parsed but not the port (replaced by {@code defaultPort})
 * @return {@link InetSocketAddress} for given parameters, only numeric IP addresses will be resolved
 */
fun parseInetSocketAddress(address: String, defaultPort: Int, strict: Boolean = false): InetSocketAddress {
    var host = address
    var port = defaultPort
    val separatorIdx  = address.lastIndexOf(':')
    val ipV6HostSeparatorIdx = address.lastIndexOf(']')

    if (separatorIdx > ipV6HostSeparatorIdx) {
        if (separatorIdx == address.indexOf(':') || ipV6HostSeparatorIdx > -1) {
            host = address.substring(0, separatorIdx)
            val portStr = address.substring(separatorIdx + 1)
            if (portStr.isNotEmpty()) {
                if (portStr.chars().allMatch(Character::isDigit)) {
                    port = portStr.toInt()
                } else if (strict) {
                    throw IllegalArgumentException("Failed to parse a port from address: $address")
                }
            }
        } else if (strict) {
            throw IllegalArgumentException("Invalid IPv4 address $address")
        }
    }

    return createUnresolvedInetSocketAddress(host, port)
}

/**
 * Replaces an unresolved InetSocketAddress with a resolved instance in the case that
 * the passed address is a numeric IP address (both IPv4 and IPv6 are supported).
 *
 * @receiver inetSocketAddress socket address instance to process
 * @return processed socket address instance
 */
fun InetSocketAddress.replaceUnresolvedNumericIp(): InetSocketAddress {
    if (!isUnresolved) {
        return this
    }

    return createForIpString(hostString, port) ?: this
}

/**
 * Replaces an unresolved InetSocketAddress with a resolved instance in the case that
 * the passed address is unresolved.
 *
 * @receiver inetSocketAddress socket address instance to process
 * @return resolved instance with same host string and port
 */
fun InetSocketAddress.replaceWithResolved(): InetSocketAddress {
    if (!isUnresolved) {
        return this
    }

    val inetSocketAddress = replaceUnresolvedNumericIp()

    if (!inetSocketAddress.isUnresolved) {
        return inetSocketAddress
    }

    return InetSocketAddress(inetSocketAddress.hostString, inetSocketAddress.port)
}

/**
 * Update the provided address with the new host string.
 *
 * @param address the address supplier
 * @param host the new host string
 * @return the updated address
 */
fun updateInetSocketAddressHost(address: (() -> SocketAddress)?, host: String): InetSocketAddress {
    if (address == null) {
        return createUnresolvedInetSocketAddress(host, 0)
    }

    val socketAddress = address()
    check(socketAddress !is DomainSocketAddress) { "Cannot update DomainSocketAddress with host name [$host]" }

    if (socketAddress !is InetSocketAddress) {
        return createResolvedInetSocketAddress(host, 0)
    }

    return createUnresolvedInetSocketAddress(host, socketAddress.port)
}

fun updateInetSocketAddressPort(address: (() -> SocketAddress)?, port: Int): InetSocketAddress {
    if (address == null) {
        return createUnresolvedInetSocketAddress(NetUtil.LOCALHOST.hostAddress, port)
    }

    val socketAddress = address()
    check(socketAddress !is DomainSocketAddress) { "Cannot update DomainSocketAddress with port number [$port]" }

    if (socketAddress !is InetSocketAddress) {
        return createResolvedInetSocketAddress(NetUtil.LOCALHOST.hostAddress, port)
    }

    return createUnresolvedInetSocketAddress(socketAddress.address?.hostAddress ?: socketAddress.hostName, port)
}

fun attemptParsingIpString(address: String): InetAddress? =
    NetUtil.createByteArrayFromIpAddressString(address)?.run {
        if (size == 4) {
            Inet4Address.getByAddress(this)
        } else {
            Inet6Address.getByAddress(this)
        }
    }

private fun createForIpString(address: String, port: Int): InetSocketAddress? =
    attemptParsingIpString(address)?.run { InetSocketAddress(this, port) }