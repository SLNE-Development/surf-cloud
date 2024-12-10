package dev.slne.surf.cloud.api.common.util

inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    fold(onSuccess = { this }, onFailure = { Result.failure(transform(it)) })

inline fun <T> Result<T>.getOrMapAndThrow(transform: (Throwable) -> Throwable): T = mapFailure(transform).getOrThrow()

