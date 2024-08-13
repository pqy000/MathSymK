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
    val calculator: S

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
 * Describes a (computational) math object which holds a subclass of `EqualPredicate` as it 'calculator' for
 * computational purpose.
 *
 * @author liyicheng
 * @see MathObjectReal
 */
interface MathObject<T : Any, S : EqualPredicate<T>> : CalculatorHolder<T, S>, IMathObject<T> {

    /**
     * Gets the calculator of this math object.
     */
    override val calculator: S


//    /**
//     * Returns a String representing this object, it is recommended that
//     * the output of the number model should be formatted
//     * through [NumberFormatter.format].
//     * @return
//     */
//    override fun toString(): String

    /**
     * Maps this math object to use a new calculator.
     *
     * @param newCalculator a calculator that is of the same type as `S` but with generic parameter `N`.
     */
    fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): MathObject<N, *>

}


abstract class AbstractMathObject<T : Any, S : EqualPredicate<T>>(override val calculator: S) : MathObject<T, S> {
//    override fun toString(): String {
//        return toString(NumberFormatter.defaultFormatter())
//    }


}
