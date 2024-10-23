package io.github.ezrnest.mathsymk.model

import io.github.ezrnest.mathsymk.structure.*
import io.github.ezrnest.mathsymk.numTh.NTFunctions


/**
 * Describes the ring of integers modulo `n`.
 */
open class IntModN(val n: Int) : OrderedRing<Int>, CommutativeRing<Int>, UnitRing<Int> {

    init {
        require(n >= 2) { "n must be at least 2, given $n" }
    }

//    override val numberClass: Class<Int>
//        get() = Int::class.java

    override fun contains(x: Int): Boolean {
        return x in 0 until n
    }

    protected fun mod(x: Int): Int {
        return NTFunctions.mod(x, n)
    }

    protected fun mod(x: Long): Int {
        return NTFunctions.mod(x, n)
    }

    override val zero: Int
        get() = 0

    override val one: Int
        get() = 1

    override fun isZero(x: Int): Boolean {
        return mod(x) == 0
    }

    override fun isUnit(x: Int): Boolean {
        return x != 0 && NTFunctions.gcd(x, n) == 1
    }

    override fun isEqual(x: Int, y: Int): Boolean {
        return mod(x) == mod(y) // assuming x and y are already in the range
    }

    override fun negate(x: Int): Int {
        return mod(Math.negateExact(x))
    }


    override fun add(x: Int, y: Int): Int {
        val x1 = x % n
        val y1 = y % n
        if (n <= Int.MAX_VALUE / 2) {
            // no overflow
            return mod(x1 + y1)
        }
        return mod(x1.toLong() + y1.toLong())
    }

    override fun subtract(x: Int, y: Int): Int {
        val x1 = x % n
        val y1 = y % n
        if (n <= Int.MAX_VALUE / 2) {
            // no overflow
            return mod(x1 - y1)
        }
        return mod(x1.toLong() - y1.toLong())
    }


    override fun multiply(x: Int, y: Int): Int {
        val x1 = x % n
        val y1 = y % n
        if (n <= 46341) {
            // no overflow
            return mod(x1 * y1)
        }
        return mod(x1.toLong() * y1.toLong())
    }

    override fun compare(o1: Int, o2: Int): Int {
        return mod(o1).compareTo(mod(o2))
    }


    override fun multiplyN(x: Int, n: Long): Int {
        val n1 = NTFunctions.mod(n, this.n)
        return multiply(x, n1)
    }

    override fun power(x: Int, n: Int): Int {
        return NTFunctions.powMod(x, n, this.n)
    }

}

open class IntModP(p: Int) : IntModN(p), Field<Int> {
    override fun isUnit(x: Int): Boolean {
        return mod(x) != 0
    }


    override fun power(x: Int, n: Int): Int {
        return super<Field>.power(x, n)
    }


    override val characteristic: Long
        get() = n.toLong()

    override fun reciprocal(x: Int): Int {
        if (mod(x) == 0) throw ArithmeticException("Division by zero")
        return NTFunctions.modInverse(x, n)
    }

}

internal class IntModPCached(p: Int) : IntModP(p) {
    private val invTable = IntArray(p)

    init {
        /*
        Explanation of the following code:
        Suppose p = q x + r, then we have
           0 = qx + r                      (mod p)
           0 = q x r^-1 + 1                (mod p)
        x^-1 = -q^-1 r^-1 = p - q^-1 r     (mod p)
         */
        invTable[1] = 1
        for (x in 2 until p) {
            val q = p / x
            val r = p % x
            if (r == 0) throw ArithmeticException("p=$p is not a prime number")
            invTable[x] = NTFunctions.mod(p.toLong() - invTable[r].toLong() * q, p)
        }
    }

    override fun reciprocal(x: Int): Int {
        val m = mod(x)
        if (m == 0) throw ArithmeticException("Division by zero")
        return invTable[mod(x)]
    }

}


open class QuotientRing<T>(open val ring: Ring<T>)

/**
 * Describes the field of rational numbers modulo `p`.
 */
class QuotientField<T>(val domain: EuclideanDomain<T>, val p: T) : Field<T> {

    override val characteristic: Long?
        get() = null

    override val zero: T
        get() = domain.zero

    override val one: T
        get() = domain.one

    override fun contains(x: T): Boolean {
        return domain.contains(x)
    }

    override fun isEqual(x: T, y: T): Boolean {
        return domain.eval {
            isZero(mod(x - y, p))
        }
    }

    override fun add(x: T, y: T): T {
        return domain.eval {
            mod(x + y, p)
        }
    }

    override fun negate(x: T): T {
        return domain.negate(x)
    }

    override fun subtract(x: T, y: T): T {
        return domain.eval { mod(x - y, p) }
    }

    override fun multiply(x: T, y: T): T {
        return domain.eval {
            mod(x * y, p)
        }
    }


    override fun reciprocal(x: T): T {
        return domain.modInverse(x, p)
    }


    override fun multiplyN(x: T, n: Long): T {
        return domain.eval { mod(multiplyN(x, n), p) }
    }


    override fun ofN(n: Long): T {
        return domain.eval { mod(ofN(n), p) }
    }


    override val isCommutative: Boolean
        get() = domain.isCommutative


}

//fun main() {
////    println(sqrt(Int.MAX_VALUE.toDouble()))
////    println(46340 * -46340)
////    println(46341 * 46341)
//    val model = IntModN(Int.MAX_VALUE / 2 - 1)
////    println(model.add(Int.MAX_VALUE-1,-10))
//    println(model.multiply(46341, 46341))
//    println(model.multiply(46340, 46340))
//    println(46341L * 46341 % (Int.MAX_VALUE / 2 - 1))
//    println(46340L * 46340 % (Int.MAX_VALUE / 2 - 1))
//}