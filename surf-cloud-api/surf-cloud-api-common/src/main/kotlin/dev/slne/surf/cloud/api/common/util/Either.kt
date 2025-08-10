package dev.slne.surf.cloud.api.common.util

import dev.slne.surf.cloud.api.common.util.Either.Companion.left
import dev.slne.surf.cloud.api.common.util.Either.Companion.right
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a value of one of two possible types.
 * An [Either] is either a [Left] containing a value of type [L],
 * or a [Right] containing a value of type [R].
 *
 * This implementation is **neutral**: neither side implies success or failure.
 * It is useful whenever a value can be one of two distinct, equally valid types.
 *
 * @param L the type of the left value.
 * @param R the type of the right value.
 * @property left the left value if present, otherwise `null`.
 * @property right the right value if present, otherwise `null`.
 */
@Serializable
sealed class Either<L, R> {

    /** The left value if present, otherwise `null`. */
    abstract val left: L?

    /** The right value if present, otherwise `null`. */
    abstract val right: R?

    /**
     * Indicates whether this instance holds a left value.
     *
     * @return `true` if this is a [Left]; `false` otherwise.
     */
    val isLeft: Boolean get() = left != null

    /**
     * Indicates whether this instance holds a right value.
     *
     * @return `true` if this is a [Right]; `false` otherwise.
     */
    val isRight: Boolean get() = right != null

    /**
     * Returns the left value if present.
     *
     * @return the non-null left value.
     * @throws NoSuchElementException if no left value is present.
     */
    fun leftOrThrow(): L = left ?: throw NoSuchElementException("Either contains no left value")

    /**
     * Returns the right value if present.
     *
     * @return the non-null right value.
     * @throws NoSuchElementException if no right value is present.
     */
    fun rightOrThrow(): R = right ?: throw NoSuchElementException("Either contains no right value")

    /**
     * Returns the left value or `null` if absent.
     *
     * @return the left value, or `null` if this is a [Right].
     */
    fun leftOrNull(): L? = left

    /**
     * Returns the right value or `null` if absent.
     *
     * @return the right value, or `null` if this is a [Left].
     */
    fun rightOrNull(): R? = right

    /**
     * Returns the left value if present, otherwise computes a default.
     *
     * @param default a function invoked to produce a value when no left value is present.
     * @return the left value or the result of [default].
     */
    inline fun getLeftOrElse(default: () -> L): L = left ?: default()

    /**
     * Returns the right value if present, otherwise computes a default.
     *
     * @param default a function invoked to produce a value when no right value is present.
     * @return the right value or the result of [default].
     */
    inline fun getRightOrElse(default: () -> R): R = right ?: default()

    /**
     * Applies one of the given functions based on which side is present and returns its result.
     *
     * @param T the result type.
     * @param ifLeft function to apply when this is a [Left].
     * @param ifRight function to apply when this is a [Right].
     * @return the result of applying the appropriate function.
     */
    inline fun <T> fold(ifLeft: (L) -> T, ifRight: (R) -> T): T = when (this) {
        is Left -> ifLeft(left)
        is Right -> ifRight(right)
    }

    /**
     * Alias for [fold]. Transforms the contained value to a single common type.
     *
     * @param T the result type.
     * @param ifLeft function to apply when this is a [Left].
     * @param ifRight function to apply when this is a [Right].
     * @return the result of applying the appropriate function.
     */
    inline fun <T> map(ifLeft: (L) -> T, ifRight: (R) -> T): T = fold(ifLeft, ifRight)

    /**
     * Transforms both possible sides independently and returns a new [Either] with the mapped types.
     *
     * @param L2 the mapped left type.
     * @param R2 the mapped right type.
     * @param leftMapper function to apply to the left value.
     * @param rightMapper function to apply to the right value.
     * @return a new [Either] containing the transformed value.
     */
    inline fun <L2, R2> mapBoth(leftMapper: (L) -> L2, rightMapper: (R) -> R2): Either<L2, R2> =
        when (this) {
            is Left -> Left(leftMapper(left))
            is Right -> Right(rightMapper(right))
        }

    /**
     * Transforms the left value if present, keeping the right value as-is.
     *
     * @param L2 the mapped left type.
     * @param leftMapper function to apply to the left value.
     * @return a new [Either] with the transformed left type.
     */
    inline fun <L2> mapLeft(leftMapper: (L) -> L2): Either<L2, R> = when (this) {
        is Left -> Left(leftMapper(left))
        is Right -> Right(right)
    }

