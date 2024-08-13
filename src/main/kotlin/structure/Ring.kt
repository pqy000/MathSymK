package cn.mathsymk.structure

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

    override val numberClass: Class<T>
        get() = super<Ring>.numberClass

    fun of(n: Long): T {
        return multiplyLong(one, n)
    }

    fun exactDivide(a: T, b: T): T {
        throw UnsupportedOperationException()
    }
}

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

interface Field<T : Any> : UnitRing<T>, DivisionRing<T> {
    val characteristic: Long


}