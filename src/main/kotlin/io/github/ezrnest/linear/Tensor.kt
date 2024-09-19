package io.github.ezrnest.linear

import io.github.ezrnest.ValueEquatable
import io.github.ezrnest.ModeledMathObject
import io.github.ezrnest.discrete.Permutation
import io.github.ezrnest.model.struct.AlgebraModel
import io.github.ezrnest.structure.*
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.MathUtils
import java.util.*
import java.util.function.Function


/*
 * Created at 2019/9/12 11:11
 *
 * Specified by lyc at 2021-03-31 22:26
 * @author  lyc
 */

/**
 * ## Mathematical Description
 *
 * A tensor is an element in the tensor product space of several (finite dimensional) linear spaces `V_1,...,V_m` on
 * a field `T`. Denote `V = V_1⊗...⊗V_m` the tensor product space, then `V` is also a linear space on `T`.
 *
 * Assume `dim V_i = n_i`, and `v(i,1),...,v(i,n_i)` are the basis of `V_i`.
 * We have `dim V = n_1*...*n_m`, and the basis of `V` are `v(1,r_1)⊗v(2,r_2)⊗...⊗v(m,r_m)`,
 * and each element in `V` can be written as their linear combination.
 *
 * We call the dimensions of the component linear spaces `(n_1,...,n_m)` the shape of the tensor,
 * and `m` the dimension of the tensor.
 *
 * ## Programming Description
 *
 * A tensor can be viewed as a multidimensional array of type `T`. Its shape is the lengths of arrays in the
 * corresponding dimensions. We call each of the dimension an 'axis' and also use 'dimension' to indicate
 * the number of dimensions.
 *
 *
 * Note: this implementation is not intentioned for fast numeric computation.
 */
interface Tensor<T> : ModeledMathObject<T, EqualPredicate<T>>, AlgebraModel<T, Tensor<T>>, GenTensor<T> {
    //Created by lyc at 2021-04-06 22:12

    /**
     * Gets a copy the shape array of this tensor.
     */
    override val shape: IntArray

    /**
     * Returns the length of this tensor at the given axis.
     */
    fun lengthAt(axis: Int): Int {
        require(axis in 0 until dim)
        return shape[axis]
    }

    /**
     * Determines whether this tensor has the same shape as `y`.
     */
    fun isSameShape(y: Tensor<*>): Boolean {
        return shape.contentEquals(y.shape)
    }


    /**
     * Gets the number of elements in this tensor, which is equal to the product of [shape].
     */
    override val size: Int


    /**
     * Gets an element in this tensor according to the index.
     *
     * @param idx the index, it is required that `0 <= idx < shape`
     */
    override operator fun get(idx: Index): T


    /*
    Math operations:
     */

    /**
     * Returns the element-wise sum of this tensor and `y`.
     *
     * The sum of two tensor `x,y` has the
     * shape of `max(x.shape, y.shape)`, here `max` means element-wise maximum of two arrays.
     */
    override fun plus(y: Tensor<T>): Tensor<T> {
        return TensorImpl.add(this, y, model as AddSemigroup<T>)
    }

    /**
     * Returns the negation of this tensor as a new tensor.
     *
     */
    override fun unaryMinus(): Tensor<T> {
        return TensorImpl.negate(this, model as AddGroup<T>)
    }

    /**
     * Returns the sum of this tensor and `y`.
     *
     * The sum of two tensor `x,y` has the
     * shape of `max(x.shape, y.shape)`, here `max` means element-wise maximum of two arrays.
     */
    override fun minus(y: Tensor<T>): Tensor<T> {
        return TensorImpl.subtract(this, y, model as AddGroup<T>)
    }

    /**
     * Returns the result of multiplying this tensor with a scalar as a new tensor.
     */
    override fun scalarMul(k: T): Tensor<T> {
        return TensorImpl.multiply(this, k, model as MulSemigroup<T>)
    }

    override fun timesLong(n: Long): Tensor<T> {
        return TensorImpl.multiplyLong(this, n, model as AddSemigroup<T>)
    }

    /**
     * Returns the result of dividing this tensor with a scalar as a new tensor.
     */
    override fun scalarDiv(k: T): Tensor<T> {
        return TensorImpl.divide(this, k, model as MulGroup<T>)
    }

