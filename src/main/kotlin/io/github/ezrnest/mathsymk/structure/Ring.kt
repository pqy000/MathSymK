package io.github.ezrnest.mathsymk.structure

import io.github.ezrnest.mathsymk.model.Fraction

/**
 *
 */
interface Ring<T> : AddGroup<T>, MulSemigroup<T> {

    /**
     * Zero, the additive identity.
     *
     * @return `0`
     */
    override val zero: T

    override fun contains(x: T): Boolean
}

interface UnitRing<T> : Ring<T>, MulMonoid<T> {



    /**
     * One, the multiplicative identity.
     *
     * @return `1`
     */
    override val one: T

    /**
     * Determines whether the given element is a unit, namely invertible with respect to multiplication.
     *
     * This method is optional.
     *
     * @exception UnsupportedOperationException if this method is not supported.
     */
    fun isUnit(x: T): Boolean {
        throw UnsupportedOperationException()
    }

//    override val numberClass: Class<T>
//        get() = zero.javaClass

    /**
     * Returns element in this ring that represents `one * n`, namely adding `one` for `n` times.
     */
    fun ofN(n: Long): T {
        return multiplyN(one, n)
    }


    /**
     * Returns the result of exact division `a/b`, namely an element `c` such that `a = c * b`.
     *
     * This method is optional for a unit ring.
     *
     * @throws ArithmeticException if `a` is not exactly divisible by `b`, or `b==0`.
     * @throws UnsupportedOperationException if this method is not supported.
     */
    fun exactDiv(a: T, b: T): T {
        throw UnsupportedOperationException()
    }

    /**
     * Exact division of two numbers.
     */
    operator fun T.div(y: T): T {
        return exactDiv(this, y)
    }
}

fun <T> UnitRing<T>.exactDiv(a: T, n: Long): T {
    return exactDiv(a, ofN(n))
}

interface CommutativeRing<T> : Ring<T> {
    override val isCommutative: Boolean
        get() = true
}

/**
 * A domain is a ring in which the product of two non-zero elements is non-zero.
 *
 * @author liyicheng
 * 2018-02-28 18:38
 */
interface Domain<T> : Ring<T>

/**
 * An integral domain is a commutative ring in which the product of two non-zero elements is non-zero.
 */
interface IntegralDomain<T> : Domain<T>, UnitRing<T>, CommutativeRing<T>


/**
 * A division ring is a ring in which every non-zero element has a multiplicative inverse, but the multiplication is not necessarily commutative.
 */
interface DivisionRing<T> : UnitRing<T>, MulGroup<T> {
    override fun isUnit(x: T): Boolean {
        return !isZero(x)
    }

    override fun exactDiv(a: T, b: T): T {
        return divide(a, b)
    }

    fun divideN(x: T, n: Long): T {
        if (n == 0L) {
            throw ArithmeticException("Divided by zero")
        }
        return divide(x, ofN(n))
    }

    /**
     * Returns the element in this division that represents the given fraction, `of(q.nume)/of(q.deno)`.
     */
    fun ofQ(q : Fraction): T {
        return ofN(q.nume) / ofN(q.deno)
    }

    override fun T.div(y: T): T {
        return divide(this, y)
    }

    operator fun T.inv(): T {
        return reciprocal(this)
    }

    operator fun T.div(n : Long): T {
        return divideN(this, n)
    }

    operator fun Long.div(y: T): T {
        return ofN(this) / y
    }
}
