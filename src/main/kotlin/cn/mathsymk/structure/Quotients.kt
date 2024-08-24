package cn.mathsymk.structure

/**
 * Describes the rational numbers, or quotients, **Q**.
 */
interface Quotients<T : Any> : OrderedField<T> {

    /**
     * The characteristic of quotient number field is zero.
     */
    override val characteristic: Long
        get() = 0

    /**
     * Returns the number value corresponding to the integer.
     */
    val Int.v
        get() = of(this.toLong())

    /**
     * Returns the number value corresponding to the integer.
     */
    val Long.v
        get() = of(this)

}