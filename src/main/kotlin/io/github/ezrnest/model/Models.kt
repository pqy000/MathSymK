package io.github.ezrnest.model

import io.github.ezrnest.model.struct.AddGroupModel
import io.github.ezrnest.model.struct.FieldModel
import io.github.ezrnest.model.struct.RingModel
import io.github.ezrnest.numberTheory.NTFunctions
import io.github.ezrnest.structure.*
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.pow

open class AsAddGroup<T : AddGroupModel<T>>(override val zero: T) : AddGroup<T> {
    override fun contains(x: T): Boolean {
        return true
    }


    override fun add(x: T, y: T): T {
        return x + y
    }

    override fun negate(x: T): T {
        return -x
    }

    override fun subtract(x: T, y: T): T {
        return x - y
    }


    override fun isEqual(x: T, y: T): Boolean {
        return x == y
    }
}

open class AsRing<T : RingModel<T>>(zero: T) : AsAddGroup<T>(zero), Ring<T> {

    override fun multiply(x: T, y: T): T {
        return x * y
    }
}

open class AsUnitRing<T : RingModel<T>>(zero: T, override val one: T) : AsRing<T>(zero), UnitRing<T>

open class AsField<T : FieldModel<T>>(zero: T, one: T, override val characteristic: Long? = 0) :
    AsUnitRing<T>(zero, one), Field<T> {

    override fun divide(x: T, y: T): T {
        return x / y
    }

    override fun reciprocal(x: T): T {
        return x.inv()
    }


}


object IntAsIntegers : Integers<Int> {

    override fun contains(x: Int): Boolean {
        return true
    }

    override val zero: Int = 0

    override val one: Int = 1

    override fun isUnit(x: Int): Boolean {
        return x == 1 || x == -1
    }

    override fun add(x: Int, y: Int): Int {
        return Math.addExact(x, y)
    }

    override fun negate(x: Int): Int {
        return Math.negateExact(x)
    }

    override fun subtract(x: Int, y: Int): Int {
        return Math.subtractExact(x, y)
    }

    override fun multiply(x: Int, y: Int): Int {
        return Math.multiplyExact(x, y)
    }

    override fun asBigInteger(x: Int): BigInteger {
        return BigInteger.valueOf(x.toLong())
    }

    override fun mod(a: Int, b: Int): Int {
        return NTFunctions.mod(a, b)
    }

    override fun divToInt(a: Int, b: Int): Int {
        return a / b
    }

    override fun remainder(a: Int, b: Int): Int {
        return a % b
    }

    override fun isEqual(x: Int, y: Int): Boolean {
        return x == y
    }

    override fun compare(o1: Int, o2: Int): Int {
        return o1.compareTo(o2)
    }

    override fun gcd(a: Int, b: Int): Int {
        return NTFunctions.gcd(a, b)
    }
    override fun gcdUV(a: Int, b: Int): Triple<Int, Int, Int> {
        return NTFunctions.gcdUV(a, b).let { Triple(it[0], it[1], it[2]) }
    }

    override fun gcdExtendedFull(a: Int, b: Int): EuclideanDomain.GcdFullResult<Int> {
        val (g, u, v) = NTFunctions.gcdUV(a, b)
        return EuclideanDomain.GcdFullResult(g, u, v, a / g, b / g)
    }

    override fun deg(a: Int, b: Int): Int {
        return NTFunctions.deg(a, b)
    }

    override fun lcm(a: Int, b: Int): Int {
        return NTFunctions.lcm(a, b)
    }

    override fun chineseRemainder(mods: List<Int>, remainders: List<Int>): Int {
        val modsLong = LongArray(mods.size) { mods[it].toLong() }
        val remaindersLong = LongArray(remainders.size) { remainders[it].toLong() }
        return NTFunctions.chineseRemainder(modsLong, remaindersLong).toInt()
    }

    override fun powMod(a: Int, n: Int, m: Int): Int {
        return NTFunctions.powMod(a, n, m)
    }

    override fun modInverse(a: Int, p: Int): Int {
        return NTFunctions.modInverse(a, p)
    }


}

object LongAsIntegers : Integers<Long> {
    override fun contains(x: Long): Boolean {
        return true
    }

    override val zero: Long = 0L

    override val one: Long = 1L

    override fun isUnit(x: Long): Boolean {
        return x == 1L || x == -1L
    }

    override fun add(x: Long, y: Long): Long {
        return Math.addExact(x, y)
    }

    override fun negate(x: Long): Long {
        return Math.negateExact(x)
    }

    override fun subtract(x: Long, y: Long): Long {
        return Math.subtractExact(x, y)
    }

