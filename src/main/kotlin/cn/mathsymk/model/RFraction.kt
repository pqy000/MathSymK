package cn.mathsymk.model

import cn.mathsymk.structure.*

/**
 * A RingFraction is a fraction of two elements in a ring.
 *
 * ## Fraction on a ring
 *
 * ## Fraction field
 *
 * If the ring is an [IntegralDomain], then the fractions form a field.
 *
 *
 *
 */
@JvmRecord
data class RFraction<T : Any>
internal constructor(val nume: T, val deno: T) {
    /*
    Basic properties:
     */

    override fun toString(): String {
        return "($nume)/($deno)"
    }


    fun <N : Any> mapTo(mapper: (T) -> N): RFraction<N> {
        return RFraction(mapper(nume), mapper(deno))
    }


    companion object {


//        fun <T : Any> of(nume: T, deno: T, model: Ring<T>): RingFraction<T> {
//            if (model.isZero(deno)) {
//                throw ArithmeticException("Cannot divide by zero: $nume / $deno")
//            }
//
//            return simplifyFrac(nume, deno, model)
//        }
//
//
//        fun <T : Any> of(nume: T, model: UnitRing<T>): RingFraction<T> {
//            return RingFraction(nume, model.one)
//        }
//
//        fun <T : Any> zero(model: UnitRing<T>): RingFraction<T> {
//            return RingFraction(model.zero, model.one)
//        }
//
//        /**
//         * Returns a zero fraction with the given denominator: `0 / deno`.
//         */
//        fun <T : Any> zero(model: Ring<T>, deno: T): RingFraction<T> {
//            if (model.isZero(deno)) {
//                throw ArithmeticException("The denominator cannot be zero: $deno")
//            }
//            return RingFraction(model.zero, deno)
//        }
//
//        /**
//         * Returns a one fraction with the given denominator: `deno / deno`.
//         */
//        fun <T : Any> one(model: Ring<T>, deno: T): RingFraction<T> {
//            if (model.isZero(deno)) {
//                throw ArithmeticException("The denominator cannot be zero: $deno")
//            }
//            return RingFraction(deno, deno)
//        }
//
//        /**
//         * Returns a one fraction with the given denominator: `1 / 1`.
//         */
//        fun <T : Any> one(model: UnitRing<T>): RingFraction<T> {
//            return RingFraction(model.one, model.one)
//        }

//        fun <T : Any> asField(model: Ring<T>, d: T): Field<RingFraction<T>> {
//            return NumberModels.asField(zero(model, d), one(model, d), null)
//        }

        fun <T : Any> asRing(model: UnitRing<T>): RFractionOnUnitRing<T> {
            return RFractionOnUnitRing(model)
        }

        /**
         * Returns the fraction field of the given ring.
         */
        fun <T : Any> asField(model: IntegralDomain<T>): RFractionOnInt<T> {
            return RFractionOnInt(model)
        }

    }
}


open class RFractionOnUnitRing<T : Any>(_model: UnitRing<T>) : UnitRing<RFraction<T>>,Module<T,RFraction<T>> {

    open val model: UnitRing<T> = _model

    final override val zero: RFraction<T> = RFraction(_model.zero, _model.one)

    final override val one: RFraction<T> = _model.one.let { RFraction(it, it) }

    override fun contains(x: RFraction<T>): Boolean {
        return model.contains(x.nume) && model.contains(x.deno)
    }

    fun frac(nume: T, deno: T): RFraction<T> {
        if (model.isZero(deno)) {
            throw ArithmeticException("Divide by zero: $nume / $deno")
        }
        return simplifyFrac(nume, deno)
    }

    /**
     * The inclusion function from the ring to the fractions.
     */
    val T.f: RFraction<T>
        get() = frac(this, model.one)


    operator fun T.div(deno: T): RFraction<T> {
        return frac(this, deno)
    }

    override val scalars: Ring<T>
        get() = model


    protected open fun simplifyFrac(nume: T, deno: T): RFraction<T> {
        return RFraction(nume, deno)
    }


    /*
    Field model:
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

//    override fun subtract(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
//        model.eval {
//            val n = x.nume * y.deno - x.deno * y.nume
//            val d = x.deno * y.deno
//            return simplifyFrac(n, d)
//        }
//    }

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

open class RFractionOnInt<T : Any>(override val model: IntegralDomain<T>) : RFractionOnUnitRing<T>(model),
    Field<RFraction<T>> {

    override fun simplifyFrac(nume: T, deno: T): RFraction<T> {
        val model = model
        if (model is UniqueFactorizationDomain) {
            val g = model.gcd(nume, deno)
            val n = model.exactDivide(nume, g)
            val d = model.exactDivide(deno, g)
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

    override fun add(x: RFraction<T>, y: RFraction<T>): RFraction<T> {
        val model = model
        if (model !is UniqueFactorizationDomain) {
            return super.add(x, y)
        }
        model.eval {
            val g = gcd(x.deno, y.deno)
            val b1 = exactDivide(x.deno, g)
            val d1 = exactDivide(y.deno, g)
            val lcm = b1 * y.deno
            val num = x.nume * d1 + y.nume * b1
            return simplifyFrac(num, lcm)
        }
    }
}