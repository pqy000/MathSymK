package io.github.ezrnest.structure

import io.github.ezrnest.function.BiMathOperator
import io.github.ezrnest.util.ModelPatterns
import kotlin.math.abs


/**
 * Semigroup is the base of almost all the algebraic structure in abstract algebra.
 *
 *
 * A semigroup is composed of a set of elements and an operation defined in the set.
 * Assume the operation is "*".
 *
 *  * It is *associative*: (a*b)*c = a*(b*c)
 *
 *
 * @author liyicheng
 * 2018-02-27 17:09
 *
 */
interface Semigroup<T> : EqualPredicate<T>, BiMathOperator<T> {
    /*
    Re-designed by lyc on 2024/8/11
     */

    /**
     * Determines whether this semigroup contains the specified element.
     */
    operator fun contains(element: T): Boolean

    /**
     * The operation of this semigroup, which is associative.
     */
    override fun apply(x: T, y: T): T
}


interface Monoid<T> : Semigroup<T> {

    /**
     * Gets the identity element of this semigroup.
     *
     * @return the identity element of this monoid.
     */
    val identity: T

}

/**
 * A group is an algebraic structure consisting of a set of elements and an operation.
 *
 *
 * Assume the operation is "*", then
 *
 *  * It is *associative*: `(a*b)*c = a*(b*c)`
 *  * There exists an identity element `e` that: `e*a = a*e = a`
 *  * For every element `a`, there exists an inverse element `a^-1` such that `a*a^-1 = a^-1*a = e`
 *
 *
 *
 * Note that most of the methods defined on the interface are optional and it can throw an UnsupportedOperation
 * if necessary.
 *
 * @author LI Yicheng,  2018-02-27 17:32
 */
interface Group<T> : Monoid<T> {
    /*
    Re-designed by lyc on 2024/8/11
     */


    /**
     * Gets the inverse of the element.
     */
    fun inverse(element: T): T

    /**
     * Determines whether this group is commutative.
     */
    val isCommutative: Boolean

}


/**
 * An Abelian semigroup calculator defines an associative and commutative operation [add], which
 * we usually denote as `+`.
 *
 * The elements form a semi-group with respect to this calculator.
 *
 * This interface is generally isomorphic to [Semigroup] where the operation is commutative,
 * but the method names differ and extra operator functions are provided.
 *
 * @author liyicheng
 * 2021-05-07 18:36
 *
 * @see Semigroup
 */
interface AddSemigroup<T> : EqualPredicate<T> {
    //Created by lyc at 2021-05-03 22:09
    //Updated by lyc at 2024-08-11