    /**
     * Transforms the right value if present, keeping the left value as-is.
     *
     * @param R2 the mapped right type.
     * @param rightMapper function to apply to the right value.
     * @return a new [Either] with the transformed right type.
     */
    inline fun <R2> mapRight(rightMapper: (R) -> R2): Either<L, R2> = when (this) {
        is Left -> Left(left)
        is Right -> Right(rightMapper(right))
    }

    /**
     * Swaps the sides of this instance: left becomes right and vice versa.
     *
     * @return a new [Either] with left and right values swapped.
     */
    fun swap(): Either<R, L> = fold(::right, ::left)

    /**
     * Executes [action] only when this is a [Left], then returns this instance for chaining.
     *
     * @param action the side-effect to perform on the left value.
     * @return this instance.
     */
    inline fun ifLeft(action: (L) -> Unit): Either<L, R> {
        if (this is Left) action(left)
        return this
    }


    /**
     * Executes [action] only when this is a [Right], then returns this instance for chaining.
     *
     * @param action the side-effect to perform on the right value.
     * @return this instance.
     */
    inline fun ifRight(action: (R) -> Unit): Either<L, R> {
        if (this is Right) action(right)
        return this
    }

    /**
     * Combines two [Either] values **only if** they are of the same side type.
     *
     * If both are [Left], [combineLeft] is used. If both are [Right], [combineRight] is used.
     * If they differ, this instance is returned unchanged.
     *
     * @param L2 the left type of [other].
     * @param R2 the right type of [other].
     * @param other the other [Either] to combine with.
     * @param combineLeft function to combine two left values into one.
     * @param combineRight function to combine two right values into one.
     * @return a combined [Either] when sides match; otherwise this instance.
     */
    inline fun <L2, R2> zipSame(
        other: Either<L2, R2>,
        combineLeft: (L, L2) -> L,
        combineRight: (R, R2) -> R
    ): Either<L, R> = when (this) {
        is Left if other is Left -> Left(combineLeft(this.left, other.left))
        is Right if other is Right -> Right(combineRight(this.right, other.right))
        else -> this
    }

    /**
     * Applies [leftMapper] when this is a [Left]; returns the right side unchanged otherwise.
     *
     * @param L2 the mapped left type.
     * @param leftMapper function transforming the left value into another [Either].
     * @return the mapped [Either] when left; otherwise the unchanged right side.
     */
    fun <L2> flatMap(leftMapper: (L) -> Either<L2, R>): Either<L2, R> = map(leftMapper, ::right)

    companion object {

        /**
         * Creates an [Either] containing the given left value.
         *
         * @param value the value to store on the left side.
         * @return an [Either] representing [value] as [Left].
         */
        fun <L, R> left(value: L): Either<L, R> = Left(value)

        /**
         * Creates an [Either] containing the given right value.
         *
         * @param value the value to store on the right side.
         * @return an [Either] representing [value] as [Right].
         */
        fun <L, R> right(value: R): Either<L, R> = Right(value)

        /**
         * Unwraps an [Either] whose left and right types are identical.
         *
         * @param either the [Either] instance to unwrap.
         * @param U the common value type.
         * @return the contained value regardless of side.
         */
        fun <U> unwrap(either: Either<out U, out U>): U = either.fold({ it }, { it })

        /**
         * Creates an [Either] from nullable left and right values. Exactly **one** must be non-null.
         *
         * @param left the candidate left value; may be `null`.
         * @param right the candidate right value; may be `null`.
         * @return an [Either] containing the single non-null value.
         * @throws IllegalStateException if both values are null or both are non-null.
         */
        fun <L, R> of(left: L?, right: R?): Either<L, R> = when {
            left != null && right == null -> Left(left)
            right != null && left == null -> Right(right)
            left == null -> error("Either must have a left or right value")
            else -> error("Either cannot have both left and right values")
        }
    }

    @PublishedApi
    @Serializable
    @SerialName("left")
    internal data class Left<L, R>(override val left: L) : Either<L, R>() {
        @Transient
        override val right: R? = null
        override fun toString(): String = "Either.Left($left)"
    }


    @Serializable
    @PublishedApi
    @SerialName("right")
    internal data class Right<L, R>(override val right: R) : Either<L, R>() {
        @Transient
        override val left: L? = null
        override fun toString(): String = "Either.Right($right)"
    }
}