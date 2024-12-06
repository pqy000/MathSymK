package io.github.ezrnest.mathsymk.linear

/*
 * Created at 2019/9/12 11:11
 *
 * Specified by lyc at 2021-03-31 22:26
 *
 * Updated by lyc on 2024/9/27
 *
 * @author  lyc
 */

import io.github.ezrnest.mathsymk.discrete.Permutation
import io.github.ezrnest.mathsymk.structure.*
import io.github.ezrnest.mathsymk.util.IterUtils
import io.github.ezrnest.mathsymk.util.MathUtils
import java.util.*
import kotlin.sequences.all


/**
 * The type of the index in a tensor, which is an array of integers.
 */
typealias Index = IntArray


/**
 * Represents a tensor, namely a multidimensional array, of elements of type [T].
 * The tensor's [shape] contains the length of each dimension and [dim]` == shape.size` is the number of dimensions,
 * while [size] is the total number of elements in this tensor.
 * The elements in a tensor can be got by a multidimensional [Index], which is an array of integers.
 *
 * The interface [Tensor] itself serves only for the data structure and the basic structure operations like slicing, reshaping, etc.,
 * but it does not provide arithmetic operations since it depends on the underlying model.
 * To perform arithmetic operations, one should use [Tensor.over] to create a context for tensor operations with a specific model.
 *
 * ### Broadcasting
 *
 * Broadcasting is a mechanism that allows tensors of different shapes to be combined in element-wise operations.
 * The broadcasting rule is as follows:
 *
 * 1. If the two tensors have a different number of dimensions, the shape of the tensor with fewer dimensions is padded with ones on its left side.
 * 2. If the shape of the two tensors does not match in any dimension, the tensor with shape equal to 1 in that dimension is stretched to match the other tensor's shape.
 * 3. If in any dimension the sizes disagree and neither is equal to 1, an exception is raised.
 *
 *
 *
 *
 *
 *
 * @see MutableTensor
 * @see Tensor.over
 */
interface Tensor<T> : GenTuple<T> {
    // Created by lyc at 2021-04-06 22:12
    // Updated by lyc at 2024/9/27

    /**
     * The dimension of this tensor, which is equal to the length of [shape].
     */
    val dim: Int get() = shape.size

    /**
     * The total count of elements in this tensor, which is the product of all the elements in [shape].
     */
    override val size: Int
        get() = MathUtils.product(shape)


    /**
     * Gets the shape of this tensor as an array of integers, whose length is equal to the [dimension][Tensor.dim] of this tensor.
     *
     * **Note**: Users should not modify the returned array.
     *
     * @see dim
     * @see size
     */
    val shape: IntArray

    /**
     * Returns the length of this tensor at the given axis.
     */
    fun lengthAt(axis: Int): Int {
        require(axis in 0 until dim)
        return shape[axis]
    }


    /**
     * Gets an element in this tensor according to the index.
     *
     * @param idx the index, it is required that `0 <= idx < shape` (element-wise).
     */
    operator fun get(idx: Index): T

    /**
     * Returns a sequence of elements in this tensor, iterating from the first dimension to the last.
     * See the following code:
     * ```
     * for (i0 in 0 until shape[0]) {
     *     for (i1 in 0 until shape[1]) {
     *         ...
     *         yield(get(i0, i1, ...))
     *     }
     * }
     * ```
     */
    override fun elementSequence(): Sequence<T> {
        return indices.map { get(it) }
    }

    /**
     * Flatten this tensor to a list. The order of the elements is the same as [elementSequence].
     *
     * @see elementSequence
     */
    override fun flattenToList(): List<T> {
        val size = this.size
        val data = ArrayList<T>(size)
        for (s in elementSequence()) {
            data += s
        }
        return data
    }

    /**
     * Returns a new tensor of applying the given function to this tensor element-wise.
     */
    override fun <S> map(mapping: (T) -> S): Tensor<S> {
        return ATensor.buildFromSequence(shape, elementSequence().map(mapping))
    }

    /**
     * Returns a new tensor of applying the given function to this tensor element-wise with the index.
     */
    fun <S> mapIndexed(mapping: (Index, T) -> S): Tensor<S> {
        return ATensor.buildFromSequence(shape, indices.zip(elementSequence()).map { (idx, t) -> mapping(idx, t) })
    }

    /**
     * Returns an independent copy of this tensor.
     */
    fun copy(): Tensor<T> {
        return ATensor.copyOf(this)
    }


    /*
    Tensor structure operations
     */


    /**
     * Returns a tensor view of diagonal elements in the given two axes with offsets.
     * Assume the given axes are the last two, then formally we have
     *
     *     result[..., i] = this[..., i, i + offset]
     *
     * The shape of the resulting tensor is determined by removing the two axes in the
     * original tensor and add a new axis at the end. The length of the new axis is
     * the same as the size of the resulting diagonals.
     *
     * If this tensor is 2-D, then this method is generally the same as selecting the
     * diagonal of a matrix.
     *
     * The default parameter returns the diagonal elements in the last two axes with no offset.
     *
     * @param offset the offset of the diagonal. If `offset == 0`, the main diagonal is returned. If `offset > 0`,
     * then the resulting diagonal is above the main diagonal.
     *
     */
    fun diagonal(offset: Int = 0, axis1: Int = -2, axis2: Int = -1): Tensor<T> {
        return TensorImpl.diagonal(this, axis1, axis2, offset)
    }