    override fun multiply(x: Long, y: Long): Long {
        return Math.multiplyExact(x, y)
    }

    override fun asBigInteger(x: Long): BigInteger {
        return BigInteger.valueOf(x)
    }

    override fun mod(a: Long, b: Long): Long {
        return NTFunctions.mod(a, b)
    }

    override fun divToInt(a: Long, b: Long): Long {
        return a / b
    }

    override fun remainder(a: Long, b: Long): Long {
        return a % b
    }

    override fun isEqual(x: Long, y: Long): Boolean {
        return x == y
    }

    override fun compare(o1: Long, o2: Long): Int {
        return o1.compareTo(o2)
    }

    override fun gcd(a: Long, b: Long): Long {
        return NTFunctions.gcd(a, b)
    }

    override fun deg(a: Long, b: Long): Long {
        return NTFunctions.deg(a, b).toLong()
    }

    override fun lcm(a: Long, b: Long): Long {
        return NTFunctions.lcm(a, b)
    }

    override fun chineseRemainder(mods: List<Long>, remainders: List<Long>): Long {
        return NTFunctions.chineseRemainder(mods.toLongArray(), remainders.toLongArray())
    }

    override fun powMod(a: Long, n: Long, m: Long): Long {
        return NTFunctions.powMod(a, n, m)
    }

    override fun modInverse(a: Long, p: Long): Long {
        return NTFunctions.modInverse(a, p)
    }

    override fun gcdUV(a: Long, b: Long): Triple<Long, Long, Long> {
        return NTFunctions.gcdUV(a, b).let { Triple(it[0], it[1], it[2]) }
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER") // Shadowing is intended to avoid ambiguity.
    override operator fun Long.times(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") x: Long): Long {
        return this * x
    }

//        override fun gcdUVMin(a: Long, b: Long): Triple<Long, Long, Long> {
//            return NTFunctions.gcdUVMin(a, b)
//        }
}

object BigIntegerAsIntegers : Integers<BigInteger> {

//        override val numberClass: Class<BigInteger>
//            get() = BigInteger::class.java

    override fun contains(x: BigInteger): Boolean {
        return true
    }

    override val zero: BigInteger get() = BigInteger.ZERO

    override val one: BigInteger get() = BigInteger.ONE


    override fun isEqual(x: BigInteger, y: BigInteger): Boolean {
        return x == y
    }

    override fun isZero(x: BigInteger): Boolean {
        return x == BigInteger.ZERO
    }

    override fun isUnit(x: BigInteger): Boolean {
        return x == BigInteger.ONE || x == BigInteger.ONE.negate()
    }

    override fun add(x: BigInteger, y: BigInteger): BigInteger {
        return x.add(y)
    }

    override fun negate(x: BigInteger): BigInteger {
        return x.negate()
    }


    override fun subtract(x: BigInteger, y: BigInteger): BigInteger {
        return x.subtract(y)
    }

    override fun multiply(x: BigInteger, y: BigInteger): BigInteger {
        return x.multiply(y)
    }


    override fun multiplyN(x: BigInteger, n: Long): BigInteger {
        return x.multiply(n.toBigInteger())
    }

    override fun power(x: BigInteger, n: Long): BigInteger {
        return x.pow(n.toInt())
    }

    override fun asBigInteger(x: BigInteger): BigInteger {
        return x
    }

    override fun mod(a: BigInteger, b: BigInteger): BigInteger {
        return a.mod(b)
    }

    override fun divToInt(a: BigInteger, b: BigInteger): BigInteger {
        return a.divide(b)
    }

    override fun compare(o1: BigInteger, o2: BigInteger): Int {
        return o1.compareTo(o2)
    }

    override fun gcd(a: BigInteger, b: BigInteger): BigInteger {
        return a.gcd(b)
    }

    override fun powMod(a: BigInteger, n: BigInteger, m: BigInteger): BigInteger {
        return a.modPow(n, m)
    }

    override fun modInverse(a: BigInteger, p: BigInteger): BigInteger {
        return a.modInverse(p)
    }

    override fun powMod(a: BigInteger, n: Long, m: BigInteger): BigInteger {
        return a.modPow(n.toBigInteger(), m)
    }

    override fun isOdd(x: BigInteger): Boolean {
        return x.testBit(0)
    }

    override fun isEven(x: BigInteger): Boolean {
        return !x.testBit(0)
    }

    override fun remainder(a: BigInteger, b: BigInteger): BigInteger {
        return a.remainder(b)
    }

    override fun divideAndRem(a: BigInteger, b: BigInteger): Pair<BigInteger, BigInteger> {
        val res = a.divideAndRemainder(b)
        return res[0] to res[1]
    }


