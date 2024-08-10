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
interface SemigroupModel<T : Any> : EqualPredicate<T>, BiMathOperator<T> {

    /**
     * Determines whether this semigroup contains the specified element.
     */
    fun contains(element: T): Boolean

    /**
     * The operation of this semigroup, which is associative.
     */
    override fun apply(x: T, y: T): T
}


interface MonoidModel<T : Any> : SemigroupModel<T> {

    /**
     * Gets the identity element of this semigroup.
     *
     * @return the identity element of this monoid.
     */
    val identity: T

}

/**
 * A group is an algebraic structure consisting of a set of elements and an operation.
 *
 *
 * Assume the operation is "*", then
 *
 *  * It is *associative*: `(a*b)*c = a*(b*c)`
 *  * There exists an identity element `e` that: `e*a = a*e = a`
 *  * For every element `a`, there exists an inverse element `a^-1` such that `a*a^-1 = a^-1*a = e`
 *
 *
 *
 * Note that most of the methods defined on the interface are optional and it can throw an UnsupportedOperation
 * if necessary.
 *
 * @author LI Yicheng,  2018-02-27 17:32
 */
interface GroupModel<T : Any> : MonoidModel<T> {

    /**
     * Gets the inverse of the element.
     */
    fun inverse(element: T): T

    /**
     * Determines whether this group is commutative.
     */
    val isCommutative: Boolean

}