    /**
     * Returns the **element-wise** product of this tensor and `y`.
     *
     */
    override fun times(y: Tensor<T>): Tensor<T> {
        return TensorImpl.multiply(this, y, model as MulSemigroup<T>)
    }

    /**
     * Returns the **element-wise** division of this tensor and `y`.
     *
     * @throws ArithmeticException if zero-division happens
     */
    fun div(y: Tensor<T>): Tensor<T> {
        return TensorImpl.divide(this, y, model as MulGroup<T>)
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
    infix fun wedge(y: Tensor<T>): Tensor<T> {
        return TensorImpl.wedge(this, y, model as Ring<T>)
    }

    /**
     * Returns the inner product of this tensor and [y], which is the sum of the element-wise product of this and `y`.
     *
     * @see times
     * @see wedge
     * @see matmul
     * @see einsum
     */
    infix fun inner(y: Tensor<T>): T {
        return TensorImpl.inner(this, y, model as Ring<T>)
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
     *
     *     z[i,j] = sum(k, x[i,k] * y[k,j])
     *
     * @see times
     * @see wedge
     * @see inner
     * @see einsum
     *
     */
    fun matmul(y: Tensor<T>, r: Int = 1): Tensor<T> {
        return TensorImpl.matmul(this, y, r)
    }

//    fun tensorDot()


    /**
     * Returns the sum of all the elements in this tensor.
     */
    fun sumAll(): T {
        val mc = model as AddSemigroup<T>
        return elementSequence().reduce(mc::add)
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
    fun sum(vararg axes: Int): Tensor<T> {
        return TensorImpl.sum(this, axes.asList())
    }

    /**
     * Returns a tensor containing the sums of diagonal elements in this tensor.
     *
     * This method is generally equivalent to
     *
     *     diagonal(offset,axis1,axis2).sum(-1)
     *
     * @see diagonal
     */
    fun trace(offset: Int = 0, axis1: Int = -2, axis2: Int = -1): Tensor<T> {
        return diagonal(offset, axis1, axis2).sum(-1)
    }

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
     * @param offset the offset of the diagonal. If `offset > 0`, the main diagonal is returned. If `offset > 0`,
     * then the resulting diagonal is above the main diagonal.
     *
     */
    fun diagonal(offset: Int = 0, axis1: Int = -2, axis2: Int = -1): Tensor<T> {
        return TensorImpl.diagonal(this, axis1, axis2, offset)
    }


    /*
    Array-like operations:
     */

//    /**
//     * Gets the elements in this tensor as a sequence. The order is the same as [indices].
//     *
//     * @see flattenToList
//     */
//    override fun elementSequence(): Sequence<T>

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
    override fun applyAll(f: (T) -> T): Tensor<T> {
        return mapTo(model, f)
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
     * Special case: If all the slicing parameters are integers, then the result tensor will be a 1-D tensor with shape `(1)`.
     *
     * An extension function is provided to make the slicing more concise.
     * Import [get][io.github.ezrnest.linear.get] (with overloads) to use it.
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
     * Inserts a new axis of length 1 at the last of this tensor. The result is
     * a view.
     *
     */
    fun newAxisAt(axis: Int = -1): Tensor<T> {
        val (am, ranges, ns) = TensorImpl.newAxisSliceView(this, axis)
        return SlicedView(this, ranges, am, ns)
    }


    /**
     * Reshapes this tensor to be a view of the given shape.
     *
     * At most one `-1` can appear in the given new shape
     * indicating the length of this dimension should be computed accordingly.
     *
     * The resulting tensor will have the same element sequence as this tensor.
     *
     * @param newShape a non-empty array containing positive integers
     * except at most one element to be `-1`. Its product should be a divisor of the size of this tensor.
     */
    fun reshape(vararg newShape: Int): Tensor<T> {
        val sh = newShape.clone()
        TensorImpl.prepareNewShape(this, sh)
        return ReshapedView(this, sh)
    }

    /**
     * Reshapes this tensor to be 1-d tensor. This method is equal to `this.reshape(-1)`.
     *
     * @see reshape
     */
    fun ravel(): Tensor<T> {
        return reshape(-1)
    }


    /**
     * Broadcasts this tensor to the given shape.
     */
    fun broadcastTo(vararg newShape: Int): Tensor<T> {
        return TensorImpl.broadcastTo(this, newShape)
    }


//    /**
//     * The operator-overloading version of the method [slice].
//     *
//     * @see [slice]
//     */
//    operator fun get(vararg ranges: Any?): Tensor<T>{
//    return slice(ranges.asList())
//    }


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
                dim,
                TensorImpl.addIfNegative(axis1, dim),
                TensorImpl.addIfNegative(axis2, dim)
            )
        )
    }


    /**
     * Returns `true` if all elements in this tensor match the given [predicate].
     *
     */
    fun all(predicate: (T) -> Boolean): Boolean {
        return elementSequence().all(predicate)
    }

    /**
     * Returns true if at least one element in this tensor matches the given predicate.
     *
     */
    fun any(predicate: (T) -> Boolean): Boolean {
        return elementSequence().any(predicate)
    }


    /*
    Vector model for T:
     */


    override fun isLinearRelevant(v: Tensor<T>): Boolean {
        return TensorImpl.isLinearDependent(this, v, model as Field)
    }

    /*
    Math object
     */

    override fun <N> mapTo(newModel: EqualPredicate<N>, mapping: Function<T, N>): Tensor<N> {
        return ATensor.buildFromSequence(newModel, shape, elementSequence().map { mapping.apply(it) })
    }

    override fun valueEquals(obj: ValueEquatable<T>): Boolean {
        if (obj !is Tensor) {
            return false
        }
        val mc = model
        if (!this.isSameShape(obj)) {
            return false
        }
        return elementSequence().zip(obj.elementSequence()).all { (a, b) -> mc.isEqual(a, b) }
    }

//    override fun toString(nf: NumberFormatter<T>): String {
//        return joinTo(StringBuilder()) {
//            nf.format(it)
//        }.toString()
//    }


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
            require(shape.isNotEmpty())
            require(shape.all { s -> s > 0 })
        }

