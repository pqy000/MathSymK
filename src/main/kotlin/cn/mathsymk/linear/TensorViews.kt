package cn.mathsymk.linear

import cn.mathsymk.discrete.Permutation
import cn.mathsymk.model.struct.Index
import cn.mathsymk.structure.AddSemigroup
import cn.mathsymk.util.ArraySup
import cn.mathsymk.util.IterUtils


/*
 * Created by liyicheng at 2021-04-11 10:04
 */


open class SlicedView<T:Any>(
    tensor: Tensor<T>,
    /**
         * The ranges in t. `ranges.size = t.dim`.
         */
        protected val ranges: List<IntProgression>,
    /**
         * Maps the axis to [tensor]'s axis. `axisMap.size = this.dim`.
         *
         * `axisMap[i ] = -1` means a new axis.
         */
        protected val axisMap: IntArray,

    shape: IntArray,
) : AbstractTensor<T>(tensor.model, shape) {

    protected open val t = tensor

//    init {
//        //check shape
//
//    }

    /**
     * The shifts in the index of t. `shifts.size = t.dim`.
     */
    protected val shifts: IntArray = IntArray(ranges.size) { i -> ranges[i].first }

    protected val steps: IntArray = IntArray(ranges.size) { i -> ranges[i].step }

    /*
    Index Convention:
    idx: in the view
    pos: in the backing tensor
     */

    /**
     * All the indices related to this sliced view in the original tensor.
     *
     * The sequence is not ordered.
     */
    protected val originalIndicesNoOrder: Sequence<Index> = IterUtils.prodIdx(ranges, copy = false)


    protected fun mapIdxTo(idx: Index, pos: Index) {
        // pos[axisMap[i]] + shifts[i]
        shifts.copyInto(pos)
        for (l in 0 until dim) {
            val axis = axisMap[l]
            if (axis < 0) {
                // new axis
                continue
            }
            pos[axis] += idx[l] * steps[axis]
        }
    }

    protected fun mapIdx(idx: Index): Index {
        val pos = IntArray(t.dim)
        mapIdxTo(idx, pos)
        return pos
    }

    override fun getChecked(idx: Index): T {
        val pos = mapIdx(idx)
        return t[pos]
    }


//    override fun elementSequence(): Sequence<T> {
//        return originalIndices.map { pos -> t[pos] }
//    }


    protected fun composeSliceTo(am: IntArray, ranges: List<IntProgression>)
            : Pair<IntArray, MutableList<IntProgression>> {
        for (i in am.indices) {
            if (am[i] >= 0) {
                am[i] = axisMap[am[i]]
            }
        }
        val newRanges = this.ranges.toMutableList()
        for (i in ranges.indices) {
            val r0 = this.ranges[axisMap[i]]
            val r1 = ranges[i]
            val first = r0.first + r1.first * r0.step
            val step = r0.step * r1.step
            val last = r0.first + r1.last * r0.step
            newRanges[axisMap[i]] = IntProgression.fromClosedRange(first, last, step)
        }
        return am to newRanges
    }

    override fun slice(slices: List<Any?>): Tensor<T> {
        val (am, ranges, sh) = TensorImpl.computeSliceView(this, slices)
        val (newAxisMap, newRanges) = composeSliceTo(am, ranges)
        return SlicedView(this.t, newRanges, newAxisMap, sh)
    }

    override fun permute(p: Permutation): Tensor<T> {
        val am = p.inverse().getArray()
        for (i in am.indices) {
            am[i] = axisMap[am[i]]
        }
        return SlicedView(t, ranges, am, p.permute(shape))
    }

}

