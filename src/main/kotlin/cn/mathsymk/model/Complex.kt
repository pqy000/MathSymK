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


        fun <T> over(model: UnitRing<T>): ComplexOverUnitRing<T> {
            return ComplexOverUnitRing(model)
        }

        fun <T> over(model: Field<T>): ComplexOnField<T> {
            return ComplexOnField(model)
        }

//        fun <T> from(reals : Reals<T>) : ComplexOnField<T> {
//            return ComplexOnField(reals)
//        }
    }
}

open class ComplexOverUnitRing<T>(_model: UnitRing<T>) : UnitRing<Complex<T>>,Module<T, Complex<T>> {
    override val scalars: UnitRing<T> = _model

    final override val zero: Complex<T> = Complex(_model.zero, _model.zero)

    final override val one: Complex<T>
        get() = Complex(scalars.one, scalars.zero)


    inline val T.i: Complex<T>
        get() = ofImag(this)

    fun of(a: T, b: T): Complex<T> {
        return Complex(a, b)
    }
    
    fun ofReal(a: T): Complex<T> {
        return Complex(a, scalars.zero)
    }
    fun ofImag(b: T): Complex<T> {
        return Complex(scalars.zero, b)
    }

    final override fun contains(x: Complex<T>): Boolean {
        return scalars.contains(x.a) && scalars.contains(x.b)
    }

    final override fun isEqual(x: Complex<T>, y: Complex<T>): Boolean {
        return scalars.isEqual(x.a, y.a) && scalars.isEqual(x.b, y.b)
    }

    final override fun isZero(x: Complex<T>): Boolean {
        return scalars.isZero(x.a) && scalars.isZero(x.b)
    }



    /*
    Complex number related methods
     */


    final override fun add(x: Complex<T>, y: Complex<T>): Complex<T> {
        return Complex(scalars.add(x.a, y.a), scalars.add(x.b, y.b))
    }

    final override fun negate(x: Complex<T>): Complex<T> {
        return Complex(scalars.negate(x.a), scalars.negate(x.b))
    }

    final override fun subtract(x: Complex<T>, y: Complex<T>): Complex<T> {
        return scalars.eval { of(x.a - y.a, x.b - y.b) }
    }


    final override fun scalarMul(k: T, v: Complex<T>): Complex<T> {
        return Complex(scalars.multiply(k, v.a), scalars.multiply(k, v.b))
    }

    fun modSq(z: Complex<T>): T {
        return scalars.eval { z.a * z.a + z.b * z.b }
    }

    inline val Complex<T>.modSq: T
        get() {
            return modSq(this)
        }

    fun conj(z: Complex<T>): Complex<T> {
        return Complex(z.a, scalars.negate(z.b))
    }

    inline val Complex<T>.conj: Complex<T>
        get() {
            return scalars.eval { of(a, -b) }
        }

    final override fun multiply(x: Complex<T>, y: Complex<T>): Complex<T> {
        return scalars.eval { of(x.a * y.a - x.b * y.b, x.a * y.b + x.b * y.a) }
    }



    final override fun multiplyLong(x: Complex<T>, n: Long): Complex<T> {
        return scalars.eval { of(x.a * n, x.b * n) }
    }

    final override fun sum(elements: List<Complex<T>>): Complex<T> {
        return scalars.eval {
            val a = scalars.sum(elements.map { it.a })
            val b = scalars.sum(elements.map { it.b })
            of(a, b)
        }
    }

    /*
    Operator overloading
     */
    operator fun Complex<T>.plus(y: T): Complex<T> {
        return add(this, ofReal(y))
    }

    operator fun T.plus(y: Complex<T>): Complex<T> {
        return add(ofReal(this), y)
    }

    operator fun Complex<T>.minus(y: T): Complex<T> {
        return subtract(this, ofReal(y))
    }

    operator fun T.minus(y: Complex<T>): Complex<T> {
        return subtract(ofReal(this), y)
    }

    operator fun Complex<T>.times(y: T): Complex<T> {
        return multiply(this, ofReal(y))
    }

    operator fun T.times(y: Complex<T>): Complex<T> {
        return multiply(ofReal(this), y)
    }
}



open class ComplexOnField<T>(override val scalars: Field<T>) :
    ComplexOverUnitRing<T>(scalars), Field<Complex<T>>,
    Algebra<T, Complex<T>> {



    override val characteristic: Long?
        get() = scalars.characteristic


    final override fun reciprocal(x: Complex<T>): Complex<T> {
        val d = scalars.eval { x.a * x.a + x.b * x.b }
        return scalars.eval { of(x.a / d, -x.b / d) }
    }

    final override fun divide(x: Complex<T>, y: Complex<T>): Complex<T> {
        val d = scalars.eval { y.a * y.a + y.b * y.b }
        return scalars.eval {
            val t1 = x.a * y.a + x.b * y.b
            val t2 = x.b * y.a - x.a * y.b
            of(t1 / d, t2 / d)
        }
    }

    final override fun scalarDiv(x: Complex<T>, k: T): Complex<T> {
        return scalars.eval { of(x.a / k, x.b / k) }
    }

    /*
    Operator overloading
     */
    operator fun Complex<T>.div(k: T): Complex<T> {
        return scalarDiv(this, k)
    }

    operator fun T.div(v: Complex<T>): Complex<T> {
        return v.inv() * this
    }
}

open class ComplexFromReals<T>(override val reals: Reals<T>) : ComplexOnField<T>(reals), ComplexNumbers<T, Complex<T>> {
    override val scalars: Reals<T>
        get() = reals

    override val characteristic: Long
        get() = 0

    override fun re(z: Complex<T>): T {
        return z.a
    }

    override fun im(z: Complex<T>): T {
        return z.b
    }

    override fun abs(z: Complex<T>): T {
        return reals.eval { sqrt(z.a * z.a + z.b * z.b) }
    }

    //TODO implement special functions

}