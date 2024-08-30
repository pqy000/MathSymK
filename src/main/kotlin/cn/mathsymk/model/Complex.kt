package cn.mathsymk.model

import cn.mathsymk.AbstractMathObject
import cn.mathsymk.IMathObject
import cn.mathsymk.MathObject
import cn.mathsymk.model.struct.AlgebraModel
import cn.mathsymk.model.struct.FieldModel
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

class Complex<T : Any>(val a: T, val b: T, model: Ring<T>) : AbstractMathObject<T, Ring<T>>(model),
    FieldModel<Complex<T>>, AlgebraModel<T, Complex<T>> {
    /*
    Created by lyc at 2024/8/29
     */
    override fun valueEquals(obj: IMathObject<T>): Boolean {
        if (obj !is Complex) {
            return false
        }
        return model.isEqual(a, obj.a) && model.isEqual(b, obj.b)
    }

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Complex<N> {
        return Complex(mapper.apply(a), mapper.apply(b), newCalculator as Ring<N>)
    }


    override val isZero: Boolean
        get() = model.isZero(a) && model.isZero(b)

    private fun of(a: T, b: T): Complex<T> {
        return Complex(a, b, model)
    }

    override fun plus(y: Complex<T>): Complex<T> {
        return model.eval { of(a + y.a, b + y.b) }
    }

    override fun unaryMinus(): Complex<T> {
        return model.eval { of(-a, -b) }
    }

    override fun times(y: Complex<T>): Complex<T> {
        return model.eval { of(a * y.a - b * y.b, a * y.b + b * y.a) }
    }

    override fun times(k: T): Complex<T> {
        return model.eval { of(k * a, k * b) }
    }

    override fun inv(): Complex<T> {
        require(model is UnitRing)
        return model.eval {
            val d = a * a + b * b
            of(a / d, -b / d)
        }
    }

    override fun div(k: T): Complex<T> {
        require(model is UnitRing)
        return model.eval { of(a / k, b / k) }
    }

    /*
    Extra methods
     */

    val mod: T
        get() {
            require(model is Reals)
            return model.squareRoot(modSquared)
        }

    val modSquared: T
        get() {
            return model.eval { a * a + b * b }
        }

    val conjugate: Complex<T>
        get() {
            return model.eval { of(a, -b) }
        }
}


/**
 * Complex number, a type of number that can be written as `A+Bi`, where A,B are
 * both real number, and "i" is the square root of `-1`.
 *
 *
 * In this type of number, all calculation will consider that both A and B are real number,
 * and followings are the basic rules.
 *  * Add:<pre> (A+Bi)+(C+Di) = (A+B)+(C+Di)</pre>
 *  * Negate:<pre> -(A+Bi) = -A + (-B)i</pre>
 *  * Subtract:<pre>Z1 - Z2 = Z1 + (-Z2)</pre>
 *  * Multiple:<pre>(A+Bi)*(C+Di)=(AC-BD)+(AD+BC)i</pre>
 *  * Divide:<pre>(A+Bi)/(C+Di)=1/(C^2+D^2)*((AC+BD)+(BC-AD)i)</pre>
 * Operations such as modulus and conjugate are also provided.
 *
 *
 *
 * @author lyc
 *
 */
