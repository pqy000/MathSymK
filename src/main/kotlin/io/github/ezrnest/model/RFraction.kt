package io.github.ezrnest.model

import io.github.ezrnest.structure.*

/**
 * RFraction defines a fraction on a unit ring of type [T].
 *
 * To use RFraction, you need to first provide a model subclassing [UnitRing] and get the corresponding RFraction model by calling [RFraction.over].
 * With the returned RFraction model, you can create and operate on fractions.
 * Here is an example:
 * ```kotlin
 *     val Z = NumberModels.bigIntegers()
 *     val Q = RFraction.over(Z)
 *     with(Q) {
 *         val a = frac(1.toBigInteger(), 2.toBigInteger())
 *         // Operate on fractions:
 *         val b = frac(3.toBigInteger(), 4.toBigInteger())
 *         val sum = a + b
 *         val product = a * b
 *         val inverseA = reciprocal(a)
 *     }
 * ```
 *
 * ## Basic properties
 *
 * A fraction over a ring is essentially defined by two components: the numerator and the denominator.
 *
 * An important distinction is made in this implementation between general rings and integral domains:
 * - Over a general [UnitRing], fractions are not guaranteed to behave as they would in a field (e.g., the lack of inverses).
 * - Over an [IntegralDomain], we can define a fraction field, which behaves similarly to the field of rational numbers.
 *
 * ## Fraction on a Unit Ring
 *
 * In a unit ring, every fraction can be represented, but without the full field properties (i.e., not every element is guaranteed to have a multiplicative inverse).
 * This is the most general type of fraction model.
 *
 * ## Fraction Field
 *
 * When the underlying ring is an [IntegralDomain], the fractions form a field. This means:
 * - Every non-zero fraction has an inverse.
 * - Arithmetic operations (addition, multiplication, etc.) follow the usual field rules.
 *
 * ## Simplification
 *
 * Fractions can be simplified when the underlying ring is a [UniqueFactorizationDomain] (such as the integers or polynomial rings).
 * Simplification involves dividing the numerator and denominator by their greatest common divisor (GCD).
 *
 * @param T the type of the ring elements in the numerator and denominator.
 */
@JvmRecord
@ConsistentCopyVisibility
data class RFraction<T>
internal constructor(val nume: T, val deno: T) {
    /*
    Basic properties:
     */

    override fun toString(): String {
        return "($nume)/($deno)"
    }

    /**
     * Maps the numerator and denominator of the fraction to a new type using the given [mapper] function.
     *
     * @param N the type of the new fraction.
     * @param mapper the function that maps elements of type [T] to type [N].
     * @return a new fraction with the mapped numerator and denominator.
     */
    fun <N> mapTo(mapper: (T) -> N): RFraction<N> {
        return RFraction(mapper(nume), mapper(deno))
    }

    companion object {

        /**
         * Creates a fraction model over a unit ring.
         *
         * This is the most general fraction model that can be constructed over any [UnitRing].
         *
         * @param model the unit ring model.
         * @return a fraction model based on the given unit ring.
         */
        fun <T> over(model: UnitRing<T>): RFracOverURing<T> {
            return RFracOverURing(model)
        }

        /**
         * Creates a fraction model over an integral domain.
         * The resulting model forms a [Field].
         *
         * @param model the integral domain model.
         * @return a fraction model based on the given integral domain.
         */
        fun <T> over(model: IntegralDomain<T>): RFracOverIntDom<T> {
            return RFracOverIntDom(model)
        }

    }
}


/**
 * Represents fractions on a unit ring. This class provides the basic arithmetic operations
 * for fractions over a unit ring, which may not necessarily form a field.
 *
 * @param T the type of the elements in the underlying unit ring.
 * @property model the unit ring model on which the fractions are based.
 */
open class RFracOverURing<T>(_model: UnitRing<T>) : UnitRing<RFraction<T>>,Module<T,RFraction<T>> {

    open val model: UnitRing<T> = _model

    final override val zero: RFraction<T> = RFraction(_model.zero, _model.one)

    final override val one: RFraction<T> = _model.one.let { RFraction(it, it) }

    override fun contains(x: RFraction<T>): Boolean {
        return model.contains(x.nume) && model.contains(x.deno)
    }

    /**
     * Creates a fraction with the given numerator [nume] and denominator [deno].
     *
     * Throws an [ArithmeticException] if the denominator is zero.
     *
     * @param nume the numerator.
     * @param deno the denominator.
     * @return the created fraction.
     */
    fun frac(nume: T, deno: T): RFraction<T> {
        if (model.isZero(deno)) {
            throw ArithmeticException("Divide by zero: $nume / $deno")
        }
        return simplifyFrac(nume, deno)
    }

