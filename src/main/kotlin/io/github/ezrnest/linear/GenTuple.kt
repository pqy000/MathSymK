package io.github.ezrnest.linear

import io.github.ezrnest.Mappable
import java.util.*


/*
 * Created by liyicheng at 2021-04-28 18:38
 */

/**
 * A generic (multidimensional) tuple-like container with finite elements in order.
 */
interface GenTuple<T> : Mappable<T> {
    /**
     * The count of elements contained in this tuple.
     */
    val size: Int

    /**
     * Gets the elements in this generic tuple as a sequence with a fixed order.
     *
     * @see flattenToList
     */
    fun elementSequence(): Sequence<T>

    /**
     * Flatten this generic tuple to a list. The order of the elements is the same as [elementSequence].
     *
     * @see elementSequence
     */
    fun flattenToList(): List<T> {
        val data = ArrayList<T>(size)
        for (s in elementSequence()) {
            data += s
        }
        return data
    }

    /**
     * Returns a new tuple of the same type as the result of applying the given function to each element in `this`.
     */
    override fun <S> map(mapping: (T) -> S): GenTuple<S>

}

/**
 * Returns `true` if all elements in this tuple match the given [predicate].
 *
 */
inline fun <T> GenTuple<T>.all(predicate: (T) -> Boolean): Boolean {
    return elementSequence().all(predicate)
}

/**
 * Returns true if at least one element in this tuple matches the given predicate.
 *
 */
inline fun <T> GenTuple<T>.any(predicate: (T) -> Boolean): Boolean {
    return elementSequence().any(predicate)
}