class MutableSliceView<T:Any>(
    tensor: MutableTensor<T>, ranges: List<IntProgression>,
    /**
         * maps the axis to t's axis. `axisMap.size = this.dim`
         */
        axisMap: IntArray,

    shape: IntArray,
) : SlicedView<T>(tensor, ranges, axisMap, shape), MutableTensor<T> {
    override val t: MutableTensor<T> = tensor

    override fun set(idx: Index, v: T) {
        checkIdx(idx)
        val pos = mapIdx(idx)
        t[pos] = v
    }

    override fun applyAll(f: (T) -> T): MutableTensor<T> {
        return mapTo(model, f)
    }


//    override fun <N> mapTo(newCalculator: MathCalculator<N>, mapper: Function<T, N>): MutableTensor<N> {
//        return ATensor.buildFromSequence(newCalculator, sh, elementSequence().map { mapper.apply(it) })
//    }

    override fun slice(slices: List<Any?>): MutableTensor<T> {
        val (am, ranges, sh) = TensorImpl.computeSliceView(this, slices)
        val (newAxisMap, newRanges) = composeSliceTo(am, ranges)
        return MutableSliceView(this.t, newRanges, newAxisMap, sh)
    }

    override fun newAxisAt(axis: Int): MutableTensor<T> {
        val (am, ranges, sh) = TensorImpl.newAxisSliceView(this, axis)
        val (newAxisMap, newRanges) = composeSliceTo(am, ranges)
        return MutableSliceView(this.t, newRanges, newAxisMap, sh)
    }

    override fun permute(p: Permutation): MutableTensor<T> {
        val ranges = this.ranges
        val am = p.inverse().getArray()
        for (i in am.indices) {
            am[i] = axisMap[am[i]]
        }
        val (newAxisMap, newRanges) = composeSliceTo(am, ranges)
        return MutableSliceView(this.t, newRanges, newAxisMap, sh)
    }


    /*
    Inherited methods:
     */


    override fun permute(vararg reorderedAxes: Int): MutableTensor<T> {
        return super<MutableTensor>.permute(*reorderedAxes)
    }

    override fun transpose(axis1: Int, axis2: Int): MutableTensor<T> {
        return super<MutableTensor>.transpose(axis1, axis2)
    }


    override fun setAll(v: T) {
        originalIndicesNoOrder.forEach { idx -> t[idx] = v }
    }

    override fun sumAll(): T {
        val mc = model as AddSemigroup
        return originalIndicesNoOrder.map { idx -> t[idx] }.reduce(mc::add) // no order here
    }

    override fun transform(f: (T) -> T) {
        originalIndicesNoOrder.forEach { idx -> t[idx] = f(t[idx]) }
    }

}

abstract class CombinedView<T:Any>(tensors: List<Tensor<T>>, shape: IntArray)
    : AbstractTensor<T>(tensors[0].model, shape) {
    open val ts: List<Tensor<T>> = tensors


    override fun sumAll(): T {
        val mc = model as AddSemigroup
        return ts.asSequence().map { it.sumAll() }.reduce(mc::add)
    }


    override val isZero: Boolean
        get() = ts.all { it.isZero }

    override fun all(predicate: (T) -> Boolean): Boolean {
        return ts.all { it.all(predicate) }
    }

    override fun any(predicate: (T) -> Boolean): Boolean {
        return ts.any { it.any(predicate) }
    }

}

open class ConcatView<T:Any>(val axis: Int, tensors: List<Tensor<T>>, shape: IntArray)
    : CombinedView<T>(tensors, shape) {

    protected val axisLevels = IntArray(tensors.size + 1)

    override val ts: List<Tensor<T>> = tensors

    init {
        axisLevels[0] = 0
        for (i in tensors.indices) {
            axisLevels[i + 1] = axisLevels[i] + tensors[i].lengthAt(axis)
        }
    }

    override fun getChecked(idx: Index): T {
        val k = ArraySup.binarySearchFloor(axisLevels, 0, axisLevels.size, idx[axis])
        val nIdx = idx.copyOf()
        nIdx[axis] -= axisLevels[k]
        return ts[k][nIdx]
    }

}

class MutableConcatView<T:Any>(axis: Int, tensors: List<MutableTensor<T>>, shape: IntArray)
    : ConcatView<T>(axis, tensors, shape), MutableTensor<T> {
    override val ts: List<MutableTensor<T>> = tensors
    override fun set(idx: Index, v: T) {
        checkIdx(idx)
        val k = ArraySup.binarySearchFloor(axisLevels, 0, axisLevels.size, idx[axis])
        val nIdx = idx.copyOf()
        nIdx[axis] -= axisLevels[k]
        ts[k][nIdx] = v
    }

    override fun setAll(v: T) {
        for (t in ts) {
            t.setAll(v)
        }
    }
}

open class StackView<T:Any>(val axis: Int, tensors: List<Tensor<T>>, shape: IntArray)
    : CombinedView<T>(tensors, shape) {

    override val ts: List<Tensor<T>> = tensors
    protected fun transIdx(idx: Index): Index {
        val nIdx = IntArray(dim - 1)
        idx.copyInto(nIdx, endIndex = axis)
        idx.copyInto(nIdx, axis, axis + 1)
        return nIdx
    }

    override fun getChecked(idx: Index): T {
        val k = idx[axis]
        val nIdx = transIdx(idx)
        return ts[k][nIdx]
    }

    override fun sumAll(): T {
        val mc = model as AddSemigroup
        return ts.asSequence().map { it.sumAll() }.reduce(mc::add)
    }

    override val isZero: Boolean
        get() = ts.all { it.isZero }

}

class MutableStackView<T:Any>(axis: Int, tensors: List<MutableTensor<T>>, shape: IntArray)
    : StackView<T>(axis, tensors, shape), MutableTensor<T> {
    override val ts: List<MutableTensor<T>> = tensors
    override fun set(idx: Index, v: T) {
        checkIdx(idx)
        val nIdx = transIdx(idx)
        val k = idx[axis]
        ts[k][nIdx] = v
    }

    override fun setAll(v: T) {
        for (t in ts) {
            t.setAll(v)
        }
    }
}


