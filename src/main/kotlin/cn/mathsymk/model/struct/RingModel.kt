package cn.mathsymk.model.struct

import cn.mathsymk.structure.EuclideanDomain
import cn.mathsymk.util.exceptions.ExceptionUtil


/**
 * Describes a number model which is suitable for a ring.
 */
interface RingModel<T : RingModel<T>> : AddGroupModel<T>, MulMonoidModel<T> {

    override val isZero: Boolean
}

//inline operator fun <T : RingNumberModel<T>> RingNumberModel<T>.times(y: T): T = multiply(y)

/**
 * Describes a number model which is suitable for a division ring.
 *
 */
interface DivisionRingModel<T : DivisionRingModel<T>> : RingModel<T>, MulGroupModel<T> {
    override fun inv(): T
}


/**
 * Describes the number model that can be elements of a Euclid ring.
 *
 *
 * Created at 2018/12/8 17:15
 * @author  liyicheng
 */
interface EuclidDomainModel<T : EuclidDomainModel<T>> : RingModel<T> {

    /**
     * Determines whether this number is a unit in the ring, which mean it is invertible with respect to multiplication.
     */
    fun isUnit(): Boolean

    /**
     * Returns the greatest common divisor of `this` and `y`.
     */
    fun gcd(y: T): T {
        @Suppress("UNCHECKED_CAST")
        val x = this as T
        return EuclideanDomain.gcdEuclid(x, y, EuclidDomainModel<T>::isZero, EuclidDomainModel<T>::rem)
    }

    /**
     * Returns a triple containing `gcd(this,y)` and `u,v` that
     * `u*this + v*y = gcd(this,y)`
     */
    fun gcdUV(y: T): Triple<T, T, T>
    //default implementation requires zero and one, which are not available.

    /**
     * Returns the least common multiplier of `this` and `y`.
     */
    fun lcm(y: T): T {
        @Suppress("UNCHECKED_CAST") val x = this as T
        if (x.isZero) {
            return x
        }
        if (y.isZero) {
            return y
        }
        val gcd = x.gcd(y)
        return (x * y).divideToInteger(gcd)
    }

    /**
     * Returns the result of dividing `this` by `y`, and the remainder.
     */
    fun divideAndRemainder(y: T): Pair<T, T>

    fun divideToInteger(y: T): T = divideAndRemainder(y).first

    fun exactDivide(y: T): T {
        val (q, r) = divideAndRemainder(y)
        if (!r.isZero) {
            ExceptionUtil.notExactDivision(this, y)
        }
        return q
    }

    operator fun rem(y: T): T = divideAndRemainder(y).second

    fun mod(y: T): T = rem(y)

    /**
     * Determines whether `this` and `y` are coprime, that is, their greatest common divisor is a unit.
     */
    fun isCoprime(y: T): Boolean {
        return gcd(y).isUnit()
    }

    /**
     * Returns the maximal degree of [y] in this, that it, the maximum integer `n`
     * such that `y^n` is a divisor of `this`.
     *
     * For example, `12.deg(2) = 2`
     */
    fun deg(y: T): Int {
        @Suppress("UNCHECKED_CAST") var b = this as T
        if (y.isZero) {
            throw ArithmeticException("a==0")
        }
        var k = 0
        var dar = b.divideAndRemainder(y)
        while (dar.second.isZero) {
            // b%a==0
            k++
            if (b == dar.first) {
                throw ArithmeticException("a==1")
            }
            b = dar.first
            // b = b/a;
            dar = b.divideAndRemainder(y)

        }
        return k
    }


    companion object {
        fun <T : EuclidDomainModel<T>> gcdUVForModel(a: T, b: T, zero: T, one: T): Triple<T, T, T> {
            return EuclideanDomain.gcdUVExtendedEuclid(a, b, zero, one,
                isZero = EuclidDomainModel<T>::isZero, subtract = { x, y -> x - y }, multiply = { x, y -> x * y },
                EuclidDomainModel<T>::divideAndRemainder, EuclidDomainModel<T>::divideToInteger
            )
        }
    }
}
