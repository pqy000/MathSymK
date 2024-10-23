package io.github.ezrnest.mathsymk.structure

/**
 * Describes the rational numbers, namely quotients, ℚ.
 */
interface Quotients<I, T> : OrderedField<T> {

    /**
     * Gets the model of the associated integers.
     */
    val integers: Integers<I>

    /**
     * The characteristic of quotient number field is zero.
     */
    override val characteristic: Long
        get() = 0

    /**
     * Returns the number value corresponding to the integer.
     */
    val Int.asQ: T
        get() = ofN(this.toLong())

    /**
     * Returns the number value corresponding to the integer.
     */
    val Long.asQ: T
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

    fun asInteger(x: T): I {
        if (isInteger(x)) return numerator(x)
        throw IllegalArgumentException("Not an integer: $x")
    }


    /**
     * Returns `base` raised to the integer power `p`.
     */
    fun power(base: T, p: I): T {
        val integers = integers
        if (isOne(base)) return one
        if (isZero(base)) {
            if (integers.isNegative(p)) {
                throw ArithmeticException("Cannot raise 0 to a non-positive power.")
            }
            return zero
        }
        run {
            val m1 = negate(one)
            if (isEqual(base, m1)) {
                return if (integers.isOdd(p)) m1 else one
            }
        }
        // only possible for p in int range
        with(integers) {
            val pInt = asInt(p)
            if (pInt <= 0) {
                if (isZero(base)) {
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

    /**
     * Returns the floor of the quotient `x`, which is the largest integer less than or equal to the fraction.
     */
    fun floor(x: T): I {
        val n = numerator(x)
        val d = denominator(x)
        with(integers) {
            val (q, r) = divideAndRem(n, d)
            if (isNegative(n) && !isZero(r)) {
                return q - one
            }
            return q
        }
    }

    /**
     * Returns `floor(x)` and `x - floor(x)`.
     */
    fun floorAndRem(x: T): Pair<I, T> {
        val n = numerator(x)
        val d = denominator(x)
        with(integers) {
            val (q, r) = divideAndRem(n, d)
            if (isNegative(n) && !isZero(r)) {
                return q - one to of(r + d, d)
            }
            return q to of(r, d)
        }
    }

    /**
     * Returns the ceil of the quotient `x`, which is the smallest integer greater than or equal to the fraction.
     */
    fun ceil(x: T): I {
        val n = numerator(x)
        val d = denominator(x)
        with(integers) {
            val (q, r) = divideAndRem(n, d)
            if (isPositive(n) && !isZero(r)) {
                return q + one
            }
            return q
        }
    }

    /**
     * Returns `ceil(x)` and `x - ceil(x)`.
     */
    fun ceilAndRem(x: T): Pair<I, T> {
        val n = numerator(x)
        val d = denominator(x)
        with(integers) {
            val (q, r) = divideAndRem(n, d)
            if (isPositive(n) && !isZero(r)) {
                return q + one to of(r - d, d)
            }
            return q to of(r, d)
        }
    }

    /**
     * Returns an integer `n` and a fraction `r` such that `x = n * y + r` and `0 <= r < |y|`,
     * where the fraction `r` has the same sign as `y`.
     */
    fun intDivRem(x: T, y: T): Pair<I, T>{
        val (q,r) = floorAndRem(divide(x, y))
        return q to r * y
    }

    fun intDiv(x: T, y: T): I{
        return floor(divide(x, y))
    }

    fun intRem(x: T, y: T): T{
        return intDivRem(x, y).second
    }

}