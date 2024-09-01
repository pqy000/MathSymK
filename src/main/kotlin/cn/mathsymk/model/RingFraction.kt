package cn.mathsymk.model

import cn.mathsymk.structure.*

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
@JvmRecord
data class RingFraction<T : Any>
internal constructor(val nume: T, val deno: T){
    /*
    Basic properties:
     */

    override fun toString(): String {
        return "($nume)/($deno)"
    }


    fun <N : Any> mapTo(mapper: (T) -> N): RingFraction<N> {
        return RingFraction(mapper(nume), mapper(deno))
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

        fun <T : Any> asRing(model: UnitRing<T>): RFractionAsRing<T> {
            return RFractionAsRing(model)
        }

        /**
         * Returns the fraction field of the given ring.
         */
        fun <T : Any> asField(model: IntegralDomain<T>): RingFractionAsField<T> {
            return RingFractionAsField(model)
        }

    }
}

open class RFractionAsRing<T : Any>( model: UnitRing<T>) : Ring<RingFraction<T>> {

    @Suppress("CanBePrimaryConstructorProperty")
    open val model: UnitRing<T> = model

    final override val zero: RingFraction<T> = RingFraction(model.zero, model.one)

    override fun contains(x: RingFraction<T>): Boolean {
        return model.contains(x.nume) && model.contains(x.deno)
    }

    fun of(nume: T, deno: T): RingFraction<T> {
        if (model.isZero(deno)) {
            throw ArithmeticException("Cannot divide by zero: $nume / $deno")
        }
        return simplifyFrac(nume, deno)
    }

    /**
     * The inclusion function from the ring to the fractions.
     */
    val T.f: RingFraction<T>
        get() = of(this, model.one)


    operator fun T.div(deno: T): RingFraction<T> {
        return of(this, deno)
    }

    /*
   Simplifying
    */
//    protected fun gcdReduce(n: T, d: T): RingFraction<T> {
//        val model = model
//        if (model !is UniqueFactorizationDomain) {
//            return of(n, d)
//        }
//        model.eval {
//            val g = gcd(n, d)
//            return of(exactDivide(n, g), exactDivide(d, g))
//        }
//    }

    protected open fun simplifyFrac(nume: T, deno: T): RingFraction<T> {
        return RingFraction(nume, deno)
    }


    /*
    Field model:
     */
    override fun isEqual(x: RingFraction<T>, y: RingFraction<T>): Boolean {
        return model.eval {
            isZero(x.nume * y.deno - x.deno * y.nume)
        }
    }

    override fun isZero(x: RingFraction<T>): Boolean {
        return model.isZero(x.nume)
    }

    override fun add(x: RingFraction<T>, y: RingFraction<T>): RingFraction<T> {
        model.eval {
            val n = x.nume * y.deno + x.deno * y.nume
            val d = x.deno * y.deno
            return simplifyFrac(n, d)
        }
    }

    override fun subtract(x: RingFraction<T>, y: RingFraction<T>): RingFraction<T> {
        model.eval {
            val n = x.nume * y.deno - x.deno * y.nume
            val d = x.deno * y.deno
            return simplifyFrac(n, d)
        }
    }

    override fun negate(x: RingFraction<T>): RingFraction<T> {
        return RingFraction(model.negate(x.nume), x.deno)
    }

    override fun multiply(x: RingFraction<T>, y: RingFraction<T>): RingFraction<T> {
        model.eval {
            val f1 = simplifyFrac(x.nume, y.deno)
            val f2 = simplifyFrac(y.nume, x.deno)
            return RingFraction(f1.nume * f2.nume, f1.deno * f2.deno)
        }
    }

    override fun multiplyLong(x: RingFraction<T>, n: Long): RingFraction<T> {
        model.eval {
            val f1 = simplifyFrac(model.of(n), x.deno)
            return RingFraction(x.nume * f1.nume, f1.deno)
        }
    }


}

class RingFractionAsField<T : Any>(override val model: IntegralDomain<T>) : RFractionAsRing<T>(model),
    Field<RingFraction<T>> {

    override val one: RingFraction<T>
        get() = RingFraction(model.one, model.one)

    override fun simplifyFrac(nume: T, deno: T): RingFraction<T> {
        if (model is UniqueFactorizationDomain) {
            val g = model.gcd(nume, deno)
            val n = model.exactDivide(nume, g)
            val d = model.exactDivide(deno, g)
            return RingFraction(n, d)
        }
        return RingFraction(nume, deno)
    }

    override val characteristic: Long?
        get() = null

    override fun reciprocal(x: RingFraction<T>): RingFraction<T> {
        if (isZero(x)) {
            throw ArithmeticException("Cannot invert zero: $x")
        }
        return RingFraction(x.deno, x.nume)
    }

    override fun divide(x: RingFraction<T>, y: RingFraction<T>): RingFraction<T> {
        if (isZero(y)) {
            throw ArithmeticException("Division by zero: $x / $y")
        }
        model.eval {
            val f1 = simplifyFrac(x.nume, y.nume)
            val f2 = simplifyFrac(y.deno, x.deno)
            return RingFraction(f1.nume * f2.nume, f1.deno * f2.deno)
        }
    }


    /*
    Field model:
     */

    override fun power(x: RingFraction<T>, n: Long): RingFraction<T> {
        if (n > 0) {
            return RingFraction(model.power(x.nume, n), model.power(x.deno, n))
        }
        if (isZero(x)) throw ArithmeticException("Cannot raise zero to a negative power: $x^$n")
        if (n == 0L) return one
        return RingFraction(model.power(x.deno, -n), model.power(x.nume, -n))
    }


}