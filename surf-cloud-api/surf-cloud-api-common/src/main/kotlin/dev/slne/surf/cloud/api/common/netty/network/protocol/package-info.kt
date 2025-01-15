/**
 * This package defines Netty packet types and utilities for network communication
 * within the Surf Cloud application.
 *
 * ### Overview:
 * - **Packet Types**:
 *   The package includes packets for transmitting various data types such as primitives,
 *   arrays, collections, and custom objects. These packets facilitate structured
 *   communication between network nodes.
 *
 * - **Packet Flow**:
 *   All packets are annotated with [SurfNettyPacket], specifying their unique identifier
 *   and flow direction, enabling seamless integration with the Netty framework.
 *
 * - **Serialization**:
 *   Packets implement efficient serialization and deserialization mechanisms using
 *   [SurfByteBuf], ensuring minimal overhead during transmission.
 *
 * ### Extensibility:
 * New packet types can be easily added to support additional data structures or custom
 * requirements. Implement the codec logic for serialization, annotate with
 * [SurfNettyPacket], and register the packet as needed.
 */
package dev.slne.surf.cloud.api.common.netty.network.protocol