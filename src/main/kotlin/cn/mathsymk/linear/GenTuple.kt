package cn.mathsymk.linear

import cn.mathsymk.util.IterUtils
import cn.mathsymk.util.MathUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


/*
 * Created by liyicheng at 2021-04-28 18:38
 */

/**
 * A generic (multidimensional) tuple-like container with finite elements in order.
 */
interface GenTuple<T> {
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
        val size = this.size
        val data = ArrayList<T>(size)
        for (s in elementSequence()) {
            data += s
        }
        return data
    }

    /**
     * Returns a new tuple of the same type as the result of applying the given function to each element in this.
     */
    fun applyAll(f: (T) -> T): GenTuple<T>
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

    override fun applyAll(f: (T) -> T): GenTensor<T>

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
interface GenMatrix<T> : GenTuple<T> {

    /**
     * The count of rows in this matrix.
     */
    val row: Int

    /**
     * The count of columns in this matrix.
     */
    val column: Int


    override val size: Int
        get() = row * column

    /**
     * Gets the shape of this matrix: `(row, column)`.
     */
    val shape: Pair<Int, Int>
        get() = row to column

    operator fun get(i: Int, j: Int): T

    override fun applyAll(f: (T) -> T): GenMatrix<T>

    /**
     * Gets the elements in this generic matrix, iterating row first and then column as:
     * ```
     * for(i in 0 until row){
     *     for(j in 0 until column){
     *         yield(this[i, j])
     *     }
     * }
     * ```
     */
    override fun elementSequence(): Sequence<T> {
        return IterUtils.prodIdx(intArrayOf(row, column)).map { (i, j) -> this[i, j] }
    }


    /**
     * Determines whether this matrix is the same shape as [y].
     */
    fun shapeMatches(y: GenMatrix<*>): Boolean {
        return row == y.row && column == y.column
    }


}

/**
 * Determines whether this matrix is a square matrix.
 */
inline val GenMatrix<*>.isSquare: Boolean get() = (row == column)

fun GenMatrix<*>.requireSquare() {
    require(isSquare) {
        "This matrix should be square! Row=$row, Column=$column."
    }
}


inline val GenMatrix<*>.rowIndices: IntRange
    get() = 0..<row

inline val GenMatrix<*>.colIndices: IntRange
    get() = 0..<column

/**
 * Gets a read-only-traversable sequence of the indices of this matrix, iterating row first and then column as:
 * ```
 * for(i in 0 until row){
 *     for(j in 0 until column){
 *         yield(i to j)
 *     }
 * }
 * ```
 */
inline val GenMatrix<*>.indices: Sequence<Pair<Int, Int>>
    get() = IterUtils.prod2(rowIndices, colIndices)


interface GenVector<T> : GenTuple<T> {

    /**
     * Gets the `i`-th element in the generic vector.
     */
    operator fun get(i: Int): T

    /**
     * Returns a list containing all the elements in this generic vector in order.
     */
    fun toList(): List<T>

    override fun flattenToList(): List<T> {
        return toList()
    }

    override fun applyAll(f: (T) -> T): GenVector<T>

    /**
     * Determines whether the two vectors are of the identity size.
     *
     * @param v another vector.
     * @return `true` if they are the identity in size.
     */
    fun isSameSize(v: GenVector<*>): Boolean {
        return size == v.size
    }
}


inline val GenVector<*>.indices: IntRange
    get() = 0..<size


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
        if (idx[level] + 1 > limits[level]) {
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
    limit: Int = Int.MAX_VALUE, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null
): A {
    val dim = this.dim
    val seps = run {
        val t = java.util.ArrayList<CharSequence>(dim)

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
    val limits = IntArray(dim) { limit }
    val truns = Collections.nCopies(dim, truncated)
    val trans = transform ?: Any?::toString
    return this.joinToL(buffer, seps, pres, posts, limits, truns, trans)
}

fun <T> GenTensor<T>.joinToString(
    separator: CharSequence = " ", prefix: CharSequence = "[", postfix: CharSequence = "]",
    limit: Int = Int.MAX_VALUE, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null
): String {
    return this.joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}

fun <T, A : Appendable> GenMatrix<T>.joinTo(
    buffer: A, sepRow: CharSequence = "\n ", sepCol: CharSequence = ", ",
    prefixRow: CharSequence = "[", postfixRow: CharSequence = "]",
    prefixCol: CharSequence = "[", postfixCol: CharSequence = "]",
    limitRow: Int = Int.MAX_VALUE, truncatedRow: CharSequence = "...",
    limitCol: Int = Int.MAX_VALUE, truncatedCol: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    val (row, col) = shape
    buffer.append(prefixRow)
    for (i in 0..<min(row, limitRow)) {
        if (i > 0) buffer.append(sepRow)
        buffer.append(prefixCol)
        for (j in 0..<min(col, limitCol)) {
            if (j > 0) buffer.append(sepCol)
            buffer.append(transform?.invoke(this[i, j]) ?: this[i, j].toString())
        }
        if (col > limitCol) {
            buffer.append(sepCol)
            buffer.append(truncatedCol)
        }
        buffer.append(postfixCol)
    }
    if (row > limitRow) {
        buffer.append(sepRow)
        buffer.append(truncatedRow)
    }
    buffer.append(postfixRow)
    return buffer
}


fun <T> GenMatrix<T>.joinToString(
    sepRow: CharSequence = "\n ", sepCol: CharSequence = ", ",
    prefixRow: CharSequence = "[", postfixRow: CharSequence = "]",
    prefixCol: CharSequence = "[", postfixCol: CharSequence = "]",
    limit: Int = Int.MAX_VALUE, truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    val builder = StringBuilder()
//    rowIndices.joinTo(builder, prefix = "[", postfix = "]", separator = "\n ", limit = limit) { i ->
//        colIndices.joinTo(builder, prefix = "[", postfix = "]", separator = ", ", limit = limit) { j ->
//            this[i, j].toString()
//        }
//        ""
//    }
    joinTo(
        builder, sepRow, sepCol, prefixRow, postfixRow, prefixCol, postfixCol,
        limit, truncated, limit, truncated, // limit for row and column
        transform
    )
    return builder.toString()
}


fun <T, A : Appendable> GenVector<T>.joinTo(
    buffer: A,
    separator: CharSequence = ", ", prefix: CharSequence = "[", postfix: CharSequence = "]",
    limit: Int = Int.MAX_VALUE, truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    return elementSequence().joinTo(buffer, separator, prefix, postfix, limit, truncated, transform)
}

fun <T> GenVector<T>.joinToString(
    separator: CharSequence = ", ", prefix: CharSequence = "[", postfix: CharSequence = "]",
    limit: Int = Int.MAX_VALUE, truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    return elementSequence().joinToString(separator, prefix, postfix, limit, truncated, transform)
}

