package dev.slne.surf.cloud.api.common.util

import kotlinx.serialization.Serializable

@Serializable
sealed class Either<L, R> {

    abstract fun <C, D> mapBoth(
        leftMapper: (L) -> C,
        rightMapper: (R) -> D
    ): Either<C, D>

    abstract fun <T> map(leftMapper: (L) -> T, rightMapper: (R) -> T): T

    abstract val left: L?
    abstract val right: R?

    fun <T> mapLeft(mapper: (L) -> T): Either<T, R> = map({ left(mapper(it)) }, ::right)
    fun <T> mapRight(mapper: (R) -> T): Either<L, T> = map(::left) { right(mapper(it)) }

    fun leftOrThrow(): L {
        return left ?: throw NoSuchElementException("Either is Right, no Left value present")
    }

    fun rightOrThrow(): R {
        return right ?: throw NoSuchElementException("Either is Left, no Right value present")
    }

    fun swap(): Either<R, L> = map(::right, ::left)
    fun <L2> flatMap(leftMapper: (L) -> Either<L2, R>): Either<L2, R> = map(leftMapper, ::right)

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)
        fun <L, R> right(value: R): Either<L, R> = Right(value)
        fun <U> unwrap(either: Either<out U, out U>): U = either.map({ it }, { it })
    }

    @PublishedApi
    @Serializable
    internal class Left<L, R>(override val left: L) : Either<L, R>() {
        @Transient
        override val right: R? = null

        override fun <C, D> mapBoth(
            leftMapper: (L) -> C,
            rightMapper: (R) -> D
        ): Either<C, D> = Left(leftMapper(left))

        override fun <T> map(leftMapper: (L) -> T, rightMapper: (R) -> T): T = leftMapper(left)

        override fun toString(): String = "Either.Left($left)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Left<*, *>) return false

            if (left != other.left) return false

            return true
        }

        override fun hashCode(): Int {
            return left?.hashCode() ?: 0
        }
    }


    @Serializable
    @PublishedApi
    internal class Right<L, R>(override val right: R) : Either<L, R>() {
        @Transient
        override val left: L? = null

        override fun <C, D> mapBoth(
            leftMapper: (L) -> C,
            rightMapper: (R) -> D
        ): Either<C, D> = Right(rightMapper(right))

        override fun <T> map(leftMapper: (L) -> T, rightMapper: (R) -> T): T = rightMapper(right)

        override fun toString(): String = "Either.Right($right)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Right<*, *>) return false

            if (right != other.right) return false

            return true
        }

        override fun hashCode(): Int {
            return right?.hashCode() ?: 0
        }
    }
}

inline fun <L, R> Either<L, R>.ifLeft(action: (L) -> Unit): Either<L, R> {
    if (this is Either.Left) {
        action(left)
    }
    return this
}

inline fun <L, R> Either<L, R>.ifRight(action: (R) -> Unit): Either<L, R> {
    if (this is Either.Right) {
        action(right)
    }
    return this
}