    override fun ofN(n: Long): BigInteger {
        return BigInteger.valueOf(n)
    }

    override fun asLong(x: BigInteger): Long {
        return x.longValueExact()
    }

    override fun abs(x: BigInteger): BigInteger {
        return x.abs()
    }


}

class DoubleAsReals(
    /**
     * The deviation allowed for testing equality of two double numbers.
     */
    val dev: Double = Double.MIN_VALUE
) : Reals<Double> {
    override fun contains(x: Double): Boolean {
        return true
    }

    override val zero: Double = 0.0

    override val one: Double = 1.0

//        override val numberClass: Class<Double>
//            get() = Double::class.java

    override fun abs(x: Double): Double {
        return kotlin.math.abs(x)
    }


    override fun isEqual(x: Double, y: Double): Boolean {
        return abs(x - y) < dev
    }

    override fun isZero(x: Double): Boolean {
        return abs(x) < dev
    }

    override fun reciprocal(x: Double): Double {
        return 1 / x
    }

    override fun divideLong(x: Double, n: Long): Double {
        return x / n
    }


    override fun add(x: Double, y: Double): Double {
        return x + y
    }

    override fun negate(x: Double): Double {
        return -x
    }

    override fun subtract(x: Double, y: Double): Double {
        return x - y
    }

    override fun multiply(x: Double, y: Double): Double {
        return x * y
    }

    override fun divide(x: Double, y: Double): Double {
        return x / y
    }


    override fun compare(o1: Double, o2: Double): Int {
        return o1.compareTo(o2)
    }

    override fun power(x: Double, n: Long): Double {
        return Math.pow(x, n.toDouble())
    }

    override fun sqrt(x: Double): Double {
        return kotlin.math.sqrt(x)
    }

    override fun nroot(x: Double, n: Long): Double {
        return x.pow(1.0 / n)
    }

    override fun constantValue(name: String): Double {
        return when (name) {
            "PI" -> kotlin.math.PI
            "E" -> kotlin.math.E
            else -> throw IllegalArgumentException("Constant $name is not defined.")
        }
    }

    override fun exp(x: Double): Double {
        return kotlin.math.exp(x)
    }

    override fun ln(x: Double): Double {
        return kotlin.math.ln(x)
    }

    override fun sin(x: Double): Double {
        return kotlin.math.sin(x)
    }

    override fun arcsin(x: Double): Double {
        return kotlin.math.asin(x)
    }

    override fun exp(base: Double, pow: Double): Double {
        return base.pow(pow)
    }

    override fun log(base: Double, x: Double): Double {
        return kotlin.math.log(x, base)
    }

    override fun cos(x: Double): Double {
        return kotlin.math.cos(x)
    }

    override fun tan(x: Double): Double {
        return kotlin.math.tan(x)
    }

    override fun arctan2(y: Double, x: Double): Double {
        return kotlin.math.atan2(y, x)
    }

    override fun arccos(x: Double): Double {
        return kotlin.math.acos(x)
    }

    override fun arctan(x: Double): Double {
        return kotlin.math.atan(x)
    }

}

class BigDecimalAsReals(val mc: MathContext = MathContext.DECIMAL128) : Reals<BigDecimal> {

    override fun contains(x: BigDecimal): Boolean {
        return true
    }

    override val zero: BigDecimal = BigDecimal.ZERO

    override val one: BigDecimal = BigDecimal.ONE

//        override val numberClass: Class<BigDecimal>
//            get() = BigDecimal::class.java

    override fun abs(x: BigDecimal): BigDecimal {
        return x.abs()
    }

    override fun isEqual(x: BigDecimal, y: BigDecimal): Boolean {
        return x.compareTo(y) == 0
    }


    override fun add(x: BigDecimal, y: BigDecimal): BigDecimal {
        return x.add(y, mc)
    }

    override fun negate(x: BigDecimal): BigDecimal {
        return x.negate()
    }

    override fun subtract(x: BigDecimal, y: BigDecimal): BigDecimal {
        return x.subtract(y, mc)
    }

    override fun multiply(x: BigDecimal, y: BigDecimal): BigDecimal {
        return x.multiply(y, mc)
    }

    override fun divide(x: BigDecimal, y: BigDecimal): BigDecimal {
        return x.divide(y, mc)
    }

    override fun reciprocal(x: BigDecimal): BigDecimal {
        return one.divide(x, mc)
    }

    override fun divideLong(x: BigDecimal, n: Long): BigDecimal {
        return x.divide(BigDecimal.valueOf(n), mc)
    }


    override fun compare(o1: BigDecimal, o2: BigDecimal): Int {
        return o1.compareTo(o2)
    }