        fun <T> checkShape(x: Tensor<T>, y: Tensor<T>) {
            if (!x.isSameShape(y)) {
                throw IllegalArgumentException("Shape mismatch: ${x.shape.contentToString()} and ${y.shape.contentToString()}.")
            }
        }

        /**
         * Creates a tensor with all zeros.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> zeros(mc: Ring<T>, vararg shape: Int): MutableTensor<T> {
            return constants(mc.zero, mc, *shape)
        }

        /**
         * Creates a tensor with all ones.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> ones(mc: UnitRing<T>, vararg shape: Int): MutableTensor<T> {
            return constants(mc.one, mc, *shape)
        }

        /**
         * Creates a tensor filled with the given constant.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> constants(c: T, mc: EqualPredicate<T>, vararg shape: Int): MutableTensor<T> {
            checkValidShape(shape)
            return ATensor.constant(c, shape.clone(), mc)
        }

        /**
         * Creates a tensor with a supplier function that takes the index as parameter.
         *
         * @param shape a non-empty array of positive integers
         */
        fun <T> of(shape: IntArray, mc: EqualPredicate<T>, supplier: (Index) -> T): MutableTensor<T> {
            checkValidShape(shape)
            return ATensor.buildFromSequence(mc, shape.clone(), IterUtils.prodIdxN(shape).map(supplier))
        }

        /**
         * A constructor-like version of creating a tensor from a supplier.
         *
         * @see of
         */
        operator fun <T> invoke(
            shape: IntArray, mc: EqualPredicate<T>,
            supplier: (Index) -> T
        ): MutableTensor<T> {
            return of(shape, mc, supplier)
        }

        /**
         * Creates a tensor from the given multidimensional list/array. [elements] (and its
         * nesting lists) can contain either elements
         * of type `T` or lists, and the shape in each dimension should be consistent.
         *
         *
         */
        inline fun <reified T> of(elements: List<Any>, mc: EqualPredicate<T>): MutableTensor<T> {
            return ATensor.fromNestingList(elements, mc, T::class.java)
        }

