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

    fun of(n: I, d: I): T


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


    /**
     * Returns `base` raised to the integer power `p`.
     */
    fun power(base: T, p : I) : T{
        val integers = integers
        if(isOne(base)) return one
        if(isZero(base)){
            if(integers.isNegative(p)){
                throw ArithmeticException("Cannot raise 0 to a non-positive power.")
            }
            return zero
        }
        run{
            val m1 = negate(one)
            if(isEqual(base, m1)){
                return if(integers.isOdd(p)) m1 else m1
            }
        }
        // only possible for p in int range
        with(integers){
            val pInt = asInt(p)
            if(pInt <= 0){
                if(isZero(base)){
                    throw ArithmeticException("Cannot raise 0 to a non-positive power.")
                }
                val nume = power(denominator(base), -pInt)
                val deno = power(numerator(base), -pInt)
                return of(nume, deno)
            }
            val nume = power(numerator(base), pInt)
            val deno = power(denominator(base), pInt)
            return of(nume, deno)
        }
    }

}