    operator fun contains(x: T): Boolean

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
    fun sum(elements: List<T>): T {
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
interface AddMonoid<T> : AddSemigroup<T> {
    //Updated by lyc at 2024-08-11

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


//    val T.isZero: Boolean
//        get() = isZero(this)

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
        return ModelPatterns.binaryProduce(n, zero, x, this::add)
    }

    /**
     * Returns the sum of all the elements.
     */
    override fun sum(elements: List<T>): T {
        return elements.fold(zero, this::add)
    }


//    /**
//     * Returns the class of the number.
//     */
//    override val numberClass: Class<T>
//        get() = zero.javaClass
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
 * This interface is generally isomorphic to [Group] where the operation is commutative,
 * but the method names differ and extra operator functions are provided.
 *
 * @author liyicheng 2021-05-07 18:45
 */
interface AddGroup<T> : AddMonoid<T> {
    //Updated by lyc at 2024-08-11

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
        val t = ModelPatterns.binaryProduce(abs(n), x) { a, b -> add(a, b) }
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

/**
 * A semigroup calculator defining an associative operation [multiply], which
 * we usually denote as `*`.
 *
 * The elements form a semigroup with respect to this calculator.
 *
 * This interface is generally isomorphic to [Semigroup],
 * but the method names differ and extra operator functions are provided.
 *
 * @author liyicheng
 * 2021-05-07 19:02
 *
 * @see Semigroup
 */
interface MulSemigroup<T> : EqualPredicate<T> {
    //Updated by lyc at 2024-08-11


    operator fun contains(x: T): Boolean


    /**
     * Applies the operation of multiplication defined in the semigroup.
     */
    fun multiply(x: T, y: T): T

    /**
     * Determines whether the operation is commutative. It is false by default.
     */
    val isCommutative: Boolean
        get() = false

    fun power(x: T, n: Long): T {
        return ModelPatterns.binaryProduce(n, x) { a, b -> multiply(a, b) }
    }

    fun product(ps: List<T>): T {
        return ps.reduce(this::multiply)
    }


    /**
     * Operator function of add for [T].
     * @see multiply
     */
    operator fun T.times(y: T): T = multiply(this, y)


    infix fun T.pow(n: Long): T = power(this, n)

}

fun <T> MulSemigroup<T>.power(x: T, n: Int): T {
    return power(x, n.toLong())
}


/**
 * A monoid model providing [one], the identity element, for an multiplicative semigroup calculator.
 *
 *
 * @author liyicheng 2021-05-07 19:02
 * @see Monoid
 */
interface MulMonoid<T> : MulSemigroup<T> {
    /*
     * Created by liyicheng at 2020-03-06 22:14
     */
    //Updated by lyc at 2024-08-11


    /**
     * The identity element, which we often denote as `1`.
     *
     * It satisfies that:
     *
     *     1 * x = x * 1 = x
     */
    val one: T

    /**
     * Determines whether `isEqual(one, x)`.
     */
    fun isOne(x: T) = isEqual(one, x)

    /**
     * Returns the result of multiplying [x] for [n] times, which we usually denote as `x^n`.
     *
     * It is defined as:
     *
     *     x^0 = 1
     *     x^(n+1) = (x^n) * x
     *
     *
     * @param n a non-negative integer
     */
    override fun power(x: T, n: Long): T {
        return if (n == 0L) {
            one
        } else {
            super.power(x, n)
        }
    }

    override fun product(ps: List<T>): T {
        return ps.fold(one, this::multiply)
    }

//    /**
//     * Returns the class of the number.
//     */
//    override val numberClass: Class<T>
//        get() = one.javaClass
}


/**
 * A GroupCalculator defines a binary operation [multiply] (which we denote as `*`)
 * an [one] element (`1`) with respect to the operation and
 * a unary operation [reciprocal] (`^-1`) that satisfies:
 *
 * 1. Associative:
 *
 *     x * (y * z) = (x * y) * z
 *
 * 2. Commutative:
 *
 *     x * y = y * x
 *
 * 3. Identity:
 *
 *     x * 1 = 1 * x = 1
 *
 * 4. Inverse:
 *
 *     x * (x^-1) = 1
 *
 * We often denote `x * (y^-1)` as `x / y`, and the corresponding method is [divide].
 *
 * This interface is generally isomorphic to [Group],
 * but the method names differ and extra operator functions are provided.
 *
 *
 * @author liyicheng 2021-05-07 19:05
 * @see Group
 */
interface MulGroup<T> : MulMonoid<T> {
    //Updated by lyc at 2024-08-11

    /**
     * Returns `x^-1`, the multiplicative inverse of the element [x].
     *
     * Special note: Some implementation of this interface may throw an [ArithmeticException]
     * if there is no inverse for `x`. For example in FieldCalculator when `x = 0`.
     *
     * @param x an element
     */
    fun reciprocal(x: T): T

    /**
     * Returns the result of `x / y := x * y^{-1}`, which is equal to `multiply(x, inverse(y))`.
     * @return `x / y`
     */
    fun divide(x: T, y: T): T {
        return multiply(x, reciprocal(y))
    }


    /**
     * Returns the result of multiplying [x] for [n] times, which we usually denote as `x^n`.
     *
     * It is defined as:
     *
     *     x^0 = 1
     *     x^(n+1) = (x^n) * x
     *     x^(-n) = (x^n)^-1
     *
     * @param n an integer
     */
    override fun power(x: T, n: Long): T {
        if (n == 0L) {
            return one
        }
        val t = ModelPatterns.binaryProduce(abs(n), x) { a, b -> multiply(a, b) }
        return if (n > 0) {
            t
        } else {
            reciprocal(t)
        }
    }


    /**
     * Operator function of division.
     * @see divide
     */
    operator fun T.div(y: T): T = divide(this, y)

}


/**
 * Returns the conjugation of [a] by [x], which is defined to be
 *
 *      apply(apply(inverse(x), a), x)
 */
fun <T> Group<T>.conjugateBy(a: T, x: T) = eval { apply(apply(inverse(x), a), x) }

/**
 * Returns the commutator of [a] and [b]: `[a,b]` = `a^-1*b^-1*a*b`
 */
fun <T> Group<T>.commutator(a: T, b: T) = eval {
    apply(apply(apply(inverse(a), inverse(b)), a), b)
}

fun <T> AddGroup<T>.asGroup(): Group<T> {
    val m = this
    return object : Group<T> {
        override fun isEqual(x: T, y: T): Boolean {
            return m.isEqual(x, y)
        }

        override fun inverse(element: T): T {
            return m.negate(element)
        }

        override val isCommutative: Boolean
            get() = true
        override val identity: T
            get() = m.zero

        override fun contains(element: T): Boolean {
            return m.contains(element)
        }

        override fun apply(x: T, y: T): T {
            return m.add(x, y)
        }

//        override val numberClass: Class<T>
//            get() = m.numberClass
    }
}