        /**
         * Creates a tensor of the given [shape] with its [elements], it is required that the length of
         * [elements] is equal to the product of [shape].
         */
        fun <T> of(shape: IntArray, mc: EqualPredicate<T>, vararg elements: T): MutableTensor<T> {
            checkValidShape(shape)
            val size = MathUtils.product(shape)
            require(elements.size == size) {
                "$size elements expected but ${elements.size} is given!"
            }
            val data = Arrays.copyOf(elements, size, Array<Any>::class.java)
            return ATensor(mc, shape, data)
        }

        /**
         * Creates a tensor of the given [shape] with a sequence of elements, it is required that the size of
         * [elements] not smaller than the product of [shape].
         */
        fun <T> of(shape: IntArray, mc: EqualPredicate<T>, elements: Sequence<T>): MutableTensor<T> {
            checkValidShape(shape)
            return ATensor.buildFromSequence(mc, shape, elements)
        }

        /**
         * Creates a tensor of the given [shape] with an iterable of elements, it is required that the size of
         * [elements] not smaller than the product of [shape].
         */
        fun <T> of(shape: IntArray, mc: EqualPredicate<T>, elements: Iterable<T>): MutableTensor<T> {
            return of(shape, mc, elements.asSequence())
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
        fun <T> einsum(expr: String, vararg tensors: Tensor<T>): MutableTensor<T> {
            return TensorImpl.einsum(tensors.asList(), expr)
        }


        /**
         * Returns a tensor of shape `(1)` that represents the given scalar.
         */
        fun <T> scalar(x: T, mc: EqualPredicate<T>): MutableTensor<T> {
            return constants(x, mc, 1)
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
operator fun <T> Tensor<T>.get(vararg slices : Any?) : Tensor<T> {
    return slice(slices.asList())
}

infix fun <T> Tensor<T>.matmul(y: Tensor<T>): Tensor<T> = this.matmul(y, r = 1)
infix fun <T> MutableTensor<T>.matmul(y: Tensor<T>): MutableTensor<T> = this.matmul(y, r = 1)

///**
// * Converts this tensor to a matrix. It is required that `dim == 2`.
// */
//fun <T> Tensor<T>.toMatrix(): Matrix<T> {
//    require(dim == 2)
//    val (row, column) = shape
//    return Matrix.of(row, column, calculator as RealCalculator<T>, flattenToList())
//}

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


    operator fun plusAssign(y: Tensor<T>) {
        val mc = model as AddSemigroup<T>
        val y1 = y.broadcastTo(*shape)
        for (idx in indices) {
            this[idx] = mc.add(this[idx], y1[idx])
        }
    }

    operator fun minusAssign(y: Tensor<T>) {
        val mc = model as AddGroup<T>
        val y1 = y.broadcastTo(*shape)
        for (idx in indices) {
            this[idx] = mc.subtract(this[idx], y1[idx])
        }
    }

    operator fun timesAssign(y: Tensor<T>) {
        val mc = model as Ring<T>
        val y1 = y.broadcastTo(*shape)
        for (idx in indices) {
            this[idx] = mc.multiply(this[idx], y1[idx])
        }
    }

    operator fun divAssign(y: Tensor<T>) {
        val mc = model as DivisionRing<T>
        val y1 = y.broadcastTo(*shape)
        for (idx in indices) {
            this[idx] = mc.divide(this[idx], y1[idx])
        }
    }


    override fun applyAll(f: (T) -> T): MutableTensor<T> {
        return mapTo(model, f)
    }

    /**
     * Performs the element-wise transformation to this mutable tensor in-place.
     *
     * @see applyAll
     */
    fun transform(f: (T) -> T) {
        indices.forEach { idx -> this[idx] = f(this[idx]) }
    }

    override fun plus(y: Tensor<T>): MutableTensor<T> {
        return TensorImpl.add(this, y, model as AddSemigroup<T>)
    }

    override fun unaryMinus(): MutableTensor<T> {
        return TensorImpl.negate(this, model as AddGroup<T>)
    }

    override fun minus(y: Tensor<T>): MutableTensor<T> {
        return TensorImpl.subtract(this, y, model as AddGroup<T>)
    }

    override fun scalarMul(k: T): MutableTensor<T> {
        return TensorImpl.multiply(this, k, model as MulSemigroup<T>)
    }

    override fun scalarDiv(k: T): MutableTensor<T> {
        return TensorImpl.multiply(this, k, model as MulGroup<T>)
    }

    override fun times(y: Tensor<T>): MutableTensor<T> {
        return TensorImpl.multiply(this, y, model as MulSemigroup<T>)
    }

    override fun div(y: Tensor<T>): MutableTensor<T> {
        return TensorImpl.divide(this, y, model as MulGroup<T>)
    }


    override infix fun wedge(y: Tensor<T>): MutableTensor<T> {
        return TensorImpl.wedge(this, y, model as Ring<T>)
    }


    override fun matmul(y: Tensor<T>, r: Int): MutableTensor<T> {
        return TensorImpl.matmul(this, y, r)
    }

    override fun sum(vararg axes: Int): MutableTensor<T> {
        return TensorImpl.sum(this, axes.asList())
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
        val sh = newShape.clone()
        TensorImpl.prepareNewShape(this, sh)
        return MutableReshapedView(this, sh)
    }

    override fun ravel(): MutableTensor<T> {
        return reshape(-1)
    }

    fun copy(): MutableTensor<T> {
        return ATensor.copyOf(this)
    }

    override fun <N> mapTo(newModel: EqualPredicate<N>, mapping: Function<T, N>): MutableTensor<N> {
        return ATensor.buildFromSequence(newModel, shape, elementSequence().map { mapping.apply(it) })
    }


}


//fun main() {
//    val mc = Calculators.integer()
//    val shape = intArrayOf(2, 2)
//    val shape2 = intArrayOf(2)
////    val v = Tensor.zeros(mc, *shape)
////    val w = Tensor.ones(mc, *shape)
//    val a = Tensor.of((0..3).toList(),mc).reshape(2,2)
//    println(a)
//    println(a.diagonal())
//    println(a.diagonal(3))
//    val u = Tensor(shape, mc) { idx -> idx.withIndex().sumBy { (1 + it.index) * it.value } }
//    val w = Tensor(shape2, mc) { it[0] + 1 }
////    Tensor.
//    println(u wedge w)
//    println(w)
//
//    println(u + w)
//    println()
//    val z = u.slice(0..1, null).reshape(4, -1)
//    println(z)
//    z[1, 1..2] = 18
//    println(u)
//    val v = u.slice(DOTS, 0..0)
//    println(u.slice(NEW_AXIS, DOTS).shape.contentToString())
//    println(v.shape.contentToString())
//    println(v)
//    val v1 = v.slice(0, null)
//    v1.setAll(1)
//    println(u)
//    println(w)
//    println(w.newAxisAt())

//    println(u.sumAll() + w.sumAll())
//    println(v.sumAll())
//    println(Tensor.einsum("ij->j", u))
//    println(u.sum())
//    println(Tensor.einsum("ijk->ij", u))
//    println(u.sum(-1))
//    println(Tensor.einsum("ijk->k", u))
//    println(u.sum(0, 1))
//    val r = TensorUtils.einsum(listOf(u, w),
//            intArrayOf(3, 3, 3, 3),
//            intArrayOf(1),
//            listOf(intArrayOf(0, 1), intArrayOf(2, 3)),
//            listOf(intArrayOf(), intArrayOf()),
////            listOf(intArrayOf(0,0), intArrayOf(0,1)),
//            mc)
//    println(r)
//    println(r.valueEquals(u.wedge(w)))

//    println(Tensor.einsum("ii", u)) // trace
//    println(Tensor.einsum("ij->", u)) // sum
//    println(Tensor.einsum("ii->i", u)) // diagonal
//    println(Tensor.einsum("ij->ji", u)) // transpose
//
//    println(Tensor.einsum("ij,ij->ij", u, w)) // element-wise multiplication
//
//    println(Tensor.einsum("ij,jk->ik", u, w)) // matrix multiplication

//
//    println()
//    println(u.sum())
//    println("u[0,0,0]=")
//    val w0 = u.slice(0, 0, 2 downTo 1, null, Tensor.NEW_AXIS)
//    println()
////    val w = u.permute(1, 0)
////    println(w)
//    val w2 = u.slice(0, 0)
//    println(w2)
//    println(u.permute(intArrayOf(1,0))[0])
//    u += w
//    println(u)


//}