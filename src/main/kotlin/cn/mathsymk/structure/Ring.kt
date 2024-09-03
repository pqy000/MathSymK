package cn.mathsymk.structure

/**
 *
 */
interface Ring<T : Any> : AddGroup<T>, MulSemigroup<T> {

    /**
     * Zero, the additive identity.
     *
     * @return `0`
     */
    override val zero: T
}

interface UnitRing<T : Any> : Ring<T>, MulMonoid<T> {



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

    fun of(n: Long): T {
        return multiplyLong(one, n)
    }

    /**
     * Returns the result of exact division `a/b`.
     *
     * This method is optional for a unit ring.
     *
     * @throws ArithmeticException if `a` is not exactly divisible by `b`, or `b==0`.
     * @throws UnsupportedOperationException if this method is not supported.
     */
    fun exactDivide(a: T, b: T): T {
        throw UnsupportedOperationException()
    }

    /**
     * Exact division of two numbers.
     */
    operator fun T.div(y: T): T {
        return exactDivide(this, y)
    }
}

interface CommutativeRing<T : Any> : Ring<T>{
    override val isCommutative: Boolean
        get() = true
}

/**
 * A domain is a ring in which the product of two non-zero elements is non-zero.
 *
 * @author liyicheng
 * 2018-02-28 18:38
 */
interface Domain<T : Any> : Ring<T>

/**
 * An integral domain is a commutative ring in which the product of two non-zero elements is non-zero.
 */
interface IntegralDomain<T : Any> : Domain<T>, UnitRing<T>, CommutativeRing<T> {
}


/**
 * A division ring is a ring in which every non-zero element has a multiplicative inverse, but the multiplication is not necessarily commutative.
 */
interface DivisionRing<T : Any> : UnitRing<T>, MulGroup<T> {
    override fun isUnit(x: T): Boolean {
        return !isZero(x)
    }

    override fun exactDivide(a: T, b: T): T {
        return divide(a, b)
    }

    fun divideLong(x: T, n: Long): T {
        if (n == 0L) {
            throw ArithmeticException("Divided by zero")
        }
        return divide(x, of(n))
    }

    override fun T.div(y: T): T {
        return divide(this, y)
    }

    operator fun T.inv(): T {
        return reciprocal(this)
    }
}
