package io.github.ezrnest.mathsymk.discrete

import io.github.ezrnest.mathsymk.structure.PartialOrder

/**
 * Describes a partially ordered set.
 */
interface Poset<T> : Set<T>{

    val order : PartialOrder<T>


    /**
     * Returns the set of all elements that are less than x: `{y | y < x}`.
     */
    fun lower(x : T) : List<T>{
        return filter { order.less(it, x) }
    }

    /**
     * Returns the set of all elements that are greater than x: `{y | y > x}`.
     */
    fun upper(x : T) : List<T>{
        return filter { order.greater(it, x) }
    }

    /**
     * Returns the set of all elements that are less than or equal to x: `{y | y <= x}`.
     */
    fun lowerEq(x : T) : List<T>{
        return filter { order.lessEqual(it, x) }
    }

    /**
     * Returns the set of all elements that are greater than or equal to x: `{y | y >= x}`.
     */
    fun upperEq(x : T) : List<T>{
        return filter { order.greaterEqual(it, x) }
    }

    /**
     * Returns the set of all elements that are comparable to x: `{y | y ~ x}`.
     */
    fun comparable(x : T) : List<T>{
        return filter { order.isEqual(it, x) }
    }

    /**
     * Returns `true` if x is a maximal element in the poset, namely, there is no element y such that `x < y`.
     */
    fun isMaximal(x : T) : Boolean{
        return !any { order.greater(it, x) }
    }

    /**
     * Returns `true` if x is a minimal element in the poset, namely, there is no element y such that `x > y`.
     */
    fun isMinimal(x : T) : Boolean{
        return !any { order.less(it, x) }
    }

    /**
     * Returns the set of all elements that are maximal in the poset.
     */
    fun maximal() : List<T>{
        return filter { isMaximal(it) }
    }

    /**
     * Returns the set of all elements that are minimal in the poset.
     */
    fun minimal() : List<T>{
        return filter { isMinimal(it) }
    }
}


interface MutablePoset<T> : Poset<T>, MutableSet<T>{
    override fun add(element: T): Boolean
}

internal class MutablePosetImpl<T>(private val set : MutableSet<T>, override val order : PartialOrder<T>) : MutablePoset<T>, MutableSet<T> by set{
}
