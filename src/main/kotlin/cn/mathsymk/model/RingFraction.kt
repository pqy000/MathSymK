package cn.mathsymk.model

import cn.mathsymk.AbstractMathObject
import cn.mathsymk.IMathObject
import cn.mathsymk.MathObject
import cn.mathsymk.model.struct.FieldModel
import cn.mathsymk.structure.*
import java.util.function.Function

/**
 * A RingFraction is a fraction of two elements in a ring.
 *
 * ## Fraction on a ring
 *
 * ## Fraction field
 *
 * If the ring is an [IntegralDomain], then the fraction is a field.
 *
 *
 *
 */
open class RingFraction<T : Any>
internal constructor(val nume: T, val deno: T, model: Ring<T>) :
    AbstractMathObject<T, Ring<T>>(model),
    FieldModel<RingFraction<T>> {
    /*
    Basic properties:
     */

    override fun toString(): String {
        return "($nume)/($deno)"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is RingFraction<*>) {
            return false
        }
        return nume == other.nume && deno == other.deno
    }

    override fun hashCode(): Int {
        return nume.hashCode() * 31 + deno.hashCode()
    }


    /*
    Math object:
     */
    override fun valueEquals(obj: IMathObject<T>): Boolean {
        if (obj === this) {
            return true
        }
        if (obj !is RingFraction<T>) {
            return false
        }
        return model.eval {
            isEqual(nume * obj.deno, obj.nume * deno)
        }
    }

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): MathObject<N, *> {
        return RingFraction(mapper.apply(nume), mapper.apply(deno), newCalculator as Ring<N>)
    }

    /*
   Simplifying
    */
    private fun gcdReduce(n: T, d: T): Pair<T, T> {
        val model = model
        if (model !is UniqueFactorizationDomain) {
            return n to d
        }
        model.eval {
            val g = gcd(n, d)
            return exactDivide(n, g) to exactDivide(d, g)
        }
    }

    private fun simplifyGetOne(): RingFraction<T> {
        val model = model
        if (model !is UnitRing) {
            return RingFraction(deno, deno, model)
        }
        val one = model.one
        return RingFraction(one, one, model)
    }

    /*
    Field model:
     */

    override val isZero: Boolean
        get() = model.isZero(nume)

    override val isInvertible: Boolean
        get() = !isZero

    override fun plus(y: RingFraction<T>): RingFraction<T> {
        val x = this
        model.eval {
            val n = x.nume * y.deno + x.deno * y.nume
            val d = x.deno * y.deno
            return simplifyFrac(n, d, model)
        }
    }

    override fun minus(y: RingFraction<T>): RingFraction<T> {
        val x = this
        model.eval {
            val n = x.nume * y.deno - x.deno * y.nume
            val d = x.deno * y.deno
            return simplifyFrac(n, d, model)
        }
    }

    override fun times(n: Long): RingFraction<T> {
        val newNume = model.multiplyLong(nume, n)
        return simplifyFrac(newNume, deno, model)
    }


    override fun unaryMinus(): RingFraction<T> {
        return RingFraction(model.negate(nume), deno, model)
    }


    override fun times(y: RingFraction<T>): RingFraction<T> {
        val x = this
        model.eval {
            val (n1, d2) = gcdReduce(x.nume, y.deno)
            val (n2, d1) = gcdReduce(y.nume, x.deno)
            return RingFraction(n1 * n2, d1 * d2, model)
        }
    }

    override fun pow(n: Long): RingFraction<T> {
        when {
            n == 0L -> return simplifyGetOne()
            n > 0 -> {
                val newNume = model.power(nume, n)
                val newDeno = model.power(deno, n)
                return RingFraction(newNume, newDeno, model) // assuming co-prime
            }

            else -> {
                val newNume = model.power(deno, -n)
                val newDeno = model.power(nume, -n)
                return RingFraction(newNume, newDeno, model)
            }
        }
    }

    override fun inv(): RingFraction<T> {
        if (model.isZero(nume)) {
            throw ArithmeticException("Zero cannot be inverted: $this")
        }
        return RingFraction(deno, nume, model)
    }

    override fun div(y: RingFraction<T>): RingFraction<T> {
        if (y.isZero) {
            throw ArithmeticException("Cannot divide by zero: $this / $y")
        }
        @Suppress("DuplicatedCode") // not duplicated since the order is different
        val x = this
        return model.eval {
            val (n1, d2) = gcdReduce(x.nume, y.nume)
            val (n2, d1) = gcdReduce(y.deno, x.deno)
            RingFraction(n1 * n2, d1 * d2, model)
        }
    }


    companion object {

        private fun <T : Any> simplifyFrac(nume: T, deno: T, model: Ring<T>): RingFraction<T> {
            if (model !is UniqueFactorizationDomain) {
                return RingFraction(nume, deno, model)
            }
            val g = model.gcd(nume, deno)
            val n = model.exactDivide(nume, g)
            val d = model.exactDivide(deno, g)
            return RingFraction(n, d, model)
        }


        fun <T : Any> of(nume: T, deno: T, model: Ring<T>): RingFraction<T> {
            if (model.isZero(deno)) {
                throw ArithmeticException("Cannot divide by zero: $nume / $deno")
            }

            return simplifyFrac(nume, deno, model)
        }


        fun <T : Any> of(nume: T, model: UnitRing<T>): RingFraction<T> {
            return RingFraction(nume, model.one, model)
        }

        fun <T : Any> zero(model: UnitRing<T>): RingFraction<T> {
            return RingFraction(model.zero, model.one, model)
        }

        /**
         * Returns a zero fraction with the given denominator: `0 / deno`.
         */
        fun <T : Any> zero(model: Ring<T>, deno: T): RingFraction<T> {
            if (model.isZero(deno)) {
                throw ArithmeticException("The denominator cannot be zero: $deno")
            }
            return RingFraction(model.zero, deno, model)
        }

        /**
         * Returns a one fraction with the given denominator: `deno / deno`.
         */
        fun <T : Any> one(model: Ring<T>, deno: T): RingFraction<T> {
            if (model.isZero(deno)) {
                throw ArithmeticException("The denominator cannot be zero: $deno")
            }
            return RingFraction(deno, deno, model)
        }

        /**
         * Returns a one fraction with the given denominator: `1 / 1`.
         */
        fun <T : Any> one(model: UnitRing<T>): RingFraction<T> {
            return RingFraction(model.one, model.one, model)
        }

//        fun <T : Any> asField(model: Ring<T>, d: T): Field<RingFraction<T>> {
//            return NumberModels.asField(zero(model, d), one(model, d), null)
//        }

        /**
         * Returns
         */
        fun <T : Any> asField(model: IntegralDomain<T>): Field<RingFraction<T>> {
            return NumberModels.asField(zero(model), one(model), null)
        }

    }
}

//class RingFractionAsField<T : Any>(model: Ring<T>)