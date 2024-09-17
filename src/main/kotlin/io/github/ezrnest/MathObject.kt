/**
 * 2018-03-05 20:19
 */
package io.github.ezrnest

import io.github.ezrnest.structure.EqualPredicate
import java.util.function.Function


///**
// * @author liyicheng
// * 2018-03-05 20:25
// */
//interface CalculatorHolder<T, S : EqualPredicate<T>> {
//
//    /**
//     * Return the calculator used by this object.
//     *
//     * @return a calculator
//     */
//    val model: S
//
//}

/**
 * Describes a generic object that can be compared by value.
 *
 */
interface ValueEquatable<T> {



    infix fun valueEquals(obj: ValueEquatable<T>): Boolean
}


interface ModeledMathObject<T, M : EqualPredicate<T>> : ValueEquatable<T> {

    /**
     * Gets the model of this math object.
     */
    val model: M


//    /**
//     * Returns a String representing this object, it is recommended that
//     * the output of the number model should be formatted
//     * through [NumberFormatter.format].
//     * @return
//     */
//    override fun toString(): String

    /**
     * Maps this modeled object to a new one with a different model `newModel`, using the given [mapping] function.
     *
     * **Remark**: Ideally, `M` should be a higher kinded type `M<T>` and `newModel` should be `M<S>`, but Kotlin does not support higher kinded types,
     * so we have to use a workaround with a super class `EqualPredicate<S>`.
     * Users should ensure that `newModel` is of the type `M<S>`.
     *
     *
     * @param newModel a new model with the generic parameter `S`, must be of the same type as `M` (while not enforced by the function signature).
     * @param mapping a function that maps the values of type `T` to new ones of type `S`.
     * It is the user's responsibility to ensure that the mapping is correct.
     */
    fun <S> mapTo(newModel: EqualPredicate<S>, mapping: Function<T, S>): ModeledMathObject<S, *>

}


abstract class AbstractMathObject<T, M : EqualPredicate<T>>(final override val model: M) :
    ModeledMathObject<T, M>