    /**
     * Returns a view of this tensor according to the given slicing parameters.
     * It is required that an element is either
     *  * an integer, `Int`: to get the corresponding elements in that axis and remove the axis in the result tensor,
     *    supporting negative indices;
     *  * a range, `IntProgression`: to get a slice of elements in that axis;
     *  * `null`: to keep the axis as it is;
     *  * a special object, [Tensor.NEW_AXIS]: to indicate a new axis should be inserted;
     *  * a special object, [Tensor.DOTS]: (it should appear at most once)
     *    to indicate zero or more omitted axes that will be kept the same.
     *    These axes are computed according to other slicing parameters.
     *
     * Special case: If all the axes are selected with an index, then the result tensor will be a 0-D scalar tensor.
     *
     * An extension function is provided to make the slicing more concise.
     * Import an overload of [get][io.github.ezrnest.linear.get] with `io.github.ezrnest.linear.get` to use it.
     */
    fun slice(vararg slices: Any?): Tensor<T> {
        return slice(slices.asList())
    }

    /**
     * Returns a view of this tensor according to the slicing ranges or indices.
     *
     * @see [slice]
     */
    fun slice(slices: List<Any?>): Tensor<T> {
        val (am, ranges, ns) = TensorImpl.computeSliceView(this, slices)
        return SlicedView(this, ranges, am, ns)
    }

    /**
     * Inserts a new axis of length 1 at the last of this tensor.
     * The result is a view.
     *
     */
    fun newAxisAt(axis: Int = -1): Tensor<T> {
        val (am, ranges, ns) = TensorImpl.newAxisSliceView(this, axis)
        return SlicedView(this, ranges, am, ns)
    }


    /**
     * Reshapes this tensor to be a view of the given shape. The resulting tensor will have the same element sequence as this tensor.
     *
     * At most one `-1` can appear in the given new shape
     * indicating the length of this dimension should be computed accordingly.
     *
     *
     * @param newShape an array containing positive integers except at most one `-1`.
     * If no `-1` is given, then the product of the new shape should be equal to the size of this tensor;
     * otherwise, the product of the new shape should be a divisor of the size of this tensor.
     * An empty array is allowed, which will reshape this tensor to a scalar tensor provided that the size of this tensor is 1.
     *
     * @throws IllegalArgumentException if the given shape is invalid.
     *
     * @see ravel
     * @see squeeze
     */
    fun reshape(vararg newShape: Int): Tensor<T> {
        val sh = newShape.copyOf()
        TensorImpl.prepareNewShape(this, sh)
        return ReshapedView(this, sh)
    }

    /**
     * Reshapes this tensor to be 1-d tensor. This method is equal to `this.reshape(-1)`.
     *
     * @see reshape
     * @see squeeze
     */
    fun ravel(): Tensor<T> {
        return reshape(-1)
    }

    /**
     * Squeezes the tensor by removing all the axes of length 1.
     */
    fun squeeze(): Tensor<T> {
        if (this.dim == 0) return this
        val (am, ranges, ns) = TensorImpl.prepareSqueezeAll(this)
        return SlicedView(this, ranges, am, ns)
    }

    /**
     * Squeezes the tensor by removing the given [axis] provided that its length is 1.
     *
     * @throws IllegalArgumentException the length at the given [axis] is not 1.
     */
    fun squeeze(axis: Int): Tensor<T> {
        val (am, ranges, ns) = TensorImpl.prepareSqueeze(this, axis)
        return SlicedView(this, ranges, am, ns)
    }


    /**
     * Broadcasts this tensor to the given [newShape].
     * The rule is as follows:
     *
     * 1. Pad the shape of this tensor with ones on the left side to make it have the same dimension as the given shape.
     * 2. For each axis, if the length of this tensor is 1, then stretch it to the length of the given shape.
     * 3. If the length of this tensor is not 1 and not equal to the length of the given shape, an exception is raised.
     *
     */
    fun broadcastTo(vararg newShape: Int): Tensor<T> {
        return TensorImpl.broadcastTo(this, newShape)
    }

    /**
     * Broadcasts this tensor to the shape of `y`.
     *
     * See the overload [broadcastTo] for the broadcasting rule.
     */
    fun broadcastTo(y: Tensor<T>): Tensor<T> {
        return TensorImpl.broadcastTo(this, y.shape)
    }


    /**
     * Returns an axis-permuted view of this tensor, placing the `i`-th axis at the `p.apply(i)`-th position.
     * The resulting tensor will have the shape of `p.permute(shape)`.
     */
    fun permute(p: Permutation): Tensor<T> {
        require(p.size == dim)
        val sh = this.shape
        val ranges = shape.map { 0 until it }
        return SlicedView(this, ranges, p.inverse().getArray(), p.permute(sh))
    }

    /**
     * Re-orders the axes in this tensor according to the given `reorderedAxes`.
     * The `i`-th axis in the resulting tensor corresponds to the `reorderedAxes[i ]`-th axis in this tensor.
     * For example, if we have a tensor of shape `(a,b,c)`, then `permute(1,2,0)` will result in a tensor of shape `(b,c,a)`.
     *
     * @param reorderedAxes its size should be equal to `this.dim`.
     */
    fun permute(vararg reorderedAxes: Int): Tensor<T> {
        return permute(Permutation.fromPermuted(*reorderedAxes))
    }