open class ReshapedView<T:Any>(tensor: Tensor<T>, shape: IntArray)
    : AbstractTensor<T>(tensor.model, shape) {

    open val t: Tensor<T> = tensor

    protected fun computeShift(shape: IntArray): IntArray {
        val dim = shape.size
        val shifts = IntArray(dim)
        var s = 1
        for (i in (dim - 1) downTo 0) {
            shifts[i] = s
            s *= shape[i]
        }
        return shifts
    }

    private val shifts: IntArray = computeShift(shape)
    private val shiftsT: IntArray = computeShift(tensor.shape)

    /**
     * Converts the index to absolute position in 1-d array.
     */
    protected fun toPos(idx: Index): Int {
        var pos = 0
        for (i in 0 until dim) {
            pos += idx[i] * shifts[i]
        }
        return pos
    }

    /**
     * Converts the absolute position in 1-d array to index in t.
     */
    protected fun toIdx(pos0: Int): Index {
        val idx = IntArray(dim)
        var pos = pos0
        for (i in 0 until dim) {
            val t = pos / shiftsT[i]
            pos -= t * shiftsT[i]
            idx[i] = t
        }
        return idx
    }

    override fun getChecked(idx: Index): T {
        val pos = toPos(idx)
        val tIdx = toIdx(pos)
        return t[tIdx]
    }

    override fun sumAll(): T {
        return t.sumAll()
    }

    override fun elementSequence(): Sequence<T> {
        return t.elementSequence()
    }

    override fun flattenToList(): List<T> {
        return t.flattenToList()
    }

    override fun reshape(vararg newShape: Int): Tensor<T> {
        return t.reshape(*newShape)
    }

    override fun ravel(): Tensor<T> {
        return if (dim == 1) {
            this
        } else {
            t.ravel()
        }
    }

    override fun all(predicate: (T) -> Boolean): Boolean {
        return t.all(predicate)
    }

    override fun any(predicate: (T) -> Boolean): Boolean {
        return t.any(predicate)
    }
}

class MutableReshapedView<T:Any>(tensor: MutableTensor<T>, shape: IntArray)
    : ReshapedView<T>(tensor, shape), MutableTensor<T> {
    override val t: MutableTensor<T> = tensor
    override fun set(idx: Index, v: T) {
        checkIdx(idx)
        val pos = toPos(idx)
        val tIdx = toIdx(pos)
        t[tIdx] = v
    }

    override fun setAll(v: T) {
        t.setAll(v)
    }

    override fun transform(f: (T) -> T) {
        t.transform(f)
    }

    override fun reshape(vararg newShape: Int): MutableTensor<T> {
        return t.reshape(*newShape)
    }

    override fun ravel(): MutableTensor<T> {
        return if (dim == 1) {
            this
        } else {
            t.ravel()
        }
    }
}

class BroadcastView<T:Any>(
    val t: Tensor<T>, shape: IntArray,
//                             val originAxes: IntArray,
    val d: Int,
    private val extendedAxes: IntArray,
//                             val newAxes: IntArray
) : AbstractTensor<T>(t.model, shape) {
    /*
     * originAxes[i]
     */

    override fun getChecked(idx: Index): T {
        val tIdx = idx.copyOfRange(d, idx.size)
        for (ax in extendedAxes) {
            tIdx[ax] = 0
        }
        return t[tIdx]
    }

    //    override fun getChecked(idx: Index): T {
//        val tIdx = IntArray(t.dim)
//        for (i in originAxes.indices) {
//            tIdx[i] = idx[originAxes[i]]
//        }
//        for (ax in extendedAxes) {
//            tIdx[ax] = 0
//        }
//        return t[tIdx]
//    }
//
    override fun sumAll(): T {
        val re = t.sumAll()
        var k = 1L
        for (ax in 0 until d) {
            k *= sh[ax]
        }
        val mc = model as AddSemigroup
        return mc.multiplyLong(re, k)
    }

}

open class IndexMapView<T:Any>(
    open val tensor: Tensor<T>,
    /**
         * Axis map.
         *
         *     tIdx[i] = idx[am[i]]
         */
        val am: IntArray,
    val offsets: IntArray,
    shape: IntArray) : AbstractTensor<T>(tensor.model, shape) {

    protected fun mapIdx(idx: Index): IntArray {
        val tIdx = offsets.clone()
        for (i in tIdx.indices) {
            tIdx[i] += idx[am[i]]
        }
        return tIdx
    }

    override fun getChecked(idx: Index): T {
        val tIdx = mapIdx(idx)
        return tensor[tIdx]
    }
}

class MutableIndexMapView<T:Any>(override val tensor: MutableTensor<T>, am: IntArray, offsets: IntArray, shape: IntArray) :
        IndexMapView<T>(tensor, am, offsets, shape), MutableTensor<T> {
    override fun set(idx: Index, v: T) {
        checkIdx(idx)
        val tIdx = mapIdx(idx)
        tensor[tIdx] = v
    }
}
