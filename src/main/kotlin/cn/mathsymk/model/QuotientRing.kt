package cn.mathsymk.model

import cn.mathsymk.numberTheory.NTFunctions
import cn.mathsymk.structure.*


/**
 * Describes the ring of integers modulo `n`.
 */
open class IntModN(val n: Int) : OrderedRing<Int>, CommutativeRing<Int>, UnitRing<Int> {

    init {
        require(n >= 2) { "n must be at least 2, given $n" }
    }

    override val numberClass: Class<Int>
        get() = Int::class.java

    override fun contains(x: Int): Boolean {
        return x in 0 until n
    }

    protected fun mod(x: Int): Int {
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
        return mod(-x)
    }

    override fun add(x: Int, y: Int): Int {
        return mod(x + y)
    }

    override fun multiply(x: Int, y: Int): Int {
        return mod(x * y)
    }

    override fun compare(o1: Int, o2: Int): Int {
        return mod(o1).compareTo(mod(o2))
    }

    override fun subtract(x: Int, y: Int): Int {
        return mod(x - y)
    }

    override fun multiplyLong(x: Int, n: Long): Int {
        val n1 = NTFunctions.mod(n, this.n.toLong()).toInt()
        return mod(x * n1)
    }

    override fun power(x: Int, n: Long): Int {
        return NTFunctions.powMod(x, n, this.n)
    }

}

open class IntModP(p: Int) : IntModN(p), Field<Int> {
    override fun isUnit(x: Int): Boolean {
        return mod(x) != 0
    }


    override fun power(x: Int, n: Long): Int {
        return super<Field>.power(x, n)
    }


    override val characteristic: Long
        get() = n.toLong()

    override fun reciprocal(x: Int): Int {
        if(mod(x) == 0) throw ArithmeticException("Division by zero")
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
            invTable[x] = NTFunctions.mod(p - invTable[r] * q, p)
        }
    }

    override fun reciprocal(x: Int): Int {
        val m = mod(x)
        if(m == 0) throw ArithmeticException("Division by zero")
        return invTable[mod(x)]
    }


}


open class QuotientRing<T : Any>(open val ring: Ring<T>) {

}

/**
 * Describes the field of rational numbers modulo `p`.
 */
class QuotientField<T : Any>(val domain: EuclideanDomain<T>, val p: T) : Field<T> {

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


    override fun multiplyLong(x: T, n: Long): T {
        return domain.eval { mod(multiplyLong(x, n), p) }
    }


    override fun of(n: Long): T {
        return domain.eval { mod(of(n), p) }
    }


    override val isCommutative: Boolean
        get() = domain.isCommutative


}