    override fun power(x: BigDecimal, n: Long): BigDecimal {
        return x.pow(n.toInt())
    }

    override fun sqrt(x: BigDecimal): BigDecimal {
        return x.sqrt(mc)
    }

    override fun constantValue(name: String): BigDecimal {
        return when (name.uppercase()) {
            "PI" -> BigDecimal(kotlin.math.PI, mc)
            "E" -> BigDecimal(kotlin.math.E, mc)
            else -> throw IllegalArgumentException("Constant $name is not defined.")
        }
    }

    override fun nroot(x: BigDecimal, n: Long): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun exp(x: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun ln(x: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun sin(x: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun arcsin(x: BigDecimal): BigDecimal {
        TODO("Not yet implemented")
    }


    companion object {
        operator fun invoke(precision: Int, roundingMode: RoundingMode): BigDecimalAsReals {
            val mc = MathContext(precision, roundingMode)
            return BigDecimalAsReals(mc)
        }
    }
}


typealias BigFraction = RFraction<BigInteger>

object BigFractionAsQuotients : RFracOverIntDom<BigInteger>(BigIntegerAsIntegers), Quotients<BigFraction> {
    override fun simplifyFrac(nume: BigInteger, deno: BigInteger): RFraction<BigInteger> {
        val r = super.simplifyFrac(nume, deno)
        if (r.deno < BigInteger.ZERO) {
            return RFraction(-r.nume, -r.deno)
        }
        return r
    }

    fun bfrac(n: Int, d: Int): BigFraction {
        return frac(n.toBigInteger(), d.toBigInteger())
    }

    fun bfrac(n: Long, d: Long): BigFraction {
        return frac(n.toBigInteger(), d.toBigInteger())
    }

    fun bfrac(n: BigInteger, d: BigInteger): BigFraction {
        return frac(n, d)
    }

    val Int.bfrac: BigFraction
        get() = this.toBigInteger().f

    val Long.bfrac: BigFraction
        get() = this.toBigInteger().f

    override val characteristic: Long
        get() = super<Quotients>.characteristic

    override fun compare(o1: BigFraction, o2: BigFraction): Int {
        val a = if (o1.deno < BigInteger.ZERO) -o1 else o1
        val b = if (o2.deno < BigInteger.ZERO) -o2 else o2
        return (a.nume * b.deno).compareTo(b.nume * a.deno)
    }


}


/**
 * Provides basic number models.
 */
object Models {
    /**
     * Gets a group calculator on the GroupNumberModel.
     */
    fun <T : AddGroupModel<T>> asGroup(zero: T): AddGroup<T> {
        return AsAddGroup(zero)
    }

    fun <T : RingModel<T>> asRing(zero: T) = AsRing(zero)

    fun <T : RingModel<T>> asUnitRing(zero: T, one: T) = AsUnitRing(zero, one)

    fun <T : FieldModel<T>> asField(zero: T, one: T, characteristic: Long? = 0) = AsField(zero, one, characteristic)


    /**
     * Gets the model of integers with type `Int`.
     *
     * @see Integers
     */
    fun ints(): Integers<Int> = IntAsIntegers

    /**
     * Gets the model of integers with type `Long`.
     */
    fun longs(): Integers<Long> = LongAsIntegers

    /**
     * Gets the model of integers with type `BigInteger`.
     */
    fun bigIntegers(): Integers<BigInteger> = BigIntegerAsIntegers

    fun doubles(dev : Double = Double.MIN_VALUE): Reals<Double> = DoubleAsReals(dev)

    fun bigDecimals(mc: MathContext = MathContext.DECIMAL128): Reals<BigDecimal> = BigDecimalAsReals(mc)

    /**
     * Returns the ring of integers mod n, `Z/nZ`.
     *
     * @param n an integer, `n >= 2`.
     */
    fun intModN(n: Int): IntModN = IntModN(n)

    /**
     * Returns the field of integers modulo p, `Z/pZ`.
     *
     * @param p a prime number. This method do not guarantee the correctness of the result if `p` is not a prime number.
     * @param cached whether to store the inverse of each element to speed up calculation.
     */
    fun intModP(p: Int, cached: Boolean = (p <= 1024)): IntModP {
        return if (cached) {
            IntModPCached(p)
        } else {
            IntModP(p)
        }
    }

    /**
     * Gets the field of fractions [Fraction].
     */
    fun fractions(): FractionAsQuotients {
        return FractionAsQuotients
    }

    /**
     * Gets the field of fractions with [BigInteger].
     *
     * @see RFraction
     */
    fun fractionBig(): BigFractionAsQuotients {
        return BigFractionAsQuotients
    }

}