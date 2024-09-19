package io.github.ezrnest.model

import io.github.ezrnest.model.Fraction.Companion.ONE
import io.github.ezrnest.model.Fraction.Companion.ZERO
import io.github.ezrnest.util.exceptions.ExceptionUtil
import io.github.ezrnest.model.struct.FieldModel
import io.github.ezrnest.numberTheory.NTFunctions
import io.github.ezrnest.structure.Quotients
import io.github.ezrnest.util.ArraySup
import io.github.ezrnest.util.MathUtils

import java.io.Serializable
import java.lang.Math.multiplyExact
import java.lang.Math.addExact
import java.lang.Math.subtractExact
import java.util.regex.Pattern
import kotlin.math.*


/**
 * This class represents a fraction, which is a number that can be expressed as the quotient of two integers.
 *
 * A fraction is composed of two co-prime integers [nume] and the [deno].
 * The sign of the numerator represents the sign of this fraction and
 * the denominator is always positive.
 *
 * The class provides basic arithmetic operations for fractions, such as addition, subtraction, multiplication, and division.
 * All the computation results are exact and `ArithmeticException` will be thrown if overflow occurs.
 *
 *
 * @author lyc
 */
@JvmRecord
data class Fraction internal constructor(
    /**
     * Gets the numerator of this Fraction, the numerator may be positive, zero or negative.
     * @return numerator
     */
    val nume: Long,
    /**
     * Gets the denominator of this Fraction, it is a positive integer.
     * @return denominator
     */
    val deno: Long,
) : FieldModel<Fraction>, Comparable<Fraction>, Serializable {

    /**
     * Determines whether this fraction is an integer.
     * @return `true` if the fraction is an integer.
     */
    val isInteger: Boolean
        get() = deno == 1L


    val isNegative: Boolean
        get() = nume < 0

    val isPositive: Boolean
        get() = nume > 0


    override val isZero: Boolean
        get() = (nume == 0L)

    override val isInvertible: Boolean
        get() = !isZero

    /**
     * The sign number of this fraction, `1` if `this > 0`, `0` if `this = 0` and `-1` if `this < 0`.
     */
    val signum: Int
        get() = nume.compareTo(0)

    /**
     * Gets the absolute value of the [nume].
     */
    val numeratorAbs: Long
        get() = nume.absoluteValue


    init {
        require(deno != 0L) { "The denominator is zero!" }
    }

    /**
     * Returns the int value corresponding to this fraction if it can be expressed using an int,
     * otherwise an exception will be thrown.
     */
    fun toInt(): Int {
        if (!isInteger) {
            throw ArithmeticException("Not an integer: $this")
        }
        return Math.toIntExact(nume)
    }

    /**
     * Returns the long value corresponding to this fraction if it can be expressed using an int,
     * otherwise an exception will be thrown.
     */
    fun toLong(): Long {
        if (!isInteger) {
            throw ArithmeticException("Not an integer: $this")
        }
        return nume
    }


    fun toDouble(): Double {
        return nume.toDouble() / deno.toDouble()
    }

    /**
     * Returns the absolute value of this fraction.
     */
    fun abs(): Fraction {
        return if (isPositive) this else -this
    }

    /**
     * Returns `this * n`.
     */
    override fun timesLong(n: Long): Fraction {
        if (n == 0L) {
            return ZERO
        }

        //to prevent potential overflow,simplify num and den
        val dAn = gcdReduce(n, deno)
        val nNum = multiplyExact(dAn[0], nume)
        //new numerator
        return Fraction(nNum, dAn[1])
    }

    /**
     * Returns`this / n`
     * @param n a non-zero number
     * @return `this / n`
     * @throws IllegalArgumentException if `n == 0`.
     */
    operator fun div(n: Long): Fraction {
        if (n == 0L) {
            throw IllegalArgumentException("Divide by zero :  / 0")
        }

        //to prevent potential overflow, simplify num and den
        val nAn = gcdReduce(nume, n)
        val nDen = multiplyExact(nAn[1], deno)
        //new numerator
        return Fraction(nAn[0], nDen)
    }

    /**
     * Returns `-this`.
     */
    override fun unaryMinus(): Fraction {
        return if (this.isZero) {
            ZERO
        } else {
            Fraction(Math.negateExact(nume), deno)
        }
    }

    /**
     * Returns the multiplicative inverse of this fraction, namely `1/this`.
     * @throws IllegalArgumentException if `this == 0`.
     */
    override fun inv(): Fraction {
        if (this.isZero) {
            throw ArithmeticException("Zero to reciprocal")
        }
        return adjustSign(deno, nume)
    }

    /**
     * Returns the reciprocal of this fraction, namely `1/this`.
     * @see inv
     */
    fun reciprocal(): Fraction = inv()

    /**
     * Returns `this * y`.
     * @param y another fraction
     */
    override fun times(y: Fraction): Fraction {
        if (isZero || y.isZero) {
            return ZERO
        }

        val (n1, d2) = gcdReduce(this.nume, y.deno)
        val (n2, d1) = gcdReduce(y.nume, this.deno)
        return Fraction(multiplyExact(n1, n2), multiplyExact(d1, d2))
    }

    /**
     * Returns `this / y`.
     * @param y a non-zero fraction
     * @throws ArithmeticException if `y == 0`.
     */
    override fun div(y: Fraction): Fraction {
        if (y.isZero) {
            ExceptionUtil.dividedByZero()
        }
        if (this.isZero) {
            return ZERO
        }
        //exchange y's numerator and denominator .
        val (n1, d2) = gcdReduce(this.nume, y.nume)
        val (n2, d1) = gcdReduce(y.deno, this.deno)
        return adjustSign(multiplyExact(n1, n2), multiplyExact(d1, d2))
    }

    /**
     * Returns `this + num`.
     */
    operator fun plus(num: Long): Fraction {
        val nNum = addExact(nume, multiplyExact(num, deno))
        if (nNum == 0L) {
            return ZERO
        }
        return Fraction(nNum, deno)
    }


    /**
     * Returns `this - num`.
     */
    operator fun minus(num: Long): Fraction {
        val nNum = subtractExact(nume, multiplyExact(num, deno))
        if (nNum == 0L) {
            return ZERO
        }
        return Fraction(nNum, deno)
    }

    /**
     * Returns`this + y`.
     */
    override fun plus(y: Fraction): Fraction {
        // a/b + c/d =
        // (a * lcm / b) / lcm + (c * lcm / d) / lcm = (a * d / gcd + c * b / gcd) / lcm
        // (a * d1 + c * b1) / lcm
        val gcd = NTFunctions.gcd(deno, y.deno)
        val b1 = deno / gcd
        val d1 = y.deno / gcd
        val lcm = multiplyExact(b1, y.deno) // b1 * y.deno
//        val num = this.nume * d1 + y.nume * b1
        val num = addExact(multiplyExact(this.nume, d1), multiplyExact(y.nume, b1))
        return of(num, lcm)
    }


    /**
     * Returns `this - y`.

     */
    override fun minus(y: Fraction): Fraction {
        // a/b + c/d =
        // (a * lcm / b) / lcm - (c * lcm / d) / lcm = (a * d / gcd - c * b / gcd) / lcm
        // (a * d1 - c * b1) / lcm
        val gcd = NTFunctions.gcd(deno, y.deno)
        val b1 = deno / gcd
        val d1 = y.deno / gcd
//        val lcm = b1 * y.deno
//        val num = this.nume * d1 - y.nume * b1
        val lcm = multiplyExact(b1, y.deno)
        val num = subtractExact(multiplyExact(this.nume, d1), multiplyExact(y.nume, b1))
        return of(num, lcm)
    }

//    /**
//     * Returns the remainder of this fraction divided by the specified `y` to integer value.
//     * This method is equivalent to [remainder].
//     *
//     * @see divideToIntegerValue
//     * @see remainder
//     */
//    operator fun rem(y: Fraction) = remainder(y)


    /**
     * Returns the `n`-th power of this.
     *
     * **Attention:** this method does NOT check underflow or overflow.
     *
     * @return `this^n`
     * @throws ArithmeticException if `this == 0` and `n <=0`
     */
    fun pow(n: Int): Fraction {
        if (isZero) {
            return if (n == 0) {
                ExceptionUtil.zeroExponent()
            } else {
                ZERO
            }
        }
        if (n == 0) {
            return ONE
        }
        val p = abs(n)
        val nume: Long = MathUtils.powExact(nume, p)
        val deno: Long = MathUtils.powExact(deno, p)
        return if (n > 0) {
            Fraction(nume, deno)
        } else {
            adjustSign(deno, nume)
        }
    }


    /**
     * Returns the square of this fraction, which is equivalent to `this * this`.
     * @return `this^2`
     */
    fun squared(): Fraction {
        return if (isZero) ZERO else Fraction(multiplyExact(nume, nume), multiplyExact(deno, deno))
    }


    /**
     * Returns the largest (closest to positive infinity) integer value that is
     * less than or equal to this fraction.
     */
    fun floor(): Long {
        return Math.floorDivExact(nume, deno)
    }

    /**
     * Returns the smallest (closest to negative infinity) value that is
     * greater than or equal to the argument and is equal to a mathematical integer. Special cases:
     * If the argument value is already equal to a mathematical integer, then the result is the same as the argument.
     */
    fun ceil(): Long {
        if (isInteger) {
            return nume
        }
        return Math.ceilDivExact(nume, deno)
    }

    /**
     * Returns a `Long` whose value is the integer part of the quotient `(this / divisor)` rounded down.
     *
     * For example, `(7/2).divideToIntegerValue(3/2) = 2`.
     *
     * @return The integer part of `this / divisor`.
     * @throws ArithmeticException if `divisor==0`
     */
    fun divideToInteger(divisor: Fraction): Long {
        if (isZero) {
            return 0
        }
        val re = this / divisor
        return re.floor()
    }

    /**
     * Divides this by the specified `divisor` to integer value and returns the remainder.
     *
     * For example, `(7/2).divideToIntAndRemainder(3/2) = (2, 1/2)`.
     *
     * @return A pair of `Long` and `Fraction` where the first element is the integer part of `this / divisor`
     * @throws ArithmeticException if `divisor==0`
     */
    fun divideToIntAndRemainder(divisor: Fraction): Pair<Long, Fraction> {
        val q = this.divideToInteger(divisor)
        val r = this - q * divisor
        return q to r
    }

//    /**
//     * Returns the remainder of this fraction divided by the specified `divisor` to integer value.
//     *
//     * @throws ArithmeticException if `divisor==0`
//     * @see divideToIntegerValue
//     * @see divideAndRemainder
//     */
//    fun remainder(divisor: Fraction): Fraction {
//        return divideAndRemainder(divisor).second
//    }


    /**
     * Returns the String representation of this fraction.
     */
    override fun toString(): String {
        if (deno == 1L) {
            return nume.toString()
        }
        return "$nume/$deno"
    }

    /**
     * Returns a String representation of this fraction, adding brackets surrounding the fraction if necessary,
     * such as `(1/2)` or `(-2/3)`.
     *
     *
     * This method can be used to eliminate confusion when this fraction is a part of an expression.
     */
    fun toStringWithBracket(): String {
        if (deno == 1L) {
            return nume.toString()
        }
        return "($nume/$deno)"
    }

    /**
     * Generates the latex string of this fraction, which is like `\frac{1}{2}` or `-\frac{3}{8}`.
     */
    fun toLatexString(): String {
        if (deno == 1L) {
            return nume.toString()
        }
        return buildString {
            if (isNegative) {
                append('-')
            }
            append("\\frac{").append(nume.absoluteValue).append("}{").append(deno).append('}')
        }
    }


    /**
     * Compares this with `other` fraction and returns:
     * - `-1` if `this < other`
     * - `0` if `this == other`
     * - `1` if `this > other`
     *
     */
    override fun compareTo(other: Fraction): Int {
//        val diff = nume * other.deno - other.nume * deno
        val gcd = NTFunctions.gcd(deno, other.deno)
        val b1 = deno / gcd
        val d1 = other.deno / gcd
        val diff = subtractExact(multiplyExact(this.nume, d1), multiplyExact(other.nume, b1))
        return diff.sign
    }


    companion object {


        /**
         * A Fraction representing `0` with zero as numerator ,
         * one as denominator and zero for sign number.
         */
        @JvmField
        val ZERO = Fraction(0, 1)

        /**
         * A Fraction representing `1`.
         */
        @JvmField
        val ONE = Fraction(1, 1)

        /**
         * A Fraction representing `-1`
         */
        @JvmField
        val NEGATIVE_ONE = Fraction(-1, 1)

        @JvmField
        val TWO = Fraction(2, 1)

        @JvmField
        val HALF = Fraction(1, 2)


        private fun gcdReduce(num: Long, den: Long): LongArray {
            val re = LongArray(2)
            val g = NTFunctions.gcd(num, den)
            re[0] = num / g
            re[1] = den / g
            return re
        }

        private fun adjustSign(num: Long, den: Long): Fraction {
            return if (den < 0) {
                Fraction(Math.negateExact(num), Math.negateExact(den))
            } else {
                Fraction(num, den)
            }
        }



        /**
         * Returns a fraction from a long.
         */
        @JvmStatic
        fun of(number: Long): Fraction {
            return when (number) {
                0L -> ZERO
                1L -> ONE
                -1L -> NEGATIVE_ONE
                else -> Fraction(number, 1)
            }
        }

        @JvmStatic
        fun of(number: Int): Fraction {
            return of(number.toLong())
        }

        /**
         * Returns a fraction representing the value of `numerator/denominator`
         * with reduction.
         *
         * @param numerator the numerator of the fraction
         * @param denominator the denominator of the fraction, non-zero
         * @return a new fraction
         */
        @JvmStatic
        fun of(numerator: Long, denominator: Long): Fraction {
            require(denominator != 0L)
            if (numerator == 0L) {
                return ZERO
            }
            val nAd = gcdReduce(numerator, denominator)
            return adjustSign(nAd[0], nAd[1])
        }

        /**
         * Returns a fraction representing the value of `numerator/denominator`
         * with reduction.
         *
         * @param numerator the numerator of the fraction
         * @param denominator the denominator of the fraction, non-zero
         * @return a new fraction
         */
        operator fun invoke(numerator: Long, denominator: Long): Fraction {
            return of(numerator, denominator)
        }

        operator fun invoke(numerator: Int, denominator: Int): Fraction {
            return of(numerator.toLong(), denominator.toLong())
        }

        /**
         * Returns a fraction corresponding to the `number`.
         */
        operator fun invoke(number : Long): Fraction {
            return of(number)
        }

        operator fun invoke(number : Int): Fraction {
            return of(number)
        }



        private val maxPrecision = log10(java.lang.Long.MAX_VALUE.toDouble()).toInt() - 1

        /**
         * Return a fraction that is closet to the value of `d` but is smaller than `d`,
         * the returned fraction's both numerator and denominator are smaller than
         * 10<sup>`precision`</sup>.
         * @param d a number
         * @return a fraction
         */
        @JvmStatic
        fun ofDouble(d: Double, precision: Int): Fraction {
            if (precision <= 0 || precision > maxPrecision) {
                throw IllegalArgumentException("Bad precision:$precision")
            }
            if (d == 0.0) return ZERO

            var d1 = d.absoluteValue
            val deno = MathUtils.powExact(10L, precision - 1)
            //		deno*= 10L;
            while (d1 < deno) {
                d1 *= 10.0
            }
            val nume = d1.toLong()
            val nAd = gcdReduce(nume, deno)
            return Fraction(MathUtils.signum(d) * nAd[0], nAd[1])
        }

        /**
         * Returns the best approximate fraction of the double number. The numerator and
         * the denominator of the fraction are both smaller than `bound`.
         * @param x a number
         * @param bound the bound of the fraction, must be at least one.
         * @return a fraction that is the best approximate
         */
        @JvmStatic
        fun bestApproximate(x: Double, bound: Long = 10000_0000, conFraLenBound: Int = 16): Fraction {
            if (bound < 1) {
                throw IllegalArgumentException("Bad bound: $bound")
            }
            if (x == 0.0) {
                return ZERO
            }
            var x1 = x.absoluteValue
            var es = LongArray(4)
            var f: LongArray? = null
            var m: Long = 1
            var y = 1.0
            var i = 0
            while (true) {
                val reminder = x1 % y
                val l = ((x1 - reminder) / y).roundToLong()
                x1 = y
                y = reminder

                val t = m * l
                if (t > bound || t < 0 || java.lang.Double.isNaN(y)) {
                    break
                }
                m = t
                es = ArraySup.ensureCapacityAndAdd(es, l, i)
                val ft = computeContinuousFraction0(es, i)
                if (max(ft[0], ft[1]) > bound || ft[0] < 0 || ft[1] < 0) {
                    break
                }
                i++
                f = ft
                if (i >= conFraLenBound) {
                    break
                }
            }
            return if (f == null) {
                ZERO
            } else Fraction(MathUtils.signum(x) * f[0], f[1])
        }

        /**
         * Reduces a positive number to a continuous fraction series.
         */
        fun continuousFractionReduce(x: Double, length: Int = 8): LongArray {
            require(x > 0)
            require(length > 0)
            var x1 = x
            val es = LongArray(length)
            var y = 1.0
            var i = 0
            while (i < length) {
                val reminder = x1 % y
                val l = Math.round((x1 - reminder) / y)
                x1 = y
                y = reminder
                es[i] = l
                i++
            }
            return es
        }

        @JvmStatic
        fun continuousFraction(x: Double, len: Int): LongArray {
            return continuousFractionReduce(x, len)
        }

        @JvmStatic
        fun continuousFraction(x: Fraction): LongArray {
            var n = x.nume
            var d = x.deno
            val results = arrayListOf<Long>()
            while (d != 0L) {
                val q = n / d
                val r = n % d
                results += q
                n = d
                d = r
            }
            return results.toLongArray()
        }


        private fun computeContinuousFraction0(array: LongArray, index: Int = array.lastIndex): LongArray {
            var index1 = index
            var nume = array[index1]
            var deno: Long = 1

            index1--
            while (index1 > -1) {
//                val nn = array[index1] * nume + deno
                val nn = addExact(multiplyExact(array[index1], nume), deno)
                val nd = nume
                nume = nn
                deno = nd
                index1--
            }
            return longArrayOf(nume, deno)
        }

        /**
         * Computes the result of the continuous fraction stored in the array and
         * returns an array of the numerator and denominator.
         * @param index the highest element in the array to compute from
         */
        @JvmStatic
        private fun computeContinuousFraction(array: LongArray, index: Int = array.lastIndex): Fraction {
            val nd = computeContinuousFraction0(array, index)
            return of(nd[0], nd[1])
        }

        /**
         * Computes the result of the continuous fraction stored in the array.
         *
         * @param array the highest element in the array to compute from
         */
        @JvmStatic
        fun computeContinuousFractionAll(array: LongArray, length: Int = array.size): List<Fraction> {
            var b0 = 1L
            var b1 = array[0]
            var c0 = 0L
            var c1 = 1L
            val result = ArrayList<Fraction>(length)
            result += Fraction(b1, c1)
            for (i in 1 until length) {
//                val b2 = array[i] * b1 + b0
//                val c2 = array[i] * c1 + c0
                val b2 = addExact(multiplyExact(array[i], b1), b0)
                val c2 = addExact(multiplyExact(array[i], c1), c0)
                result += Fraction(b2, c2)
                b0 = b1
                b1 = b2
                c0 = c1
                c1 = c2
            }

            return result
        }

        /**
         * Identify the given expression
         */
        @JvmStatic
        val EXPRESSION_PATTERN: Pattern = Pattern.compile("([+\\-]?\\d+)(/(\\d+))?")

        // *([\\+\\-]?\\d+(\\/\\d+)?) * another replacement which
        val DECIMAL_PATTERN: Pattern = Pattern.compile("([+\\-]?\\d+)\\.(\\d+)")

        /**
         * Return a fraction representing the value of the given expression. The input can be either in fraction way
         * like `3/5` or in decimal way like `3.14`.
         * @param expr the expression
         */
        @JvmStatic
        fun of(expr: String): Fraction {
            var m = EXPRESSION_PATTERN.matcher(expr)
            if (m.matches()) {
                val n = m.group(1).toLong()
                return try {
                    val d = m.group(3).toLong()
                    of(n, d)
                } catch (e: Exception) {
                    of(n)
                }
            }
            m = DECIMAL_PATTERN.matcher(expr)
            if (m.matches()) {
                val n1 = m.group(1).toLong()
                val n2 = m.group(2)
                val digits = n2.length
                val deno = MathUtils.powExact(10L, digits)
//                val nume = n1 * deno + n2.toLong()
                val nume = addExact(multiplyExact(n1, deno), n2.toLong())
                return of(nume, deno)
            }

            throw NumberFormatException("Illegal Fraction:$expr")

        }


        /**
         * Return 1 number , 0 , -1 number if the given fraction is bigger than , equal to or smaller than `n`.
         * @param f a number as Fraction
         * @param n a number
         * @return a positive number , 0 , a negative number if the given fraction is bigger than , equal to or smaller than `n`.
         */
        @JvmStatic
        fun compareFraction(f: Fraction, n: Long): Int {
//            return java.lang.Long.signum(f.nume - f.deno * n)
            return f.nume.compareTo(multiplyExact(f.deno, n))
        }

        /**
         * Return 1 , 0 , -1 if the given fraction is bigger than , equal to or smaller than `n`.
         * @param f a number as Fraction
         * @param n a number
         * @return a positive number , 0 , a negative number if the given fraction is bigger than , equal to or smaller than `n`.
         */
        @JvmStatic
        fun compareFraction(f: Fraction, n: Double): Int {
            return f.toDouble().compareTo(n)
        }

        /**
         * Returns a fraction according to the given [signum], [numerator] and [denominator].
         */
        @JvmStatic
        fun of(signum: Int, numerator: Long, denominator: Long): Fraction {
            if (signum == 0) {
                return ZERO
            }
            return of(
                if (signum > 0) numerator else Math.negateExact(numerator),
                denominator
            )
        }


        @JvmStatic
        fun sum(vararg fractions: Fraction): Fraction {
            return sum(fractions.asList())
        }

        @JvmStatic
        fun sum(fractions: List<Fraction>): Fraction {
            when (fractions.size) {
                0 -> return ZERO
                1 -> return fractions[0]
                2 -> return fractions[0] + fractions[1]
            }

            val size = fractions.size
            val numes = LongArray(size)
            val denos = LongArray(size)

            for ((index, f) in fractions.withIndex()) {
                numes[index] = f.nume
                denos[index] = f.deno
            }

            val lcm = NTFunctions.lcm(*denos)
            var sum = 0L
            for (i in 0 until size) {
//                sum += numes[i] * (lcm / denos[i])
                sum = addExact(sum, multiplyExact(numes[i], lcm / denos[i]))
            }
            return of(sum, lcm)
        }


        @JvmStatic
        fun product(fractions: List<Fraction>): Fraction {
            val size = fractions.size
            when (size) {
                0 -> return ONE
                1 -> return fractions[0]
                2 -> return fractions[0] * fractions[1]
            }
            return fractions.reduce { acc, fraction -> acc * fraction }
        }


        /**
         * Gets the model for Fraction, which is a subclass of [Quotients].
         *
         *
         */
        @JvmStatic
        val asQuotient: FractionAsQuotients
            get() = FractionAsQuotients

    }

}

