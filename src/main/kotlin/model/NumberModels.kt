package cn.mathsymk.model

import cn.mathsymk.model.struct.AddGroupModel
import cn.mathsymk.model.struct.FieldModel
import cn.mathsymk.model.struct.RingModel
import cn.mathsymk.number_theory.NTFunctions
import cn.mathsymk.structure.*
import java.math.BigInteger
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

open class AsUnitRing<T : RingModel<T>>(zero: T, override val one: T) : AsRing<T>(zero), UnitRing<T> {

}

open class AsField<T : FieldModel<T>>(zero: T, one: T, override val characteristic: Long = 0) :
    AsUnitRing<T>(zero, one), Field<T> {

    override fun divide(x: T, y: T): T {
        return x / y
    }

    override fun reciprocal(x: T): T {
        return x.inv()
    }


}


object NumberModels {
    /**
     * Gets a group calculator on the GroupNumberModel.
     */
    fun <T : AddGroupModel<T>> asGroup(zero: T): AddGroup<T> {
        return AsAddGroup(zero)
    }

    fun <T : RingModel<T>> asRing(zero: T) = AsRing(zero)

    fun <T : RingModel<T>> asUnitRing(zero: T, one: T) = AsUnitRing(zero, one)

    fun <T : FieldModel<T>> asField(zero: T, one: T, characteristic: Long = 0) = AsField(zero, one, characteristic)


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
            return x + y
        }

        override fun negate(x: Int): Int {
            return -x
        }

        override fun subtract(x: Int, y: Int): Int {
            return x - y
        }

        override fun multiply(x: Int, y: Int): Int {
            return x * y
        }

        override fun asBigInteger(x: Int): BigInteger {
            return BigInteger.valueOf(x.toLong())
        }

        override fun mod(a: Int, b: Int): Int {
            return NTFunctions.mod(a, b)
        }

        override fun divideToInteger(a: Int, b: Int): Int {
            return a / b
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

        override fun gcdUV(a: Int, b: Int): Triple<Int, Int, Int> {
            return NTFunctions.gcdUV(a, b).let { Triple(it[0], it[1], it[2]) }
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
            return x + y
        }

        override fun negate(x: Long): Long {
            return -x
        }

        override fun subtract(x: Long, y: Long): Long {
            return x - y
        }

        override fun multiply(x: Long, y: Long): Long {
            return x * y
        }

        override fun asBigInteger(x: Long): BigInteger {
            return BigInteger.valueOf(x)
        }

        override fun mod(a: Long, b: Long): Long {
            return NTFunctions.mod(a, b)
        }

        override fun divideToInteger(a: Long, b: Long): Long {
            return a / b
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


    object DoubleAsReals : Reals<Double> {
        override fun contains(x: Double): Boolean {
            return true
        }

        override val zero: Double = 0.0

        override val one: Double = 1.0

        override val numberClass: Class<Double>
            get() = Double::class.java

        override fun abs(x: Double): Double {
            return kotlin.math.abs(x)
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

        override fun isEqual(x: Double, y: Double): Boolean {
            return x == y
        }

        override fun compare(o1: Double, o2: Double): Int {
            return o1.compareTo(o2)
        }

        override fun power(x: Double, n: Long): Double {
            return Math.pow(x, n.toDouble())
        }

        override fun squareRoot(x: Double): Double {
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

        override fun exp(a: Double, b: Double): Double {
            return a.pow(b)
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

        override fun arccos(x: Double): Double {
            return kotlin.math.acos(x)
        }

        override fun arctan(x: Double): Double {
            return kotlin.math.atan(x)
        }

    }
}