    /**
     * Transposes two axes in this tensor.
     *
     */
    fun transpose(axis1: Int = -1, axis2: Int = -2): Tensor<T> {
        return permute(
            Permutation.swap(
                dim, TensorImpl.addIfNegative(axis1, dim), TensorImpl.addIfNegative(axis2, dim)
            )
        )
    }


    fun argWhere(predicate: (T) -> Boolean): List<Index> {
        return indices.filter { predicate(get(it)) }.toList()
    }


    companion object {

        /**
         * Used in [slice] to indicate a new axis of length one.
         */
        const val NEW_AXIS = "NEW_AXIS"

        /**
         * Used in [slice] to indicate omitted axes.
         */
        const val DOTS = "..."

        private fun checkValidShape(shape: IntArray) {
//            require(shape.isNotEmpty())
            require(shape.all { s -> s > 0 })
        }


        /**
         * Creates a tensor with all zeros.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> zeros(model: AddMonoid<T>, vararg shape: Int): MutableTensor<T> {
            return fill(model.zero, *shape)
        }

        /**
         * Creates a tensor with all ones.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> ones(model: UnitRing<T>, vararg shape: Int): MutableTensor<T> {
            return fill(model.one, *shape)
        }

        /**
         * Creates a tensor filled with the given constant.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> fill(c: T, vararg shape: Int): MutableTensor<T> {
            checkValidShape(shape)
            return ATensor.constant(c, shape.copyOf())
        }

        /**
         * Returns a tensor of shape `()` that represents the given scalar.
         *
         * The tensor will have `dim == 0` and `size == 1`.
         *
         */
        fun <T> scalar(x: T): MutableTensor<T> {
            return fill(x)
        }

        /**
         * Creates a tensor with a supplier function that takes the index as parameter.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> of(vararg shape: Int, supplier: (Index) -> T): MutableTensor<T> {
            return ofShaped(shape, supplier)
        }

        private fun <T> ofShaped(shape: IntArray, supplier: (Index) -> T): MutableTensor<T> {
            checkValidShape(shape)
            return ATensor.buildFromSequence(shape.copyOf(), IterUtils.prodIdxNoCopy(shape).map(supplier))
        }


        /**
         * A constructor-like version of creating a tensor from a supplier.
         *
         * @see ofFlat
         */
        operator fun <T> invoke(vararg shape: Int, supplier: (Index) -> T): MutableTensor<T> {
            return ofShaped(shape, supplier)
        }

        /**
         * Creates a tensor from the given multidimensional list/array. [elements] (and its
         * nesting lists) can contain either elements
         * of type `T` or lists, and the shape in each dimension should be consistent.
         *
         *
         */
        inline fun <reified T> ofNested(elements: List<Any>): MutableTensor<T> {
            return ATensor.fromNestingList(elements, T::class.java)
        }

        /**
         * Creates a tensor of the given [shape] with its [elements], it is required that the length of
         * [elements] is equal to the product of [shape].
         */
        fun <T> ofFlat(shape: IntArray, vararg elements: T): MutableTensor<T> {
            checkValidShape(shape)
            val size = MathUtils.product(shape)
            require(elements.size == size) {
                "$size elements expected but ${elements.size} is given!"
            }
            val data = Arrays.copyOf(elements, size, Array<Any>::class.java)
            return ATensor(shape, data)
        }

        /**
         * Creates a tensor of the given [shape] with a sequence of elements, it is required that the size of
         * [elements] not smaller than the product of [shape].
         */
        fun <T> ofFlat(shape: IntArray, elements: Sequence<T>): MutableTensor<T> {
            checkValidShape(shape)
            return ATensor.buildFromSequence(shape, elements)
        }

        /**
         * Creates a tensor of the given [shape] with an iterable of elements, it is required that the size of
         * [elements] not smaller than the product of [shape].
         */
        fun <T> ofFlat(shape: IntArray, elements: Iterable<T>): MutableTensor<T> {
            return ofFlat(shape, elements.asSequence())
        }

//        /**
//         * Creates a 2-dimensional tensor from a matrix. The `(i,j)`-th element in the returned tensor
//         * is equal to `(i,j)`-th element in `m`.
//         */
//        fun <T> fromMatrix(m: AbstractMatrix<T>): MutableTensor<T> {
//            return ATensor.fromMatrix(m)
//        }

        /**
         * Returns a copy of the given tensor as a mutable tensor.
         */
        fun <T> copyOf(t: Tensor<T>): MutableTensor<T> {
            return ATensor.copyOf(t)
        }

        /**
         * Creates a tensor of the same shape as `t` with elements initialized by the given function.
         */
        fun <T> like(t : Tensor<*>, init: (Index) -> T): MutableTensor<T> {
            return ofShaped(t.shape, init)
        }

        /**
         * Applies the given function to the elements in the two tensors element-wise.
         *
         * @see Tensor.map
         */
        fun <T1, T2, S> map2(t1: Tensor<T1>, t2: Tensor<T2>, f: (T1, T2) -> S): Tensor<S> {
            return TensorImpl.map2(t1, t2, f)
        }


        /**
         * Concatenate several tensors as a view.
         * The tensors must have the same dimensions and their shapes must
         * be equal in all axes except the concatenating axis.
         *
         * For example, concatenating two tensors of shape `(a,b), (a,c)` at axis 1 will result in a
         * tensor of shape `(a,b+c)`.
         */
        fun <T> concat(ts: List<Tensor<T>>, axis: Int = 0): Tensor<T> {
            val (ax, shape) = TensorImpl.prepareConcat(ts, axis)
            return ConcatView(ax, ts, shape)
        }

        /**
         * Concatenate several tensors as a view.
         *
         * @see concat
         */
        fun <T> concat(vararg ts: Tensor<T>, axis: Int = 0): Tensor<T> {
            return concat(ts.asList(), axis)
        }

        /**
         * Concatenate several mutable tensors as a mutable view.
         * The tensors must have the same dimensions and their shapes must
         * be equal in all axes except the concatenating axis.
         *
         * Any changes to the resulting view will be reflected to the original tensors.
         *
         * For example, concatenating two tensors of shape `(a,b), (a,c)` at axis 1 will result in a
         * tensor of shape `(a,b+c)`.
         */
        fun <T> concatM(ts: List<MutableTensor<T>>, axis: Int = 0): MutableTensor<T> {
            val (ax, shape) = TensorImpl.prepareConcat(ts, axis)
            return MutableConcatView(ax, ts, shape)
        }

        /**
         * Concatenate several mutable tensors as a mutable view.
         * Any changes to the resulting view will be reflected to the original tensors.
         *
         * @see concatM
         */
        fun <T> concatM(vararg ts: MutableTensor<T>, axis: Int = 0): MutableTensor<T> {
            return concatM(ts.asList(), axis)
        }

        /**
         * Stacks several tensors on a new axis as a view. It is required that all the given tensors are
         * of the same shape.
         *
         * For example, stacking two tensors of shape `(a,b)` at axis 0 will result in a tensor
         * of shape `(2,a,b)`.
         */
        fun <T> stack(ts: List<Tensor<T>>, axis: Int = 0): Tensor<T> {
            val (ax, shape) = TensorImpl.prepareStack(ts, axis)
            return StackView(ax, ts, shape)
        }

        /**
         * Stacks several tensors on a new axis as a view. It is required that all the given tensors are
         * of the same shape.
         *
         * @see stack
         */
        fun <T> stack(vararg ts: Tensor<T>, axis: Int = 0): Tensor<T> {
            return stack(ts.asList(), axis)
        }

        /**
         * Stacks several mutable tensors on a new axis as mutable view.
         * It is required that all the given tensors are of the same shape.
         *
         * For example, stacking two tensors of shape `(a,b)` at axis 0 will result in a tensor
         * of shape `(2,a,b)`.
         */
        fun <T> stackM(ts: List<MutableTensor<T>>, axis: Int = 0): MutableTensor<T> {
            val (ax, shape) = TensorImpl.prepareStack(ts, axis)
            return MutableStackView(ax, ts, shape)
        }

        /**
         * Stacks several mutable tensors on a new axis as mutable view.
         * It is required that all the given tensors are
         * of the same shape.
         *
         * @see stackM
         */
        fun <T> stackM(vararg ts: MutableTensor<T>, axis: Int = 0): MutableTensor<T> {
            return stackM(ts.asList(), axis)
        }


        /*
        Models
         */

        /**
         * Creates a context for tensor operations with a specific model.
         * This method has several overloads for different models.
         *
         * @param model the model of the elements in the tensor
         * @param shape (optional) the prescribed shape of the tensors
         */
        fun <T> over(model: EqualPredicate<T>, vararg shape: Int): TensorOverEqualPredicate<T> {
            return TensorOverEqualPredicateImpl(model, shape)
        }

        fun <T> over(model: AddMonoid<T>, vararg shape: Int): TensorOverAddMonoid<T> {
            return TensorOverAddMonoidImpl(model, shape)
        }

        fun <T> over(model: AddGroup<T>, vararg shape: Int): TensorOverAddGroup<T> {
            return TensorOverAddGroupImpl(model, shape)
        }

        fun <T> over(model: Ring<T>, vararg shape: Int): TensorOverRing<T> {
            return TensorOverRingImpl(model, shape)
        }

        fun <T> over(model: UnitRing<T>, vararg shape: Int): TensorOverURing<T> {
            return TensorOverURingImpl(model, shape)
        }

        fun <T> over(model: Field<T>, vararg shape: Int): TensorOverField<T> {
            return TensorOverFieldImpl(model, shape)
        }


    }

}

