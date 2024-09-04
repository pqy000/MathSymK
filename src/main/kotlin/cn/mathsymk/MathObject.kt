/**
 * 2018-03-05 20:19
 */
package cn.mathsymk

import cn.mathsymk.structure.EqualPredicate
import java.util.function.Function


/**
 * @author liyicheng
 * 2018-03-05 20:25
 */
interface CalculatorHolder<T, S : EqualPredicate<T>> {

    /**
     * Return the calculator used by this object.
     *
     * @return a calculator
     */
    val model: S

}


interface IMathObject<T> {

//    /**
//     * Returns a String representing this object, the [NumberFormatter] should
//     * be used whenever a number is presented.
//     * @param nf a number formatter
//     * @return
//     * @see NumberFormatter
//     */
//    fun toString(nf: NumberFormatter<T>): String

    infix fun valueEquals(obj: IMathObject<T>): Boolean
}

/**
 * Describes a (computational) math object which holds a [model] for its data.
 * The model provides the operations for the math object.
 * For example, we can have polynomials with different coefficients in `Double` or in `Fraction`, which have different models.
 *
 * @author liyicheng
 */
interface MathObject<T, S : EqualPredicate<T>> : CalculatorHolder<T, S>, IMathObject<T> {

    /**
     * Gets the model of this math object.
     */
    override val model: S


//    /**
//     * Returns a String representing this object, it is recommended that
//     * the output of the number model should be formatted
//     * through [NumberFormatter.format].
//     * @return
//     */
//    override fun toString(): String

    /**
     * Maps this math object to use a new model.
     *
     * @param newCalculator a calculator that is of the same type as `S` but with generic parameter `N`.
     */
    fun <N> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): MathObject<N, *>

}


abstract class AbstractMathObject<T, S : EqualPredicate<T>>(override val model: S) : MathObject<T, S> {

}
