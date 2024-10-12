/**

-
## Package Documentation for `eu.thesimplecloud.api.protocol.buffer.decoder`
-
- The `eu.thesimplecloud.api.protocol.buffer.decoder` package provides a set of functional interfaces and utilities
- designed to facilitate the process of decoding values from binary data buffers. The primary purpose of this package
- is to offer a flexible and reusable approach for transforming raw binary data into meaningful objects, making it
- particularly useful for network protocols and data serialization tasks.
-
-
### Key Interfaces
-
-
#### 1. `DecodeFactory<B : ByteBuf, T>`
- The `DecodeFactory` interface represents a factory for decoding values from a buffer of type `B` (e.g., `ByteBuf`)
- to a value of type `T`. It provides a standardized way of extracting structured data from a raw byte buffer, making
- it reusable across different parts of the application. This interface also contains additional specialized
- sub-interfaces for specific decoding tasks:
-
- `DecodeBuilderFactory<B : ByteBuf, T>`: A factory that decodes a value while updating an existing object of type `T`.
-
- `DecodeLongFactory<B : ByteBuf>`: A factory for decoding `Long` values specifically from the buffer.
-
-
#### 2. `Decoder<B : ByteBuf, T>`
- The `Decoder` functional interface defines a simple, standardized method for decoding values of type `T` from a buffer
- of type `B`. It provides a convenient and lightweight way to implement decoding logic without the need for extensive
- boilerplate code.
-
-
### Usage
-
- The package's interfaces are intended to be used in network communication and serialization scenarios where data needs
- to be read from buffers and converted into domain-specific objects. The use of functional interfaces allows developers
- to define decoding logic in a concise and expressive manner, making the code more maintainable and easier to understand.
-
- The `DecodeFactory` interface also provides an intuitive approach to handling custom object creation, where complex types
- may require additional data to be properly constructed from the byte buffer.
-
-
### Example
-
-
```kotlin
```
- val decodeLongFactory: DecodeFactory\<ByteBuf, Long> = DecodeFactory.DecodeLongFactory { buffer ->
    -
    ```
    buffer.readLong()
    ```
    - }
-
- val longValue: Long = decodeLongFactory.invoke(buffer)
-
```
```
-
- In this example, `decodeLongFactory` is an instance of `DecodeLongFactory` that decodes a `Long` value from a given `ByteBuf`.
-
-
### Summary
-
- The `eu.thesimplecloud.api.protocol.buffer.decoder` package is focused on providing a unified approach for decoding various
- data types from byte buffers. By using functional interfaces, it offers flexibility and encourages the separation of concerns,
- making the codebase modular and easier to extend for future decoding needs.
*/


package dev.slne.surf.cloud.api.netty.protocol.buffer.decoder;