/**
 * A vararg version of get.
 * This method supports negative indices.
 */
operator fun <T> Tensor<T>.get(vararg idx: Int): T {
    for (i in idx.indices) {
        if (idx[i] < 0) {
            idx[i] += lengthAt(i)
        }
    }
    return this[idx]
}

/**
 * The operator-overloading version of the method [Tensor.slice], providing a more concise way to slice a tensor.
 */
operator fun <T> Tensor<T>.get(vararg slices: Any?): Tensor<T> {
    return slice(slices.asList())
}


val Tensor<*>.shapeString: String
    get() = shape.contentToString()

/**
 * Gets a read-only-traversable sequence of the indices of this tensor,
 * iterating from the first dimension to the last as the order described in [Tensor.elementSequence].
 *
 * This method is generally equal to `IterUtils.prodIdxNoCopy(shape)`
 *
 * @see IterUtils.prodIdxNoCopy
 * @see Tensor.elementSequence
 */
inline val Tensor<*>.indices: Sequence<Index>
    get() = IterUtils.prodIdxNoCopy(shape)


fun <T, A : Appendable> Tensor<T>.joinToL(
    buffer: A, separators: List<CharSequence>, prefixes: List<CharSequence>, postfixes: List<CharSequence>,
    limits: IntArray, truncated: List<CharSequence>, transform: (T) -> CharSequence
): A {
    if (this.dim == 0) {
        buffer.append(transform(this[intArrayOf()]))
        return buffer
    }
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


fun <T, A : Appendable> Tensor<T>.joinTo(
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

fun <T> Tensor<T>.joinToString(
    separator: CharSequence = ", ", prefix: CharSequence = "[", postfix: CharSequence = "]",
    limit: Int = Int.MAX_VALUE, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null
): String {
    val limits = IntArray(dim) { limit }
    return this.joinTo(StringBuilder(), separator, prefix, postfix, limits, truncated, transform).toString()
}


/**
 * Determines whether this tensor has the same shape as `y`.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Tensor<*>.isSameShape(y: Tensor<*>): Boolean {
    return shape.contentEquals(y.shape)
}


/**
 * Returns a copy of this tensor as a matrix.
 *
 * It is required that this tensor is 2-dimensional.
 */
fun <T> Tensor<T>.toMatrix(): Matrix<T> {
    require(dim == 2)
    val (row, column) = shape
    return Matrix.fromFlat(row, column, flattenToList())
}

interface MutableTensor<T> : Tensor<T> {


    /**
     * Sets an element in this tensor.
     */
    operator fun set(idx: Index, v: T)

    /**
     * Sets all the elements in this tensor to be the same value `v`.
     */
    fun setAll(v: T) {
        for (idx in indices) {
            set(idx, v)
        }
    }

    /**
     * Sets all the elements in this tensor according to `t`.
     */
    fun setAll(t: Tensor<T>) {
        val t1 = t.broadcastTo(*shape)
        for (idx in indices) {
            set(idx, t1[idx])
        }
    }

    /**
     * Returns a new mutable tensor of applying the given function to this tensor element-wise.
     */
    override fun <S> map(mapping: (T) -> S): MutableTensor<S> {
        return ATensor.buildFromSequence(shape, elementSequence().map(mapping))
    }

    /**
     * Returns a new mutable tensor of applying the given function to this tensor element-wise with the index.
     */
    override fun <S> mapIndexed(mapping: (Index, T) -> S): MutableTensor<S> {
        return ATensor.buildFromSequence(shape, indices.zip(elementSequence()).map { (idx, t) -> mapping(idx, t) })
    }


    /**
     * Performs the element-wise transformation to this mutable tensor in-place.
     *
     * @see Tensor.map
     */
    fun transform(f: (T) -> T) {
        indices.forEach { idx -> this[idx] = f(this[idx]) }
    }

    override fun diagonal(offset: Int, axis1: Int, axis2: Int): MutableTensor<T> {
        return TensorImpl.diagonal(this, axis1, axis2, offset)
    }


    override fun slice(slices: List<Any?>): MutableTensor<T> {
        val (am, ranges, ns) = TensorImpl.computeSliceView(this, slices)
        return MutableSliceView(this, ranges, am, ns)
    }

    override fun slice(vararg slices: Any?): MutableTensor<T> {
        return slice(slices.asList())
    }

    override fun newAxisAt(axis: Int): MutableTensor<T> {
        val (am, ranges, ns) = TensorImpl.newAxisSliceView(this, axis)
        return MutableSliceView(this, ranges, am, ns)
    }

    /**
     * Sets all the element in the slice to be `v`.
     * This method is generally equal to `slice(slices.asList()).setAll(v)`.
     *
     *
     */
    operator fun set(vararg slices: Any?, v: T) {
        if (slices.all { it is Int }) {
            set(IntArray(slices.size) { i -> slices[i] as Int }, v)
        } else {
            slice(slices.asList()).setAll(v)
        }
    }

    /**
     * Sets all the element in the slice to be the same as `v`.
     * This method is generally equal to `slice(slices.asList()).setAll(v)`.
     */
    operator fun set(vararg slices: Any?, v: Tensor<T>) {
        slice(slices.asList()).setAll(v)
    }


    override fun permute(p: Permutation): MutableTensor<T> {
        require(p.size == dim)
        val sh = this.shape
        val ranges = shape.map { 0 until it }
        return MutableSliceView(this, ranges, p.inverse().getArray(), p.permute(sh))
    }

    override fun permute(vararg reorderedAxes: Int): MutableTensor<T> {
        return permute(Permutation.fromPermuted(*reorderedAxes))
    }

    override fun transpose(axis1: Int, axis2: Int): MutableTensor<T> {
        return permute(
            Permutation.swap(
                dim,
                TensorImpl.addIfNegative(axis1, dim),
                TensorImpl.addIfNegative(axis2, dim)
            )
        )
    }


    override fun reshape(vararg newShape: Int): MutableTensor<T> {
        val sh = newShape.copyOf()
        TensorImpl.prepareNewShape(this, sh)
        return MutableReshapedView(this, sh)
    }

    override fun ravel(): MutableTensor<T> {
        return reshape(-1)
    }

    override fun squeeze(): MutableTensor<T> {
        if (this.dim == 0) return this
        val (am, ranges, ns) = TensorImpl.prepareSqueezeAll(this)
        return MutableSliceView(this, ranges, am, ns)
    }

    override fun squeeze(axis: Int): MutableTensor<T> {
        val (am, ranges, ns) = TensorImpl.prepareSqueeze(this, axis)
        return MutableSliceView(this, ranges, am, ns)
    }


    /**
     * Returns an independent copy of this mutable tensor as a mutable tensor.
     */
    override fun copy(): MutableTensor<T> {
        return ATensor.copyOf(this)
    }
}

/**
 * Returns a mutable copy of this tensor.
 */
fun <T> Tensor<T>.toMutable(): MutableTensor<T> {
    return ATensor.copyOf(this)
}

interface TensorsShaped {
    // created on 2024/9/27
    /**
     * The prescribed dimension, `dim = shape.size`.
     */
    val dim: Int
        get() = shape.size

    /**
     * The prescribed shape.
     */
    val shape: IntArray
}

interface TensorOverEqualPredicate<T> : EqualPredicate<Tensor<T>>, TensorsShaped {
    /**
     * The model of the elements in this tensor.
     */
    val model: EqualPredicate<T>

    override fun isEqual(x: Tensor<T>, y: Tensor<T>): Boolean {
        require(x.shape.contentEquals(y.shape))
        return x.elementSequence().zip(y.elementSequence()).all { (a, b) -> model.isEqual(a, b) }
    }
}

interface TensorOverAddMonoid<T> : TensorOverEqualPredicate<T>, AddMonoid<Tensor<T>> {
    override val model: AddMonoid<T>

    override fun contains(x: Tensor<T>): Boolean = true


    /**
     * Returns the zero tensor of the prescribed [shape].
     */
    override val zero: Tensor<T>
        get() = Tensor.zeros(model, *shape)

    override fun isZero(x: Tensor<T>): Boolean {
        return x.all { model.isZero(it) }
    }

    /**
     * Gets a zero tensor with the given shape.
     */
    fun zeros(vararg shape: Int): MutableTensor<T> {
        return Tensor.zeros(model, *shape)
    }

    /**
     * Gets a zero tensor with the same shape as `x`.
     */
    fun zerosLike(x: Tensor<T>): MutableTensor<T> {
        return Tensor.zeros(model, *x.shape)
    }

    /**
     * Returns the sum of this tensor and `y` with automatic broadcasting.
     *
     * The sum of two tensor `x,y` has the
     * shape of `max(x.shape, y.shape)`, here `max` means element-wise maximum of two arrays.
     */
    override fun add(x: Tensor<T>, y: Tensor<T>): Tensor<T> {
        return TensorImpl.add(x, y, model)
    }

    /**
     * Returns the element-wise sum of this tensor and `y` with automatic broadcasting.
     */
    override fun Tensor<T>.plus(y: Tensor<T>): Tensor<T> {
        return add(this, y)
    }

    override fun sumOf(elements: List<Tensor<T>>): Tensor<T> {
//        return super.sum(elements)
        if (elements.isEmpty()) {
            return zero
        }
        val res = elements[0].toMutable()
        for (i in 1 until elements.size) {
            res += elements[i]
        }
        return res
    }

    override fun multiplyN(x: Tensor<T>, n: Long): Tensor<T> {
        return TensorImpl.multiplyN(x, n, model)
    }

    /**
     * Adds a scalar to this tensor element-wise.
     */
    fun addScalar(x: Tensor<T>, k: T): Tensor<T> {
        return TensorImpl.addScalar(x, k, model)
    }

    /**
     * Adds a scalar to this tensor element-wise.
     */
    fun addScalar(k: T, x: Tensor<T>): Tensor<T> {
        return TensorImpl.addScalar(k, x, model)
    }

    operator fun Tensor<T>.plus(k: T): Tensor<T> {
        return addScalar(this, k)
    }

    operator fun T.plus(x: Tensor<T>): Tensor<T> {
        return addScalar(this, x)
    }

    /**
     * Returns the sum of all the elements in this tensor.
     */
    fun Tensor<T>.sumAll(): T {
        return TensorImpl.sumAll(this, model)
    }

    /**
     * Returns the sum of elements in the given axis(axes). If the given axes is empty, then it will
     * return the sum of all the elements as a scalar tensor.
     *
     * For example, assume the tensor `t = [[0,1,2],[2,3,4]]`, then `t.sum(0) = [2,4,6]`.
     *
     * @param axes the axes to sum, negative indices are allowed to indicate axes from the last.
     *
     */
    fun Tensor<T>.sum(vararg axes: Int): Tensor<T> {
        return TensorImpl.sum(this, axes.asList(), model)
    }

    /**
     * Returns a tensor containing the sums of diagonal elements in this tensor.
     *
     * This method is generally equivalent to
     *
     *     diagonal(offset,axis1,axis2).sum(-1)
     *
     * @see Tensor.diagonal
     */
    fun Tensor<T>.trace(offset: Int = 0, axis1: Int = -2, axis2: Int = -1): Tensor<T> {
        return diagonal(offset, axis1, axis2).sum(-1)
    }

    /*
    Mutable tensor
     */

    operator fun MutableTensor<T>.plusAssign(y: Tensor<T>) {
        TensorImpl.inPlaceAdd(this, y, model)
    }

    operator fun MutableTensor<T>.plusAssign(k: T) {
        TensorImpl.inPlaceAddScalar(this, k, model)
    }

    operator fun MutableTensor<T>.timesAssign(n: Long) {
        TensorImpl.inPlaceMultiplyN(this, n, model)
    }
}

interface TensorOverAddGroup<T> : TensorOverAddMonoid<T>, AddGroup<Tensor<T>> {
    override val model: AddGroup<T>

    override fun negate(x: Tensor<T>): Tensor<T> {
        return TensorImpl.negate(x, model)
    }

    /**
     * Returns the difference of this tensor and `y` with automatic broadcasting.
     */
    override fun subtract(x: Tensor<T>, y: Tensor<T>): Tensor<T> {
        return TensorImpl.subtract(x, y, model)
    }

    /**
     * Returns the element-wise difference of this tensor and `y` with automatic broadcasting.
     */
    override fun Tensor<T>.minus(y: Tensor<T>): Tensor<T> {
        return subtract(this, y)
    }

    override fun multiplyN(x: Tensor<T>, n: Long): Tensor<T> {
        return TensorImpl.multiplyN(x, n, model)
    }

    operator fun MutableTensor<T>.minusAssign(y: Tensor<T>) {
        return TensorImpl.inPlaceSubtract(this, y, model)
    }
}

interface TensorOverRing<T> : TensorOverAddGroup<T>, Ring<Tensor<T>>, RingModule<T, Tensor<T>> {
    override val model: Ring<T>

    override fun contains(x: Tensor<T>): Boolean = true

    override val scalars: Ring<T>
        get() = model

    override val zero: Tensor<T>
        get() = Tensor.zeros(model, *shape)

    override fun scalarMul(k: T, v: Tensor<T>): Tensor<T> {
        return TensorImpl.multiplyScalar(v, k, model)
    }

    /**
     * Returns the **element-wise** product of `x` and `y` with automatic broadcasting.
     */
    override fun multiply(x: Tensor<T>, y: Tensor<T>): Tensor<T> {
        return TensorImpl.multiply(x, y, model)
    }

    /**
     * Returns the **element-wise** multiplication of this tensor and `y` with automatic broadcasting.
     */
    override fun Tensor<T>.times(y: Tensor<T>): Tensor<T> {
        return multiply(this, y)
    }

    operator fun Tensor<T>.times(k: T): Tensor<T> {
        return TensorImpl.multiplyScalar(this, k, model)
    }

    operator fun T.times(x: Tensor<T>): Tensor<T> {
        return TensorImpl.multiplyScalar(x, this, model)
    }


    /**
     * Returns the matrix multiplication of this and [y].
     *
     * To perform matrix multiplication of rank `r` for two tensors `x,y`, first it is
     * required that the last `r` dimensions of `x` and first `r` dimensions of `y` have
     * the same shape.
     * The resulting tensor `z` has the shape of `x.shape[:-r] + y.shape[r:]`.
     *
     * Denote `i, j, k` indices of length `x.dim-r, y.dim-r, r` respectively, the following
     * equation is satisfied:
     * ```
     *     z[i,j] = sum(k, x[i,k] * y[k,j])
     * ```
     *
     * @see times
     * @see wedge
     * @see inner
     * @see einsum
     *
     */
    fun Tensor<T>.matmul(y: Tensor<T>, r: Int): Tensor<T> {
        return TensorImpl.matmul(this, y, r, model)
    }

    /**
     * Returns the matrix multiplication of this and [y] of rank 1.
     *
     * It is required that the last dimension of this tensor is equal to the first dimension of `y`.
     * The resulting tensor `z` has the shape of `this.shape[:-1] + y.shape[1:]`.
     * Denote `i, j, k` indices of length `x.dim-1, y.dim-1, 1` respectively, we have
     * ```
     *     z[i,j] = sum(k, x[i,k] * y[k,j])
     * ```
     *
     * @see times
     * @see wedge
     * @see inner
     * @see einsum
     *
     */
    infix fun Tensor<T>.matmul(y: Tensor<T>): Tensor<T> {
        return TensorImpl.matmul(this, y, 1, model)
    }


    /**
     * Returns the wedge product of this tensor and [y].
     *
     * The tensor product of two tensor `z = x⊗y` has the
     * shape of `x.shape + y.shape`, here `+` means concatenation of two arrays.
     *
     * The `[i,j]` element of `z` is equal to the scalar product of `x[ i ]` and `y[ j ]`, that is,
     *
     *     z[i,j] = x[i] * y[j]
     *
     *
     * @see times
     * @see inner
     * @see matmul
     * @see einsum
     */
    infix fun Tensor<T>.wedge(y: Tensor<T>): Tensor<T> {
        return TensorImpl.wedge(this, y, model)
    }

    /**
     * Returns the inner product of this tensor and [y], which is the sum of the element-wise product of this and `y`.
     *
     * @see times
     * @see wedge
     * @see matmul
     * @see einsum
     */
    infix fun Tensor<T>.inner(y: Tensor<T>): T {
        return TensorImpl.inner(this, y, model)
    }

    /**
     * Returns the einsum of several tensors defined by the given expression.
     *
     *
     * Examples:
     *
     *      | Expression | Required Shapes | Result Shape | Description                 | Equivalent Method
     *      |------------|-----------------|--------------|-----------------------------|-------------------------
     *      | i->i       | (a)             | (a)          | identity                    | x
     *      | i->        | (a)             | (1)          | sum                         | x.sum()
     *      | ij->ji     | (a,b)           | (b,a)        | transpose                   | x.transpose()
     *      | ii         | (a,a)           | (1)          | trace                       | x.trace()
     *      | ii->i      | (a,a)           | (a)          | diagonal                    | x.diagonal()
     *      | ij,ij->ij  | (a,b),(a,b)     | (a,b)        | element-wise multiplication | x.multiply(y)
     *      | ij,jk->ik  | (a,b),(b,c)     | (a,c)        | matrix multiplication       | x.matmul(y)
     *
     *
     *
     * @see times
     * @see wedge
     * @see inner
     * @see matmul
     */
    fun einsum(expr: String, vararg tensors: Tensor<T>): MutableTensor<T> {
        return TensorImpl.einsum(tensors.asList(), expr, model)
    }

    operator fun MutableTensor<T>.timesAssign(k: T) {
        TensorImpl.inPlaceMultiplyScalar(this, k, model)
    }

    operator fun MutableTensor<T>.timesAssign(y: Tensor<T>) {
        TensorImpl.inPlaceMultiply(this, y, model)
    }
}

interface TensorOverURing<T> : TensorOverRing<T>, UnitRingModule<T, Tensor<T>> {
    override val model: UnitRing<T>

    /**
     * Gets the tensor of all ones in the prescribed shape.
     *
     * It is the unit element with respect to the element-wise multiplication.
     */
    override val one: Tensor<T>
        get() = Tensor.ones(model, *shape)

    /**
     * Gets a tensor of all ones with the given shape.
     */
    fun ones(vararg shape: Int): MutableTensor<T> {
        return Tensor.ones(model, *shape)
    }

    /**
     * Gets a tensor of all ones with the same shape as `x`.
     */
    fun onesLike(x: Tensor<T>): MutableTensor<T> {
        return Tensor.ones(model, *x.shape)
    }
}

interface TensorOverField<T> : TensorOverURing<T>, Field<Tensor<T>>, UnitAlgebra<T, Tensor<T>> {
    override val model: Field<T>

    override val scalars: Field<T>
        get() = model

    override val characteristic: Long?
        get() = model.characteristic


    /**
     * Returns the **element-wise** multiplication of `x` and `y` with automatic broadcasting.
     */
    override fun divide(x: Tensor<T>, y: Tensor<T>): Tensor<T> {
        return TensorImpl.divide(x, y, model)
    }

    /**
     * Returns the **element-wise** inverse of `x`.
     */
    override fun reciprocal(x: Tensor<T>): Tensor<T> {
        return TensorImpl.reciprocal(x, model)
    }

    fun divide(x: T, y: Tensor<T>): Tensor<T> {
        return TensorImpl.divideScalarBy(x, y, model)
    }

    operator fun Tensor<T>.div(k: T): Tensor<T> {
        return TensorImpl.divideScalar(this, k, model)
    }

    operator fun T.div(x: Tensor<T>): Tensor<T> {
        return TensorImpl.divideScalarBy(this, x, model)
    }

    operator fun MutableTensor<T>.divAssign(y: Tensor<T>) {
        TensorImpl.inPlaceDivide(this, y, model)
    }

    operator fun MutableTensor<T>.divAssign(k: T) {
        TensorImpl.inPlaceDivideScalar(this, k, model)
    }
}

internal open class TensorShapedImpl(final override val shape: IntArray) : TensorsShaped {
}

internal class TensorOverEqualPredicateImpl<T>(override val model: EqualPredicate<T>, shape: IntArray) :
    TensorShapedImpl(shape), TensorOverEqualPredicate<T>

internal class TensorOverAddMonoidImpl<T>(override val model: AddMonoid<T>, shape: IntArray) :
    TensorShapedImpl(shape), TensorOverAddMonoid<T>

internal class TensorOverAddGroupImpl<T>(override val model: AddGroup<T>, shape: IntArray) :
    TensorShapedImpl(shape), TensorOverAddGroup<T>

internal class TensorOverRingImpl<T>(override val model: Ring<T>, shape: IntArray) :
    TensorShapedImpl(shape), TensorOverRing<T>

internal class TensorOverURingImpl<T>(override val model: UnitRing<T>, shape: IntArray) :
    TensorShapedImpl(shape), TensorOverURing<T>

internal class TensorOverFieldImpl<T>(override val model: Field<T>, shape: IntArray) :
    TensorShapedImpl(shape), TensorOverField<T>