    /**
     * The inclusion function from the ring to the fractions. It lifts an element of type [T] in the ring
     * to a fraction with the given element as the numerator and `1` as the denominator.
     */
    val T.f: RFraction<T>
        get() = frac(this, model.one)


    operator fun T.div(deno: T): RFraction<T> {
        return frac(this, deno)
    }

    override val scalars: Ring<T>
        get() = model

    /**
     * Simplifies a fraction if possible. The default behavior returns the fraction as-is.
     * Subclasses may override this to provide domain-specific simplifications.
     *
     * @param nume the numerator.
     * @param deno the denominator.
     * @return the simplified fraction.
     */
    protected open fun simplifyFrac(nume: T, deno: T): RFraction<T> {
        return RFraction(nume, deno)
    }


    /*
    Basic model:
     */
    override fun isEqual(x: RFraction<T>, y: RFraction<T>): Boolean {
        return model.eval {
            isZero(x.nume * y.deno - x.deno * y.nume)
        }
    }

    override fun isZero(x: RFraction<T>): Boolean {
        return model.isZero(x.nume)
    }

    override fun add(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
        model.eval {
            val n = x.nume * y.deno + x.deno * y.nume
            val d = x.deno * y.deno
            return simplifyFrac(n, d)
        }
    }

    override fun subtract(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
        model.eval {
            val n = x.nume * y.deno - x.deno * y.nume
            val d = x.deno * y.deno
            return simplifyFrac(n, d)
        }
    }

    override fun negate(x: RFraction<T>): RFraction<T> {
        return RFraction(model.negate(x.nume), x.deno)
    }

    override fun multiply(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
        model.eval {
            val f1 = simplifyFrac(x.nume, y.deno)
            val f2 = simplifyFrac(y.nume, x.deno)
            return RFraction(f1.nume * f2.nume, f1.deno * f2.deno)
        }
    }

    override fun scalarMul(k: T, v: RFraction<T>): RFraction<T> {
        model.eval {
            val f1 = simplifyFrac(k, v.deno)
            return RFraction(v.nume * f1.nume, f1.deno)
        }
    }

    override fun multiplyLong(x: RFraction<T>, n: Long): RFraction<T> {
        return scalarMul(model.of(n), x)
    }


}

open class RFracOverIntDom<T>(override val model: IntegralDomain<T>) : RFracOverURing<T>(model),
    Field<RFraction<T>> {

    override fun simplifyFrac(nume: T, deno: T): RFraction<T> {
        val model = model
        if (model is UniqueFactorizationDomain) {
            val g = model.gcd(nume, deno)
            val n = model.exactDiv(nume, g)
            val d = model.exactDiv(deno, g)
            return RFraction(n, d)
        }
        return RFraction(nume, deno)
    }

    /*
    Field model:
    */

    override val characteristic: Long?
        get() = null

    override fun reciprocal(x: RFraction<T>): RFraction<T> {
        if (isZero(x)) {
            throw ArithmeticException("Cannot invert zero: $x")
        }
        return RFraction(x.deno, x.nume)
    }

    override fun divide(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
        if (isZero(y)) {
            throw ArithmeticException("Division by zero: $x / $y")
        }
        model.eval {
            val f1 = simplifyFrac(x.nume, y.nume)
            val f2 = simplifyFrac(y.deno, x.deno)
            return RFraction(f1.nume * f2.nume, f1.deno * f2.deno)
        }
    }


    override fun power(x: RFraction<T>, n: Long): RFraction<T> {
        if (n > 0) {
            return RFraction(model.power(x.nume, n), model.power(x.deno, n))
        }
        if (isZero(x)) throw ArithmeticException("Cannot raise zero to a negative power: $x^$n")
        if (n == 0L) return one
        return RFraction(model.power(x.deno, -n), model.power(x.nume, -n))
    }

    private fun addWithGCD(x: RFraction<T>, y: RFraction<T>, model : UniqueFactorizationDomain<T>): RFraction<T> {
        with(model) {
            val g = gcd(x.deno, y.deno)
            val b1 = exactDiv(x.deno, g)
            val d1 = exactDiv(y.deno, g)
            val lcm = b1 * y.deno
            val num = x.nume * d1 + y.nume * b1
            return simplifyFrac(num, lcm)
        }
    }

    override fun add(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
        val model = model
        if (model !is UniqueFactorizationDomain) {
            return super.add(x, y)
        }
        return addWithGCD(x, y, model)
    }

    override fun subtract(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
        val model = model
        if (model !is UniqueFactorizationDomain) {
            return super<RFracOverURing>.subtract(x, y)
        }
        return addWithGCD(x, negate(y), model)
    }
}