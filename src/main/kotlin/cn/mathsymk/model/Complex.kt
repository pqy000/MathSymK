package cn.mathsymk.model

import cn.mathsymk.structure.*
import java.util.function.Function


/*
Created by liyicheng 2020/2/24
*/

///**
// * Describes the expanded complex including the infinity point.
// */
//sealed class ComplexE<T> constructor(mc: Field<T>) : AbstractMathObject<T, Field<T>>(mc) {
//
//    abstract fun isInf(): Boolean
//
//    abstract override fun <N> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): ComplexE<N>
//}
//
//class ComplexInf<T> internal constructor(mc: Field<T>) : ComplexE<T>(mc) {
//
//    override fun isInf(): Boolean {
//        return true
//    }
//
//    override fun <N> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): ComplexInf<N> {
//        return ComplexInf(newCalculator as Field)
//    }
//
//    override fun valueEquals(obj: IMathObject<T>): Boolean {
//        return obj is ComplexInf
//    }
//
//    override fun toString(nf: NumberFormatter<T>): String {
//        return "Inf"
//    }
//
//    override fun equals(other: Any?): Boolean {
//        return other is ComplexInf<*>
//    }
//
//    override fun hashCode(): Int {
//        return 0
//    }
//}

/**
 * Defines the complex number, which is a number that can be written as `a+bi`, where `a` is the real part and `b` is the imaginary part.
 * The complex numbers satisfy:
 * * Addition: `(a+bi)+(c+di) = (a+c) + (b+d)i`
 * * Multiplication: `i^2 = -1`, `(a+bi)(c+di) = (ac-bd) + (ad+bc)i`
 *
 *
 */
@JvmRecord
data class Complex<T>(val a: T, val b: T) {
    /*
    Created by lyc at 2024/8/29
     */

//    /*
//    MathObject
//     */
//
//
//    override fun valueEquals(obj: IMathObject<T>): Boolean {
//        if (obj !is Complex) {
//            return false
//        }
//        return model.isEqual(a, obj.a) && model.isEqual(b, obj.b)
//    }

    override fun toString(): String {
//        if (model.isZero(b)) return "$a"
//        if (model.isZero(a)) return "${b}i"
        return "$a+${b}i"
    }


    fun <N> mapTo(mapper: Function<T, N>): Complex<N> {
        return Complex(mapper.apply(a), mapper.apply(b))
    }

    companion object {

        fun <T> over(model: Ring<T>): ComplexOnRing<T> {
            return ComplexOnRing(model)
        }

        fun <T> over(model: UnitRing<T>): ComplexOnUnitRing<T> {
            return ComplexOnUnitRing(model)
        }

        fun <T> over(model: Field<T>): ComplexOnField<T> {
            return ComplexOnField(model)
        }

//        fun <T> from(reals : Reals<T>) : ComplexOnField<T> {
//            return ComplexOnField(reals)
//        }
    }
}

open class ComplexOnRing<T>(_model: Ring<T>) : Ring<Complex<T>>, Module<T, Complex<T>> {
    override val zero: Complex<T> = Complex(_model.zero, _model.zero)
    override val scalars: Ring<T>
        get() = model


    open val model: Ring<T> = _model

    inline val T.i: Complex<T>
        get() = imag(this)


    override fun isEqual(x: Complex<T>, y: Complex<T>): Boolean {
        return model.isEqual(x.a, y.a) && model.isEqual(x.b, y.b)
    }

    override fun isZero(x: Complex<T>): Boolean {
        return model.isZero(x.a) && model.isZero(x.b)
    }

    fun of(a: T, b: T): Complex<T> {
        return Complex(a, b)
    }

    fun real(a: T): Complex<T> {
        return Complex(a, model.zero)
    }

    fun imag(b: T): Complex<T> {
        return Complex(model.zero, b)
    }

    override fun negate(x: Complex<T>): Complex<T> {
        return Complex(model.negate(x.a), model.negate(x.b))
    }

    override fun scalarMul(k: T, v: Complex<T>): Complex<T> {
        return model.eval { of(k * v.a, k * v.b) }
    }

    operator fun T.times(v: Complex<T>): Complex<T> {
        return scalarMul(this, v)
    }

    operator fun Complex<T>.times(k: T): Complex<T> {
        return scalarMul(k, this)
    }


    override fun contains(x: Complex<T>): Boolean {
        return model.contains(x.a) && model.contains(x.b)
    }

    override fun add(x: Complex<T>, y: Complex<T>): Complex<T> {
        return Complex(model.add(x.a, y.a), model.add(x.b, y.b))
    }

    operator fun Complex<T>.plus(y: T): Complex<T> {
        return add(this, real(y))
    }

    operator fun T.plus(y: Complex<T>): Complex<T> {
        return add(real(this), y)
    }


    /*
    Complex number related methods
     */


    val Complex<T>.modSquared: T
        get() {
            return model.eval { a * a + b * b }
        }

    val Complex<T>.conjugate: Complex<T>
        get() {
            return model.eval { of(a, -b) }
        }

    override fun multiply(x: Complex<T>, y: Complex<T>): Complex<T> {
        return model.eval { of(x.a * y.a - x.b * y.b, x.a * y.b + x.b * y.a) }
    }

    override fun subtract(x: Complex<T>, y: Complex<T>): Complex<T> {
        return model.eval { of(x.a - y.a, x.b - y.b) }
    }

    override fun multiplyLong(x: Complex<T>, n: Long): Complex<T> {
        return model.eval { of(x.a * n, x.b * n) }
    }

    override fun sum(elements: List<Complex<T>>): Complex<T> {
        return model.eval {
            val a = model.sum(elements.map { it.a })
            val b = model.sum(elements.map { it.b })
            of(a, b)
        }
    }
}

open class ComplexOnUnitRing<T>(override val model: UnitRing<T>) :
    ComplexOnRing<T>(model), UnitRing<Complex<T>> {

    override val one: Complex<T>
        get() = Complex(model.one, model.zero)

    open val i: Complex<T>
        get() = imag(model.one)
}

open class ComplexOnField<T>(override val model: Field<T>) :
    ComplexOnUnitRing<T>(model), Field<Complex<T>>,
    Algebra<T, Complex<T>> {

    override val scalars: Field<T>
        get() = model
    override val characteristic: Long?
        get() = model.characteristic


    override fun reciprocal(x: Complex<T>): Complex<T> {
        val d = model.eval { x.a * x.a + x.b * x.b }
        return model.eval { of(x.a / d, -x.b / d) }
    }

    override fun divide(x: Complex<T>, y: Complex<T>): Complex<T> {
        val d = model.eval { y.a * y.a + y.b * y.b }
        return model.eval {
            val t1 = x.a * y.a + x.b * y.b
            val t2 = x.b * y.a - x.a * y.b
            of(t1 / d, t2 / d)
        }
    }

    override fun scalarDiv(x: Complex<T>, k: T): Complex<T> {
        return model.eval { of(x.a / k, x.b / k) }
    }

    operator fun Complex<T>.div(k: T): Complex<T> {
        return scalarDiv(this, k)
    }

    operator fun T.div(v: Complex<T>): Complex<T> {
        return v.inv() * this
    }
}

open class ComplexFromReals<T>(override val reals: Reals<T>) : ComplexOnField<T>(reals), ComplexNumbers<T, Complex<T>> {

    override val i: Complex<T>
        get() = imag(reals.one)
    override val model: Reals<T>
        get() = reals


    override fun re(z: Complex<T>): T {
        return z.a
    }

    override fun im(z: Complex<T>): T {
        return z.b
    }

}