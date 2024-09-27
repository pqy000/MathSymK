package io.github.ezrnest.linear

import io.github.ezrnest.Mappable
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.MathUtils
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

inline fun <T> GenTuple<T>.all(predicate: (T) -> Boolean): Boolean {
    for (e in elementSequence()) {
        if (!predicate(e)) return false
    }
    return true
}

typealias Index = IntArray

/**
 * Generic tensor-like container.
 */
interface GenTensor<T> : GenTuple<T> {

    /**
     * The dimension of this tensor, which is equal to the length of [shape].
     */
    val dim: Int get() = shape.size

    /**
     * The total count of elements in this tensor, which is the product of all elements in [shape].
     */
    override val size: Int
        get() = MathUtils.product(shape)

    /**
     * The shape of this tensor, which is an array of integers representing the size of each dimension.
     */
    val shape: IntArray

    operator fun get(idx: Index): T

    override fun <S> map(mapping: (T) -> S): GenTensor<S>

    /**
     * Gets the elements in this generic tuple, iterating from the first dimension to the last as:
     * ```
     * for(i0 in 0 until shape[0]){
     *     for(i1 in 0 until shape[1]){
     *         //...
     *         yield(this[i0, i1, ...])
     *     }
     * }
     * ```
     *
     * @see indices
     * @see flattenToList
     */
    override fun elementSequence(): Sequence<T> {
        return indices.map { this[it] }
    }
}

val GenTensor<*>.shapeString: String
    get() = shape.contentToString()

/**
 * Gets a read-only-traversable sequence of the indices of this tensor,
 * iterating from the first dimension to the last as.
 *
 * This method is generally equal to `IterUtils.prodIdxN(shape)`
 *
 * @see IterUtils.prodIdxN
 */
inline val GenTensor<*>.indices: Sequence<Index>
    get() = IterUtils.prodIdxN(shape)


/**
 * Generic matrix-like container.
 */


fun <T, A : Appendable> GenTensor<T>.joinToL(
    buffer: A, separators: List<CharSequence>, prefixes: List<CharSequence>, postfixes: List<CharSequence>,
    limits: IntArray, truncated: List<CharSequence>, transform: (T) -> CharSequence
): A {
    val dim = this.dim
    val shape = this.shape
    val idx = IntArray(shape.size)
    var level = 0
    Outer@
    while (true) {
        while (idx[level] == shape[level]) {
            buffer.append(postfixes[level])
            idx[level] = 0
            level--
            if (level < 0) {
                break@Outer
            }
            idx[level]++
        }
        if (idx[level] + 1 > limits[level] && idx[level] < shape[level] - 1) {
            buffer.append(separators[level])
            buffer.append(truncated[level])
            idx[level] = shape[level] - 1
        }


        if (idx[level] == 0) {
            buffer.append(prefixes[level])
        } else {
            buffer.append(separators[level])
        }
        if (level == dim - 1) {
            buffer.append(transform(this[idx]))
            idx[level]++
        } else {
            level++
            continue
        }
    }
    return buffer
}


fun <T, A : Appendable> GenTensor<T>.joinTo(
    buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "[", postfix: CharSequence = "]",
    limits: IntArray = IntArray(dim) { Int.MAX_VALUE }, truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    val dim = this.dim
    val seps = run {
        val t = ArrayList<CharSequence>(dim)

        val spaces = " ".repeat(prefix.length)
        var padded = "\n\n"
        for (i in 1 until dim - 1) {
            padded += spaces
            t += padded
        }
        if (dim > 1) {
            t += padded.substring(1) + spaces
        }
        t += separator
        t
    }

    val pres = Collections.nCopies(dim, prefix)
    val posts = Collections.nCopies(dim, postfix)
    val truns = Collections.nCopies(dim, truncated)
    val trans = transform ?: Any?::toString
    return this.joinToL(buffer, seps, pres, posts, limits, truns, trans)
}

fun <T> GenTensor<T>.joinToString(
    separator: CharSequence = ", ", prefix: CharSequence = "[", postfix: CharSequence = "]",
    limit: Int = Int.MAX_VALUE, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null
): String {
    val limits = IntArray(dim) { limit }
    return this.joinTo(StringBuilder(), separator, prefix, postfix, limits, truncated, transform).toString()
}


