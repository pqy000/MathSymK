package cn.mathsymk.structure

/**
 *
 */
interface Ring<T : Any> : AddGroup<T>, MulSemigroup<T> {


}

interface UnitRing<T : Any> : Ring<T>, MulMonoid<T> {

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

    override val numberClass: Class<*>
        get() = super<Ring>.numberClass

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
}
