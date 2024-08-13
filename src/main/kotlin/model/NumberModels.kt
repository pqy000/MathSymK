package cn.mathsymk.model

import cn.mathsymk.model.struct.AddGroupModel
import cn.mathsymk.model.struct.FieldModel
import cn.mathsymk.model.struct.RingModel
import cn.mathsymk.number_theory.NTFunctions
import cn.mathsymk.structure.AddGroup
import cn.mathsymk.structure.Field
import cn.mathsymk.structure.Integers
import cn.mathsymk.structure.Ring
import util.ModelPatterns
import java.math.BigInteger


object NumberModels {
    /**
     * Gets a group calculator on the GroupNumberModel.
     */
    fun <T : AddGroupModel<T>> asGroup(zero: T): AddGroup<T> {
        return object : AddGroup<T> {
            override fun contains(x: T): Boolean {
                return true
            }

            override val zero: T = zero

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
    }

    fun <T : RingModel<T>> asRing(zero: T) = object : Ring<T> {
        override fun contains(x: T): Boolean {
            return true
        }

        override val zero: T = zero

        override fun add(x: T, y: T): T {
            return x + y
        }

        override fun negate(x: T): T {
            return -x
        }

        override fun subtract(x: T, y: T): T {
            return x - y
        }

        override fun multiply(x: T, y: T): T {
            return x * y
        }

        override fun isEqual(x: T, y: T): Boolean {
            return x == y
        }
    }

    fun <T : FieldModel<T>> asField(zero: T, one: T, characteristic: Long = 0) = object : Field<T> {
        override fun contains(x: T): Boolean {
            return true
        }

        override val zero: T = zero

        override val one: T = one

        override val characteristic: Long = characteristic

        override fun add(x: T, y: T): T {
            return x + y
        }

        override fun negate(x: T): T {
            return -x
        }

        override fun subtract(x: T, y: T): T {
            return x - y
        }

        override fun multiply(x: T, y: T): T {
            return x * y
        }

        override fun isEqual(x: T, y: T): Boolean {
            return x == y
        }

        override fun divide(x: T, y: T): T {
            return x / y
        }

        override fun reciprocal(x: T): T {
            return x.inv()
        }
    }



    object DoubleAsReals

    object IntAsIntegers

    object LongAsIntegers : Integers<Long>{
        override fun contains(x: Long): Boolean {
            return true
        }

        override val zero: Long = 0L

        override val one: Long = 1L

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

        override fun compare(x: Long, y: Long): Int {
            return x.compareTo(y)
        }

        override fun gcd(x: Long, y: Long): Long {
            return NTFunctions.gcd(x, y)
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

        override fun powMod(x: Long, n: Long, m: Long): Long {
            return NTFunctions.powMod(x, n, m)
        }

        override fun modInverse(a: Long, p: Long): Long {
            return NTFunctions.modInverse(a, p)
        }

        override fun gcdUV(a: Long, b: Long): Triple<Long, Long, Long> {
            return NTFunctions.gcdUV(a, b).let { Triple(it[0], it[1], it[2]) }
        }

//        override fun gcdUVMin(a: Long, b: Long): Triple<Long, Long, Long> {
//            return NTFunctions.gcdUVMin(a, b)
//        }
    }
}