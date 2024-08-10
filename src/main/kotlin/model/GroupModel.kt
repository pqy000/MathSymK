package cn.mathsymk.model

import cn.mathsymk.function.BiMathOperator


/**
 * Semigroup is the base of almost all the algebraic structure in abstract algebra.
 *
 *
 * A semigroup is composed of a set of elements and an operation defined in the set.
 * Assume the operation is "*".
 *
 *  * It is *associative*: (a*b)*c = a*(b*c)
 *
 *
 * @author liyicheng
 * 2018-02-27 17:09
 */
interface SemigroupModel<T> : EqualPredicate<T>, BiMathOperator<T>{

    /**
     * Determines whether this semigroup contains the specified element.
     */
    fun contains(element: T): Boolean

    /**
     * The operation of this semigroup, which is associative.
     */
    override fun apply(x: T, y: T): T
}


interface MonoidModel<T> : SemigroupModel<T> {

    /**
     * Gets the identity element of this semigroup.
     *
     * @return the identity element of this monoid.
     */
    val identity: T

}


interface GroupModel<T : Any> : MonoidModel<T>{

    /**
     * Gets the inverse of the element.
     */
    fun inverse(element: T): T

    /**
     * Determines whether this group is commutative.
     */
    val isCommutative: Boolean

}