package cn.mathsymk.model

import util.ModelPatterns
import kotlin.math.abs


/**
 * An abelian semigroup calculator defines an associative and commutative operation [add], which
 * we usually denote as `+`.
 *
 * The elements form a semi-group with respect to this calculator.
 *
 * This interface is generally isomorphic to [SemigroupCalculator] where the operation is commutative,
 * but the method names differ and extra operator functions are provided.
 *
 * @author liyicheng
 * 2021-05-07 18:36
 *
 * @see SemigroupCalculator
 */
interface AbelSemigroupModel<T> : EqualPredicate<T> {
    //Created by lyc at 2021-05-03 22:09

    /**
     * Applies the operation of addition defined in the semigroup.
     *
     * The operation is associative and commutative:
     *
     *     x + (y + z) = (x + y) + z := x + y + z
     *     x + y = y + x
     */
    fun add(x: T, y: T): T

    operator fun T.plus(y: T): T = add(this, y)

    /**
     * Returns the result of adding [x] for [n] times, which we usually denote as `n * x` or simply `nx`.
     *
     * It is defined as:
     *
     *     1 * x = x
     *     (n+1) * x = (n * x) + x
     *
     *
     * @param n a positive integer
     */
    fun multiplyLong(x: T, n: Long): T {
        require(n > 0) { "n > 0 is required" }
        return ModelPatterns.binaryProduce(n - 1, x, x, this::add)
    }

    /**
     * Returns the sum of all the elements in the given list.
     *
     * @param elements elements to sum
     */
    fun sum(elements: Iterable<T>): T {
        return elements.reduce(this::add)
    }

    /**
     * Operator function for [T].
     * @see multiplyLong
     */
    operator fun Long.times(x: T): T = multiplyLong(x, this)

    /**
     * Operator function for [T].
     * @see multiplyLong
     */
    operator fun T.times(n: Long) = multiplyLong(this, n)


}


/**
 * A monoid calculator provides [zero], the identity element, for an abelian semigroup calculator.
 *
 *
 * @author liyicheng 2021-05-07 18:44
 */
interface AbelMonoidModel<T> : AbelSemigroupModel<T> {

    /**
     * The zero element, which we often denote as `0`.
     *
     * It satisfies that:
     *
     *     0 + x = x + 0 = x
     */
    val zero: T

    /**
     * Determines whether [x] is zero.
     *
     * This method is the same as `isEqual(zero, x)`.
     */
    fun isZero(x: T) = isEqual(zero, x)

    /**
     * Returns the result of adding [x] for [n] times, which we usually denote as `n * x` or simply `nx`.
     *
     * It is defined as:
     *
     *     0 * x = zero
     *     (n+1) * x = (n * x) + x
     *
     *
     * @param n a non-negative integer
     */
    override fun multiplyLong(x: T, n: Long): T {
        require(n >= 0) { "n >= 0 is required" }
        return ModelPatterns.binaryProduce(n,zero, x, this::add)
    }

    /**
     * Returns the sum of all the elements.
     */
    override fun sum(elements: Iterable<T>): T {
        return elements.fold(zero, this::add)
    }


    /**
     * Returns the class of the number.
     */
    override val numberClass: Class<T>
        @Suppress("UNCHECKED_CAST")
        get() = (zero as Any).javaClass as Class<T>
}

/**
 * A GroupCalculator defines a binary operation [add] (which we denote as `+`)
 * an [zero] element (`0`) with respect to the operation and
 * a unary operation [negate] (`-`) that satisfies:
 *
 * 1. Associative:
 *
 *     x + (y + z) = (x + y) + z
 *
 * 2. Commutative:
 *
 *     x + y = y + x
 *
 * 3. Identity:
 *
 *     x + 0 = 0 + x = 0
 *
 * 4. Inverse:
 *
 *     x + (- x) = 0
 *
 * We often denote `x + (-y)` as `x - y`, and the corresponding method is [subtract].
 *
 * This interface is generally isomorphic to [GroupModel] where the operation is commutative,
 * but the method names differ and extra operator functions are provided.
 *
 * @author liyicheng 2021-05-07 18:45
 */
interface AbelGroupModel<T> : AbelMonoidModel<T> {

    /**
     * Returns `-x`, the inverse with respect to addition of [x].
     */
    fun negate(x: T): T

    /**
     * Returns `x - y`, which is defined as `x + (-y)`.
     */
    fun subtract(x: T, y: T): T {
        return add(x, negate(y))
    }

    /**
     * Returns the result of adding [x] for [n] times, which we usually denote as `n * x` or simply `nx`.
     *
     * It is defined as:
     *
     *     0 * x = zero
     *     (n+1) * x = (n * x) + x
     *     (-n) * x = - (n * x)
     *
     *
     * @param n an integer
     */
    override fun multiplyLong(x: T, n: Long): T {
        if (n == 0L) {
            return zero
        }
        val t = ModelPatterns.binaryProduce(abs(n), x, this::add)
        return if (n > 0) {
            t
        } else {
            negate(t)
        }
    }


    /**
     * Operator function inverse.
     * @see negate
     */
    operator fun T.unaryMinus(): T = negate(this)

    /**
     * Operator function subtract.
     * @see subtract
     */
    operator fun T.minus(y: T): T = subtract(this, y)
}