/**
 * A model for fractions, which is a subclass of [Quotients].
 */
object FractionAsQuotients : Quotients<Fraction> {
    override fun isEqual(x: Fraction, y: Fraction): Boolean {
        return x == y
    }

    override fun negate(x: Fraction): Fraction {
        return -x
    }

    override val zero: Fraction
        get() = ZERO
    override val one: Fraction
        get() = ONE

    fun frac(n: Long, d: Long): Fraction = Fraction.of(n, d)

    fun frac(n: Int, d: Int): Fraction = Fraction.of(n.toLong(), d.toLong())


    override fun contains(x: Fraction): Boolean {
        return true
    }

    override fun add(x: Fraction, y: Fraction): Fraction {
        return x + y
    }

    override fun subtract(x: Fraction, y: Fraction): Fraction {
        return x - y
    }

    override fun multiply(x: Fraction, y: Fraction): Fraction {
        return x * y
    }

    override fun divide(x: Fraction, y: Fraction): Fraction {
        return x / y
    }


    override fun reciprocal(x: Fraction): Fraction {
        return x.reciprocal()
    }

    override fun compare(o1: Fraction, o2: Fraction): Int {
        return o1.compareTo(o2)
    }

    override fun power(x: Fraction, n: Long): Fraction {
        return x.pow(n)
    }
}

fun Long.toFrac(): Fraction = Fraction.of(this)

fun Int.toFrac(): Fraction = Fraction.of(this.toLong())

operator fun Long.times(f: Fraction): Fraction = f * this


