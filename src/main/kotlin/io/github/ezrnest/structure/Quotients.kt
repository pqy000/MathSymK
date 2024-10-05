package io.github.ezrnest.structure

/**
 * Describes the rational numbers, namely quotients, ℚ.
 */
interface Quotients<I,T> : OrderedField<T> {

    /**
     * Gets the model of the associated integers.
     */
    val integers : Integers<I>

    /**
     * The characteristic of quotient number field is zero.
     */
    override val characteristic: Long
        get() = 0

    /**
     * Returns the number value corresponding to the integer.
     */
    val Int.asQ : T
        get() = ofN(this.toLong())

    /**
     * Returns the number value corresponding to the integer.
     */
    val Long.asQ : T
        get() = ofN(this)

    fun numerator(x: T): I

    fun denominator(x: T): I


    /**
     * Returns the number value corresponding to the integer `n`.
     */
    override fun ofN(n: Long): T {
        return super.ofN(n)
    }

    fun isInteger(x: T): Boolean

    fun asInteger(x: T): I{
        if(isInteger(x)) return numerator(x)
        throw IllegalArgumentException("Not an integer: $x")
    }

}