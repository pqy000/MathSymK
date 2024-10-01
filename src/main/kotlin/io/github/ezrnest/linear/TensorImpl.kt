package io.github.ezrnest.linear

import io.github.ezrnest.numberTheory.NTFunctions
import io.github.ezrnest.structure.*
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.MathUtils
import java.util.*
import kotlin.math.min


abstract class AbstractTensor<T>(
    override val shape: IntArray
) : Tensor<T> {

    //Created by lyc at 2021-03-31 20:39


    final override fun lengthAt(axis: Int): Int {
        require(axis in 0 until dim)
        return shape[axis]
    }


    override fun toString(): String {
        val sb = StringBuilder()
        val dim = dim

        sb.append("Tensor(").append(shape.joinToString()).append("): \n")
        val limits = IntArray(dim) { 1 }
        if (dim >= 1) limits[dim - 1] = 10
        if (dim >= 2) limits[dim - 2] = 10
        this.joinTo(sb, limits = limits)
        return sb.toString()
    }


    final override val dim: Int get() = shape.size


    override val size: Int
        get() = MathUtils.product(shape)


//    override val indices: Sequence<Index>
//        get() = IterUtils.prodIdxN(sh)

    /**
     * Checks whether `idx` is a valid index for this tensor, throws exception if necessary.
     */
    protected fun checkIdx(idx: Index) {
        require(idx.size == dim) {
            "Dim mismatch: required $dim, given ${idx.size}"
        }
        for (i in 0 until dim) {
            if (!(0 <= idx[i] && idx[i] < shape[i])) {
                throw IndexOutOfBoundsException(
                    "Tensor index out of bound at axis $i: " +
                            "Shape=${shape.contentToString()}, Index=${idx.contentToString()}"
                )
            }
        }
    }
}

abstract class AbstractMutableTensor<T>(shape: IntArray) : AbstractTensor<T>(shape),
    MutableTensor<T> {



    override fun permute(vararg reorderedAxes: Int): MutableTensor<T> {
        return super<MutableTensor>.permute(*reorderedAxes)
    }

    override fun transpose(axis1: Int, axis2: Int): MutableTensor<T> {
        return super<MutableTensor>.transpose(axis1, axis2)
    }
}


abstract class AbstractTensorFromArray<T>(shape: IntArray) : AbstractMutableTensor<T>(shape) {
    protected val shifts = IntArray(dim)

    init {
        var s = 1
        for (i in (dim - 1) downTo 0) {
            shifts[i] = s
            s *= shape[i]
        }
    }

    protected fun toPos(idx: Index): Int {
        var pos = 0
        for (i in 0 until dim) {
            pos += idx[i] * shifts[i]
        }
        return pos
    }
}

/**
 * An array-implementation of tensor.
 */
class ATensor<T>
internal constructor(shape: IntArray, val data: Array<Any?>) :
    AbstractTensorFromArray<T>(shape) {

    override val size: Int
        get() = data.size

    override fun get(idx: Index): T {
        checkIdx(idx)
        @Suppress("UNCHECKED_CAST")
        return data[toPos(idx)] as T
    }

    override fun elementSequence(): Sequence<T> {
        @Suppress("UNCHECKED_CAST")
        return data.asSequence() as Sequence<T>
    }

    override fun flattenToList(): List<T> {
        @Suppress("UNCHECKED_CAST")
        return data.asList() as List<T>
    }

    override fun copy(): ATensor<T> {
        return ATensor(shape, data.copyOf())
    }

    override fun <S> map(mapping: (T) -> S): ATensor<S> {
        return apply1(mapping)
    }


    override fun set(idx: Index, v: T) {
        checkIdx(idx)
        data[toPos(idx)] = v
    }

    override fun setAll(v: T) {
        Arrays.fill(data, v)
    }


    internal inline fun <S> apply1(f: (T) -> S): ATensor<S> {
        val ndata = Array<Any?>(size) {
            @Suppress("UNCHECKED_CAST")
            f(data[it] as T)
        }
        return ATensor(shape, ndata)
    }

    internal inline fun inPlaceApply1(f: (T) -> T): ATensor<T> {
        for (i in 0 until size) {
            @Suppress("UNCHECKED_CAST")
            data[i] = f(data[i] as T)
        }
        return this
    }

    override fun transform(f: (T) -> T) {
        inPlaceApply1(f)
    }

    internal inline fun <S> inPlaceApply2(y: Tensor<S>, f: (T, S) -> T) {
        if (y is ATensor) {
            val d1 = data
            val d2 = y.data
            for (i in 0 until size) {
                @Suppress("UNCHECKED_CAST")
                d1[i] = f(d1[i] as T, d2[i] as S)
            }
        } else {
            var pos = 0
            val data = this.data
            for (s in y.elementSequence()) {
                @Suppress("UNCHECKED_CAST")
                data[pos] = f(data[pos] as T, s)
                pos++
            }
        }
    }


    override fun reshape(vararg newShape: Int): MutableTensor<T> {
        val sh = newShape.copyOf()
        TensorImpl.prepareNewShape(this, sh)
        return ATensor(sh, data)
    }


    companion object {

        fun <T> buildFromSequence(shape: IntArray, sequence: Sequence<T>): ATensor<T> {
            return buildFromSeqMap(shape, sequence) { it }
        }

        internal inline fun <T, S> buildFromSeqMap(shape: IntArray, sequence: Sequence<T>, f: (T) -> S): ATensor<S> {
            val size = MathUtils.product(shape)
            val seqIt = sequence.iterator()
            val data = Array<Any?>(size) { f(seqIt.next()) }
            return ATensor(shape, data)
        }

        internal inline fun <T1, T2, S> buildFromSeqMap2(
            shape: IntArray, s1: Sequence<T1>, s2: Sequence<T2>, f: (T1, T2) -> S
        ): ATensor<S> {
            val size = MathUtils.product(shape)
            val it1 = s1.iterator()
            val it2 = s2.iterator()
            val data = Array<Any?>(size) { f(it1.next(), it2.next()) }
            return ATensor(shape, data)
        }


        internal inline fun <T1, T2, S> apply2(x: ATensor<T1>, y: ATensor<T2>, f: (T1, T2) -> S): ATensor<S> {
            val d1 = x.data
            val d2 = y.data
            val ndata = Array<Any?>(x.size) {
                @Suppress("UNCHECKED_CAST")
                f(d1[it] as T1, d2[it] as T2)
            }
            return ATensor(x.shape, ndata)
        }

        fun <T> copyOf(tensor: Tensor<T>): ATensor<T> {
            if (tensor is ATensor) {
                return tensor.copy()
            }
            return buildFromSequence(tensor.shape, tensor.elementSequence())
        }

        fun <T> constant(c: T, shape: IntArray): ATensor<T> {
            val size = MathUtils.product(shape)
            // create an array of the given type
            val data = Array<Any?>(size) { c }
            return ATensor(shape, data)
        }

        fun <T> zeros(shape: IntArray, mc: AddMonoid<T>): ATensor<T> {
            return constant(mc.zero, shape)
        }

        fun <T> wedge(x: ATensor<T>, y: ATensor<T>, mc: Ring<T>): ATensor<T> {
            val shape = x.shape + y.shape
            val size = x.size * y.size
            val data = arrayOfNulls<Any>(size)
            val dataX = x.data
            val dataY = y.data
            var pos = 0
            for (a in dataX) {
                for (b in dataY) {
                    @Suppress("UNCHECKED_CAST")
                    data[pos++] = mc.multiply(a as T, b as T)
                }
            }
            return ATensor(shape, data)
        }


        private fun <T> recurAdd(
            list: List<Any?>, shape: IntArray, level: Int, pos: Int, dest: Array<Any?>, clz: Class<T>
        ): Int {
            val size = shape[level]
            require(list.size == size) {
                "Required length at axis $level is $size, but ${list.size} is given!"
            }
            var p = pos
            if (level == shape.lastIndex) {
                for (e in list) {
                    dest[p++] = clz.cast(e)
                }
                return pos + size
            }
            for (e in list) {
                require(e is List<*>) {
                    "Nesting level mismatch!"
                }
                p = recurAdd(e, shape, level + 1, p, dest, clz)
            }
            return p
        }

        fun <T> fromNestingList(list: List<Any>, clazz: Class<T>): ATensor<T> {
            val sh = arrayListOf<Int>()
            var l = list
            while (true) {
                require(l.isNotEmpty())
                sh += l.size
                val e = l[0]
                if (clazz.isInstance(e)) {
                    break
                }
                require(e is List<*>) {
                    "Elements in the given list should be either list or object of required type. " +
                            "Given: $e"
                }
                @Suppress("UNCHECKED_CAST")
                l = e as List<Any>
            }
            val shape = sh.toIntArray()
            val size = MathUtils.product(shape)

            val data = arrayOfNulls<Any>(size)
            val pos = recurAdd(list, shape, 0, 0, data, clazz)
            assert(pos == size)
            return ATensor(shape, data)
        }
    }
}

//class DoubleTensor(shape: IntArray, val data: DoubleArray) : AbstractTensorFromArray<Double>(shape) {
//    init {
//        require(data.size == size) {
//            "Data size mismatch: required ${size}, but ${data.size} is given."
//        }
//    }
//
//    override fun get(idx: Index): Double {
//        checkIdx(idx)
//        return data[toPos(idx)]
//    }
//
//    override fun set(idx: Index, v: Double) {
//        checkIdx(idx)
//        data[toPos(idx)] = v
//    }
//
//    override fun setAll(v: Double) {
//        Arrays.fill(data, v)
//    }
//
//    override fun elementSequence(): Sequence<Double> {
//        return data.asSequence()
//    }
//
//    override fun flattenToList(): List<Double> {
//        return data.asList()
//    }
//
//    override fun copy(): DoubleTensor {
//        return DoubleTensor(shape, data.copyOf())
//    }
//
//    fun mapDouble(mapping: DoubleUnaryOperator): DoubleTensor {
//        return apply1 { mapping.applyAsDouble(it) }
////        val ndata = DoubleArray(size) {
////            mapping.applyAsDouble(data[it])
////        }
////        return DoubleTensor(shape, ndata)
//    }
//
//    override fun <S> map(mapping: (Double) -> S): MutableTensor<S> {
//        return ATensor.buildFromSequence(shape, elementSequence().map { mapping(it) })
//    }
//
//    override fun transform(f: (Double) -> Double) {
//        for (i in 0 until size) {
//            data[i] = f(data[i])
//        }
//    }
//
//    fun transformDouble(f: DoubleUnaryOperator) {
//        inPlaceApply1 { f.applyAsDouble(it) }
//    }
//
//    private inline fun apply1(f: (Double) -> Double): DoubleTensor {
//        val ndata = DoubleArray(size) {
//            f(data[it])
//        }
//        return DoubleTensor(shape, ndata)
//    }
//
//    private inline fun inPlaceApply1(f: (Double) -> Double) {
//        for (i in 0 until size) {
//            data[i] = f(data[i])
//        }
//    }
//
//    companion object{
//
//        private inline fun apply2(x: DoubleTensor, y: DoubleTensor, f: (Double, Double) -> Double): DoubleTensor {
//            val d1 = x.data
//            val d2 = y.data
//            val ndata = DoubleArray(x.size) {
//                f(d1[it], d2[it])
//            }
//            return DoubleTensor(x.shape, ndata)
//        }
//
//
//    }
//}

internal object TensorImpl {

    internal fun checkShape(x: Tensor<*>, y: Tensor<*>) {
        require(x.shape.contentEquals(y.shape)) {
            "Shape mismatch: ${x.shape.contentToString()} and ${y.shape.contentToString()}."
        }
    }

    fun addIfNegative(a: Int, m: Int): Int {
        return if (a < 0) {
            a + m
        } else {
            a
        }
    }


    private inline fun <T, S> apply1(x: Tensor<T>, f: (T) -> S): ATensor<S> {
        if (x is ATensor) {
            return x.apply1(f)
        }
        return ATensor.buildFromSeqMap(x.shape, x.elementSequence(), f)
    }

    private inline fun <T1, T2, S> apply2(x: Tensor<T1>, y: Tensor<T2>, f: (T1, T2) -> S): ATensor<S> {
        if (x is ATensor && y is ATensor && x.isSameShape(y)) {
            return ATensor.apply2(x, y, f)
        }
        val (x1, y1) = broadcast(x, y)
        return ATensor.buildFromSeqMap2(x1.shape, x1.elementSequence(), y1.elementSequence(), f)
    }

    fun <T1, T2, S> map2(x: Tensor<T1>, y: Tensor<T2>, f: (T1, T2) -> S): ATensor<S> {
        return apply2(x, y, f)
    }

    private inline fun <T> inPlaceApply1(x: MutableTensor<T>, f: (T) -> T) {
        if (x is ATensor) {
            x.inPlaceApply1(f)
        } else {
            for (idx in x.indices) {
                x[idx] = f(x[idx])
            }
        }
    }

    private inline fun <T> inPlaceApply2(x: MutableTensor<T>, y_: Tensor<T>, f: (T, T) -> T) {
        val y = broadcastTo(y_, x.shape) // broadcast y to x's shape
        if (x is ATensor && y is ATensor) {
            x.inPlaceApply2(y, f)
        } else {
            for (idx in x.indices) {
                x[idx] = f(x[idx], y[idx])
            }
        }
    }

    fun <T> add(x: Tensor<T>, y: Tensor<T>, mc: AddSemigroup<T>): ATensor<T> {
        return apply2(x, y, mc::add)
    }

    fun <T> addScalar(x: Tensor<T>, c: T, mc: AddSemigroup<T>): ATensor<T> {
        return apply1(x) { mc.add(it, c) }
    }

    fun <T> addScalar(c: T, x: Tensor<T>, mc: AddSemigroup<T>): ATensor<T> {
        return apply1(x) { mc.add(c, it) }
    }


    /**
     * Returns the negation of this tensor.
     *
     */
    fun <T> negate(x: Tensor<T>, mc: AddGroup<T>): ATensor<T> {
        return apply1(x) { mc.negate(it) }
    }

    /**
     * Returns the sum of this tensor and `y`.
     *
     * The sum of two tensor `x,y` has the
     * shape of `max(x.shape, y.shape)`, here `max` means element-wise maximum of two arrays.
     */
    fun <T> subtract(x0: Tensor<T>, y0: Tensor<T>, model: AddGroup<T>): ATensor<T> {
        return apply2(x0, y0) { a, b -> model.subtract(a, b) }
    }

    fun <T> subtractScalar(x: Tensor<T>, c: T, mc: AddGroup<T>): ATensor<T> {
        return apply1(x) { mc.subtract(it, c) }
    }

    /**
     * Returns the result of multiplying this tensor with a scalar.
     */
    fun <T> multiplyScalar(x: Tensor<T>, k: T, mc: MulSemigroup<T>): ATensor<T> {
        return apply1(x) { mc.multiply(it, k) }
    }

    fun <T> multiplyScalar(k: T, x: Tensor<T>, mc: MulSemigroup<T>): ATensor<T> {
        return apply1(x) { mc.multiply(k, it) }
    }

    fun <T> multiplyN(x: Tensor<T>, k: Long, mc: AddSemigroup<T>): ATensor<T> {
//        return ATensor.buildFromSequence(x.shape, x.indices.map { idx -> mc.multiplyN(x[idx], k) })
        return apply1(x) { mc.multiplyN(it, k) }
    }

    /**
     * Returns the result of dividing this tensor with a scalar.
     */
    fun <T> divide(x: Tensor<T>, k: T, mc: MulGroup<T>): ATensor<T> {
//        return ATensor.buildFromSequence(x.shape, x.indices.map { idx -> mc.divide(k, x[idx]) })
        return apply1(x) { mc.divide(it, k) }
    }

    fun <T> divideScalar(x: Tensor<T>, c: T, mc: MulGroup<T>): ATensor<T> {
        return apply1(x) { mc.divide(it, c) }
    }

    fun <T> divideScalarBy(c: T, x: Tensor<T>, mc: MulGroup<T>): ATensor<T> {
        return apply1(x) { mc.divide(c, it) }
    }


    /**
     * Returns the **element-wise** product of this tensor and `y`.
     *
     */
    fun <T> multiply(x0: Tensor<T>, y0: Tensor<T>, mc: MulSemigroup<T>): ATensor<T> {
        return apply2(x0, y0, mc::multiply)
    }

    fun <T> reciprocal(x: Tensor<T>, mc: Field<T>): ATensor<T> {
        return apply1(x) { mc.reciprocal(it) }
    }

    /**
     * Returns the **element-wise** division of this tensor and `y`.
     *
     * @throws ArithmeticException if zero-division happens
     */
    fun <T> divide(x0: Tensor<T>, y0: Tensor<T>, mc: MulGroup<T>): ATensor<T> {
        return apply2(x0, y0, mc::divide)
    }

    fun <T> inPlaceAdd(x: MutableTensor<T>, y: Tensor<T>, mc: AddSemigroup<T>) {
        inPlaceApply2(x, y, mc::add)
    }

    fun <T> inPlaceSubtract(x: MutableTensor<T>, y: Tensor<T>, mc: AddGroup<T>) {
        inPlaceApply2(x, y, mc::subtract)
    }

    fun <T> inPlaceAddScalar(x: MutableTensor<T>, c: T, mc: AddSemigroup<T>) {
        inPlaceApply1(x) { mc.add(it, c) }
    }

    fun <T> inPlaceSubtractScalar(x: MutableTensor<T>, c: T, mc: AddGroup<T>) {
        inPlaceApply1(x) { mc.subtract(it, c) }
    }


    fun <T> inPlaceMultiplyScalar(x: MutableTensor<T>, k: T, mc: MulSemigroup<T>) {
        inPlaceApply1(x) { mc.multiply(k, it) }
    }

    fun <T> inPlaceMultiplyN(x: MutableTensor<T>, k: Long, mc: AddSemigroup<T>) {
        inPlaceApply1(x) { mc.multiplyN(it, k) }
    }

    fun <T> inPlaceMultiply(x: MutableTensor<T>, y: Tensor<T>, mc: MulSemigroup<T>) {
        inPlaceApply2(x, y, mc::multiply)
    }

    fun <T> inPlaceDivideScalar(x: MutableTensor<T>, k: T, mc: MulGroup<T>) {
        inPlaceApply1(x) { mc.divide(it, k) }
    }

    fun <T> inPlaceDivide(x: MutableTensor<T>, y: Tensor<T>, mc: MulGroup<T>) {
        inPlaceApply2(x, y, mc::divide)
    }


    fun <T> inner(x: Tensor<T>, y: Tensor<T>, mc: Ring<T>): T {
        require(x.isSameShape(y)) {
            "Two tensor must have the same shape for inner!" +
                    "Given shapes: ${x.shape.contentToString()}, ${y.shape.contentToString()}."
        }
        return x.elementSequence().zip(y.elementSequence()).fold(mc.zero) { re, (a, b) ->
            mc.eval { re + a * b }
        }
    }

    fun <T> wedge(x: Tensor<T>, y: Tensor<T>, mc: Ring<T>): ATensor<T> {
        if (x is ATensor && y is ATensor) {
            return ATensor.wedge(x, y, mc)
        }
        val shape = x.shape + y.shape
        val result = ATensor.constant(mc.zero, shape)
        val data = result.data
        var pos = 0
        val seqX = x.elementSequence()
        val seqY = y.elementSequence()
        for (a in seqX) {
            for (b in seqY) {
                data[pos++] = mc.multiply(a, b)
            }
        }
        return result
    }

    fun <T> isLinearDependent(x: Tensor<T>, y: Tensor<T>, mc: Field<T>): Boolean {
        checkShape(x, y)
        var k: T? = null
        for ((a, b) in x.elementSequence().zip(y.elementSequence())) {
            if (k == null) {
                if (mc.isZero(a)) {
                    if (mc.isZero(b)) {
                        continue
                    }
                    return false
                }
                if (mc.isZero(b)) {
                    return false
                }
                k = mc.divide(a, b)
            } else {
                if (!mc.isEqual(a, mc.multiply(k, b))) {
                    return false
                }
            }
        }
        return true
    }


    fun <T> computeSliceView(x: Tensor<T>, slices: List<Any?>)
            : Triple<IntArray, List<IntProgression>, IntArray> {
        // remark: we need union type here
        var l = 0 // in this tensor
        val ns = arrayListOf<Int>()
        val ranges = arrayListOf<IntProgression>()
        val shape = x.shape
        val am = arrayListOf<Int>()
        for ((pos, t) in slices.withIndex()) {
            if (Tensor.NEW_AXIS != t && Tensor.DOTS != t) {
                require(l < x.dim) { "Too many indices!" }
            }
            when (t) {
                is Int -> {
                    val i = NTFunctions.mod(t, shape[l])
                    ranges.add(i..i)
                    l++
                }

                is IntProgression -> {
                    val r = IntProgression.fromClosedRange(
                        addIfNegative(t.first, shape[l]),
                        addIfNegative(t.last, shape[l]),
                        t.step
                    )
                    require(!r.isEmpty())
                    ranges.add(r)
                    ns.add((r.last - r.first) / r.step + 1)
                    am.add(l)
                    l++
                }

                null -> {
                    am.add(l)
                    ranges.add(0 until shape[l])
                    ns.add(shape[l])
                    l++
                }

                Tensor.NEW_AXIS -> {
                    ns.add(1)
                    am.add(-1)
                }

                Tensor.DOTS -> {
                    var rem = x.dim - ranges.size
                    for (j in (pos + 1) until slices.size) {
                        val t2 = slices[j]
                        require(Tensor.DOTS != t2) {
                            "Only one '...' is allowed in slice!"
                        }
                        if (t2 == null || t2 is Int || t2 is IntProgression) {
                            rem--
                        }
                    }
                    repeat(rem) {
                        am.add(l)
                        ranges.add(0 until shape[l])
                        ns.add(shape[l])
                        l++
                    }
                }

                else -> {
                    throw IllegalArgumentException("Not supported slice for $t")
                }
            }
        }
        while (l < x.dim) {
            am.add(l)
            ranges.add(0 until shape[l])
            ns.add(shape[l])
            l++
        }
//        if (ns.isEmpty()) {
//            // return a 1-d tensor instead
//            am.add(-1)
//            ns.add(1)
//        }
        return Triple(am.toIntArray(), ranges, ns.toIntArray())
    }

    fun newAxisSliceView(x: Tensor<*>, axis: Int):
            Triple<IntArray, List<IntProgression>, IntArray> {
        val dim = x.dim
        val ax = addIfNegative(axis, dim)
        require(ax in 0 until dim)
        val shape = x.shape
        val ns = IntArray(dim + 1)
        val ranges = shape.map { 0 until it }
        val am = IntArray(dim + 1)
        for (i in 0 until ax) {
            ns[i] = shape[i]
            am[i] = i
        }
        for (i in ax until dim) {
            ns[i + 1] = shape[i]
            am[i + 1] = i
        }
        ns[ax] = 1
        am[ax] = -1
        return Triple(am, ranges, ns)
    }

    fun <T> prepareSqueezeAll(t: Tensor<T>): Triple<IntArray, List<IntProgression>, IntArray> {
        val dim = t.dim
        val nsList = arrayListOf<Int>()
        val rangesList = ArrayList<IntProgression>(dim)
        val axisMapList = arrayListOf<Int>()
        for (i in 0 until dim) {
            if (t.shape[i] == 1) {
                rangesList.add(0 until 1)
                continue
            }
            nsList.add(t.shape[i])
            rangesList.add(0 until t.shape[i])
            axisMapList.add(i)
        }
        return Triple(axisMapList.toIntArray(), rangesList, nsList.toIntArray())
    }

    fun <T> prepareSqueeze(t: Tensor<T>, axis_: Int): Triple<IntArray, List<IntProgression>, IntArray> {
        val axis = addIfNegative(axis_, t.dim)
        require(axis in 0 until t.dim) {
            "Axis $axis_ out of bound."
        }
        require(t.shape[axis] == 1) {
            "Cannot squeeze axis $axis with length ${t.shape[axis]} != 1."
        }
        val shape = t.shape
        val ns = IntArray(shape.size - 1)
        val ranges = ArrayList<IntProgression>(shape.size)
        val am = IntArray(shape.size - 1)
        for (i in 0 until axis) {
            ns[i] = shape[i]
            ranges.add(0 until shape[i])
            am[i] = i
        }
        ranges.add(0 until 1)
        for (i in (axis + 1) until shape.size) {
            ns[i - 1] = shape[i]
            ranges.add(0 until shape[i])
            am[i - 1] = i
        }
        return Triple(am, ranges, ns)
    }

    fun <T> prepareConcat(ts: List<Tensor<T>>, axis: Int): Pair<Int, IntArray> {
        require(ts.isNotEmpty())
        val dim = ts[0].dim
        val resShape = ts[0].shape.copyOf()
        val ax = addIfNegative(axis, dim)
        require(ax in 0 until dim) {
            "Axis $axis out of bound."
        }
        resShape[axis] = 0
        for ((k, t) in ts.withIndex()) {
            require(t.dim == dim) {
                "Tensor dim mismatch for ${k + 1}-th tensor: required dim=$dim, but ${t.dim} is given."
            }
            for (l in resShape.indices) {
                if (l == ax) {
                    resShape[l] += t.lengthAt(l)
                } else {
                    require(resShape[l] == t.lengthAt(l)) {
                        "Tensor shape mismatch for ${k + 1}-th tensor at axis ${l}: " +
                                "required length=${resShape[l]}, but ${t.lengthAt(l)} is given."
                    }
                }
            }
        }
        return ax to resShape
    }


    fun <T> prepareStack(ts: List<Tensor<T>>, axis: Int): Pair<Int, IntArray> {
        require(ts.isNotEmpty())
        require(ts.all { it.isSameShape(ts[0]) }) {
            "Cannot stack tensors of shapes: ${ts.joinToString { it.shape.contentToString() }}"
        }
        val ax = addIfNegative(axis, ts[0].dim)
        val shape = ts[0].shape
        val ns = IntArray(shape.size + 1)
        shape.copyInto(ns, endIndex = ax)
        shape.copyInto(ns, ax + 1, ax)
        ns[ax] = ts.size
        return ax to ns
    }

    fun prepareNewShape(t: Tensor<*>, ns: IntArray) {
        val size = t.size
//        require(ns.isNotEmpty())
        var n1Idx = -1
        var s = 1
        for (i in ns.indices) {
            val len = ns[i]
            if (len == -1) {
                if (n1Idx != -1) {
                    throw IllegalArgumentException(
                        "Only one -1 is allowed in the shape array: ${ns.contentToString()}!"
                    )
                }
                n1Idx = i
            } else {
                require(len > 0) {
                    "Shape must be positive: ${ns.contentToString()}!"
                }
                s *= len
            }
        }
        if (n1Idx >= 0) {
            val len = size / s
            require(s * len == size) {
                "The given shape ${ns.contentToString()} does not fit the original shape ${t.shape.contentToString()}."
            }
            ns[n1Idx] = len
        } else {
            require(s == size) {
                "The given shape ${ns.contentToString()} does not fit the original shape ${t.shape.contentToString()}."
            }
        }
    }


    /**
     * Broadcasts the given tensor [t] to the given shape [ns].
     */
    fun <T> broadcastTo(t: Tensor<T>, ns: IntArray): Tensor<T> {
        if (t.shape.contentEquals(ns)) {
            return t
        }
        require(t.dim <= ns.size) {
            "Cannot broad cast ${t.shape.contentToString()} to ${ns.contentToString()}!"
        }
        val extendedAxes = arrayListOf<Int>()
        val shape = t.shape
        val d = ns.size - t.dim
        for (l in shape.lastIndex downTo 0) {
            if (shape[l] == ns[d + l]) {
                continue
            }
            if (shape[l] == 1) {
                extendedAxes.add(l)
                continue
            }
            throw IllegalArgumentException(
                "Cannot broadcast ${t.shape.contentToString()} to ${ns.contentToString()}, shape mismatch" +
                        "at axis ${l + d}."
            )
        }
        return BroadcastView(t, ns, d, extendedAxes.toIntArray())
    }


    private fun <T1, T2> broadcast0(t1: Tensor<T1>, t2: Tensor<T2>): Pair<Tensor<T1>, Tensor<T2>> {
        val newShape = IntArray(t2.dim)
        val diff = t2.dim - t1.dim
        val s1 = t1.shape
        val s2 = t2.shape
        val extended1 = arrayListOf<Int>()
        val extended2 = arrayListOf<Int>()
        for (l in s1.lastIndex downTo 0) {
            val dim1 = s1[l]
            val dim2 = s2[l + diff]
            if (dim1 == dim2) {
                newShape[l + diff] = dim1
                continue
            }
            if (dim1 == 1) {
                newShape[l + diff] = dim2
                extended1.add(l)
                continue
            }
            if (dim2 == 1) {
                newShape[l + diff] = dim1
                extended2.add(l + diff)
                continue
            }
            throw IllegalArgumentException(
                "Cannot broadcast ${s1.contentToString()} with ${s2.contentToString()}, " +
                        "shape mismatch at axis $l ."
            )
        }
        s2.copyInto(newShape, endIndex = diff)
        val r1 = BroadcastView(t1, newShape, diff, extended1.toIntArray())
        val r2 = if (extended2.isEmpty()) {
            t2
        } else {
            BroadcastView(t2, newShape, 0, extended2.toIntArray())
        }
        return r1 to r2
//        val newAxes2 = intArrayOf()

    }

    fun <T1, T2> broadcast(t1: Tensor<T1>, t2: Tensor<T2>): Pair<Tensor<T1>, Tensor<T2>> {
        if (t1.isSameShape(t2)) {
            return t1 to t2
        }
        return if (t1.dim <= t2.dim) {
            broadcast0(t1, t2)
        } else {
            broadcast0(t2, t1).let { it.second to it.first }
        }

    }


    /**
     * Returns the general einsum of several tensors, assume `R = resShape.size`,
     * `M = mulShape.size`, then the general formula is
     *
     *      result[i_1,...,i_R] = sum(j_1,...,j_M; prod(k; t_k[tIdx]))
     *      where tIdx[l] = i_{tToResList[k][l]} or j_{tToMulList[k][l]}
     *
     *
     * For example, let us be given tensors `t1, t2, t3` with shapes `s1=(2,3), s2=(3,4), s3=(3,4,5)`,
     * The following code provides `r.shape = (2,5)`:
     * ```
     *     val r = einsum(listOf(t1, t2, t3), resShape = intArrayOf(2, 5), mulShape = intArrayOf(3,4),
     *                    tToResList = listOf(listOf(0 to 0), listOf(), listOf(2 to 1)),
     *                    tToMulList = listOf(listOf(1 to 0), listOf(0 to 0, 1 to 1), listOf(0 to 0, 1 to 1))
     *                    )
     *     r.shape // (2,5)
     * ```
     *
     * @param resShape the shape of resulting tensors
     * @param mulShape the shape of multiplying axes
     * @param tToResList
     * @param tToMulList
     * corresponding axes and summed.
     */
    fun <T> einsum(
        ts: List<Tensor<T>>,
        resShape: IntArray, mulShape: IntArray,
        tToResList: List<List<Pair<Int, Int>>>, tToMulList: List<List<Pair<Int, Int>>>,
        mc: Ring<T>
    ): ATensor<T> {
        val n = ts.size
        val result = ATensor.constant(mc.zero, resShape)
        val data = result.data
        val tIdxList = Array(ts.size) { IntArray(ts[it].dim) }

        val mIndices = IterUtils.prodIdxNoCopy(mulShape)
        fun placeIdx(partIdx: Index, tToPartList: List<List<Pair<Int, Int>>>) {
            for (k in 0 until n) {
                val tToPart = tToPartList[k]
                val tIdx = tIdxList[k]
                for ((axisT, axisR) in tToPart) {
                    tIdx[axisT] = partIdx[axisR]
                }
            }
        }

        var pos = 0
        for (rIdx in result.indices) {
            placeIdx(rIdx, tToResList)
            //place the indices corresponds to res part
            var re = mc.zero
            for (mIdx in mIndices) {
                placeIdx(mIdx, tToMulList)
                //place the indices corresponds to mul part
                var mul: T = run {
                    val t = ts[0]
                    val tIdx = tIdxList[0]
                    t[tIdx]
                }
                for (k in 1 until n) {
                    val t = ts[k]
                    val tIdx = tIdxList[k]
                    mul = mc.eval { mul * t[tIdx] }
                }
                re = mc.eval { re + mul }
            }
            data[pos++] = re
        }
        return result
    }

    val CHAR_PATTERN = "\\w\\d*".toRegex()


    fun <T> einsum(ts: List<Tensor<T>>, expr: String, model: Ring<T>): MutableTensor<T> {
        require(ts.isNotEmpty())
        val i1 = expr.indexOf("->")
        val tAxes = if (i1 >= 0) {
            expr.substring(0, i1)
        } else {
            expr
        }.split(",").also {
            require(it.size == ts.size) {
                "Count mismatch: ${it.size} tensors are required but ${ts.size} is given. "

            }
        }.mapIndexed { i, s ->
            CHAR_PATTERN.findAll(s.trim()).map {
                it.value
            }.toList().also {
                require(it.size == ts[i].dim) {
                    "Dim mismatch for ${i + 1}-th tensor: " +
                            "${it.size} is required but given tensor dim = ${ts[i].dim}. " +
                            "Expr=[${expr}], tensor dims=${ts.joinToString { t -> t.dim.toString() }}"
                }
            }
        }
        val charCount = sortedMapOf<String, Int>().also {
            tAxes.asSequence().flatten().forEach { s -> it.merge(s, 1, Int::plus) }
        }
        val chars = charCount.keys
        val res: List<String> = if (i1 >= 0) {
            val s = expr.substring(i1 + 2)
            CHAR_PATTERN.findAll(s.trim()).map {
                it.value
            }.toList()
        } else {
            charCount.entries.filter { it.value == 1 }.map { it.key }
        }

        val mul = chars.toSortedSet().also { it.removeAll(res) }
        val chToResIdx = res.withIndex().associate { it.value to it.index }
        val chToMulIdx = mul.withIndex().associate { it.value to it.index }
        fun shapeFor(part: Collection<String>): IntArray {
            return if (part.isEmpty()) {
                intArrayOf(1)
            } else {
                IntArray(part.size)
            }
        }

        val resShape = shapeFor(res)
        val mulShape = shapeFor(mul)
        val n = ts.size
        val tToResList = ArrayList<List<Pair<Int, Int>>>(n)
        val tToMulList = ArrayList<List<Pair<Int, Int>>>(n)
        for (k in 0 until n) {
            val t = ts[k]
            val tShape = t.shape
            val axes = tAxes[k]
            val tToRes = arrayListOf<Pair<Int, Int>>()
            val tToMul = arrayListOf<Pair<Int, Int>>()
            for (l in 0 until t.dim) {
                val ch = axes[l]
                fun addIdxAndCheckShape(
                    chToPartIdx: Map<String, Int>,
                    tToPart: MutableList<Pair<Int, Int>>,
                    partShape: IntArray
                ) {
                    val idx = chToPartIdx[ch] ?: return
                    if (partShape[idx] == 0) {
                        partShape[idx] = tShape[l]
                    } else {
                        require(partShape[idx] == tShape[l]) {
                            "Shape mismatch for ${l + 1}-th tensor at axis $idx, " +
                                    "required length=${partShape[idx]} but ${tShape[idx]} is given. " +
                                    "Expr=[${expr}], shapes=${ts.joinToString { it.shape.contentToString() }}"

                        }
                    }
                    tToPart += (l to idx)
                }
                addIdxAndCheckShape(chToResIdx, tToRes, resShape)
                addIdxAndCheckShape(chToMulIdx, tToMul, mulShape)
            }
            tToResList += tToRes
            tToMulList += tToMul
        }
        //TODO optimize the order of mul
        return einsum(ts, resShape, mulShape, tToResList, tToMulList, model)
    }


    fun <T> sumInOneAxis(t: Tensor<T>, sumAxis: Int, mc: AddMonoid<T>): MutableTensor<T> {
        val axis = addIfNegative(sumAxis, t.dim)
        require(axis in 0 until t.dim)
        if (t.dim == 1) {
            return Tensor.scalar(sumAll(t, mc))
        }
        val tShape = t.shape
        val shape = IntArray(t.dim - 1)
        tShape.copyInto(shape, 0, 0, axis)
        tShape.copyInto(shape, axis, axis + 1)
        val result = ATensor.zeros(shape, mc)
        val data = result.data
        val tIdx = IntArray(t.dim)
        val axisLen = t.lengthAt(axis)
        var pos = 0
        for (idx in result.indices) {
            idx.copyInto(tIdx, 0, 0, axis)
            idx.copyInto(tIdx, axis + 1, axis)
            var re = mc.zero
            for (i in 0 until axisLen) {
                tIdx[axis] = i
                re = mc.eval { re + t[tIdx] }
            }
            data[pos++] = re
        }
        return result
    }

    /**
     * Returns the sum of [t] in given [sumAxes] and [remAxes], it is required that both axes are non-empty.
     */
    fun <T> sumInAxes(t: Tensor<T>, sumAxes: IntArray, remAxes: IntArray, model: AddMonoid<T>): MutableTensor<T> {
        val tShape = t.shape
        fun makeShapeArray(axes: IntArray): IntArray {
            val shape = IntArray(axes.size)
            for (i in axes.indices) {
                shape[i] = tShape[axes[i]]
            }
            return shape
        }

        val sumShape = makeShapeArray(sumAxes)
        val resShape = makeShapeArray(remAxes)
        val result = ATensor.zeros(resShape, model)
        val data = result.data
        val tIdx = IntArray(t.dim)

        fun placeIdx(idx: Index, am: IntArray) {
            for (i in am.indices) {
                tIdx[am[i]] = idx[i]
            }
        }

        var pos = 0
        val sumIndices = IterUtils.prodIdxNoCopy(sumShape)
        for (idx in result.indices) {
            placeIdx(idx, remAxes)
            var re = model.zero
            for (sumIdx in sumIndices) {
                placeIdx(sumIdx, sumAxes)
                re = model.eval { re + t[tIdx] }
            }
            data[pos++] = re
        }
        return result
    }

    fun <T> sumAll(t: Tensor<T>, model: AddMonoid<T>): T {
        return t.elementSequence().fold(model.zero, model::add)
    }

    fun <T> sum(t: Tensor<T>, sumAxesList: List<Int>, model: AddMonoid<T>): MutableTensor<T> {
        if (sumAxesList.isEmpty()) {
            return Tensor.scalar(sumAll(t, model))
        }
        if (sumAxesList.size == 1) {
            return sumInOneAxis(t, sumAxesList.first(), model)
        }
        val axesSet = sumAxesList.asSequence().map {
            val axis = addIfNegative(it, t.dim)
            require(axis in 0 until t.dim)
            axis
        }.toSet()
        if (axesSet.size == t.dim) {
            return Tensor.scalar(sumAll(t, model))
        }
        val sumAxes = axesSet.toMutableList()
        val remAxes = (0 until t.dim).filterNotTo(arrayListOf()) { it in axesSet }
        sumAxes.sortBy { axis -> t.lengthAt(axis) } // place axes of bigger length backwards
        return sumInAxes(t, sumAxes.toIntArray(), remAxes.toIntArray(), model)
    }


    /**
     * Returns the matrix multiplication of [x] and [y].
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
     */
    fun <T> matmul(x: Tensor<T>, y: Tensor<T>, r: Int, mc: Ring<T>): MutableTensor<T> {
        val shape1 = x.shape
        val shape2 = y.shape
        val dim1 = shape1.size
        val dim2 = shape2.size
        require(dim1 >= r && dim2 >= r)
        if (dim1 == r && dim2 == r) {
            return Tensor.scalar(inner(x, y, mc))
        }
        val mShape = shape1.sliceArray(dim1 - r until dim1)
        require(mShape.contentEquals(shape2.sliceArray(0 until r))) {
            "Shape mismatch in matmul of rank $r: ${x.shapeString} and ${y.shapeString}"
        }
        val rShape = shape1.sliceArray(0 until (dim1 - r)) + shape2.sliceArray(r until dim2)
        val result = ATensor.zeros(rShape, mc)
        val data = result.data
        val xIdx = IntArray(dim1)
        val yIdx = IntArray(dim2)
        val mIndices = IterUtils.prodIdxNoCopy(mShape)
        var pos = 0
        for (rIdx in result.indices) {
            rIdx.copyInto(xIdx, endIndex = dim1 - r)
            rIdx.copyInto(yIdx, destinationOffset = r, startIndex = dim1 - r)
            var re = mc.zero
            for (mIdx in mIndices) {
                mIdx.copyInto(xIdx, destinationOffset = dim1 - r)
                mIdx.copyInto(yIdx)
                re = mc.eval { re + x[xIdx] * y[yIdx] }
            }
            data[pos++] = re
        }
        return result

    }

    /**
     * Returns the tensor dot product of [x] and [y].
     *
     * To perform tensor dot product of rank `r` for two tensors `x,y`, first it is
     * required that the last `r` dimensions of `x` and `y` are the same.
     * The resulting tensor `z` has the shape of `x.shape[:-r] + y.shape[:-r]`.
     *
     * Denote `i, j, k` indices of length `x.dim-r, y.dim-r, r` respectively, the following
     * equation is satisfied:
     *
     *     z[i,j] = sum(k, x[i,k] * y[j,k])
     *
     *
     *
     */
    fun <T> tensorDot(x: Tensor<T>, y: Tensor<T>, r: Int, mc: Ring<T>): MutableTensor<T> {
        val shape1 = x.shape
        val shape2 = y.shape
        val dim1 = shape1.size
        val dim2 = shape2.size
        val rem1 = dim1 - r
        val rem2 = dim2 - r
        require(rem1 >= 0 && rem2 >= 0)
        if (dim1 == r && dim2 == r) {
            return Tensor.scalar(inner(x, y, mc))
        }
        val mShape = shape1.sliceArray(rem1 until dim1)
        require(mShape.contentEquals(shape2.sliceArray(rem2 until dim2)))
        val rShape = shape1.sliceArray(0 until rem1) + shape2.sliceArray(0 until rem2)
        val result = ATensor.zeros(rShape, mc)
        val data = result.data
        val xIdx = IntArray(dim1)
        val yIdx = IntArray(dim2)
        val mIndices = IterUtils.prodIdxNoCopy(mShape)
        var pos = 0
        for (rIdx in result.indices) {
            rIdx.copyInto(xIdx, endIndex = rem1)
            rIdx.copyInto(xIdx, endIndex = rem2)
            var re = mc.zero
            for (mIdx in mIndices) {
                mIdx.copyInto(xIdx, destinationOffset = rem1)
                mIdx.copyInto(yIdx, destinationOffset = rem2)
                re = mc.eval { re + x[xIdx] * y[yIdx] }
            }
            data[pos++] = re
        }
        return result
    }

    private fun prepareDiag(x: Tensor<*>, axis1: Int, axis2: Int, offset: Int): Triple<IntArray, IntArray, IntArray> {
        require(x.dim >= 2) {
            "The given tensor's dim must >= 2!"
        }
        val dim = x.dim
        val ax1 = addIfNegative(axis1, dim)
        val ax2 = addIfNegative(axis2, dim)
        require(ax1 in 0 until dim && ax2 in 0 until dim) {
            "Invalid axes: $axis1, $axis2 for tensor dim=$dim. "
        }
        require(ax1 != ax2) {
            "The two axes for diagonal must not be the same!"
        }
        val am = IntArray(dim)
        // xIdx[i] = idx[am[i]], x: i <-> view: am[i]
        val offsets = IntArray(dim)
        val shape = IntArray(dim - 1)
        val xShape = x.shape

        fun computeShape(ax1: Int, ax2: Int, offset: Int): Int {
            val s = min(xShape[ax1], xShape[ax2] - offset)
            require(s > 0) {
                "The resulting tensor is empty! " +
                        "Diagonal in axes $axis1,$axis2 with offset=$offset, " +
                        "tensor shape=${x.shape.contentToString()}."
            }
            return s
        }
        if (offset >= 0) {
            shape[dim - 2] = computeShape(ax1, ax2, offset)
            offsets[ax2] = offset
        } else {
            shape[dim - 2] = computeShape(ax2, ax1, -offset)
            offsets[ax1] = -offset
        }


        var pos = 0
        for (i in 0 until dim) {
            if (i == ax1 || i == ax2) {
                continue
            }
            am[i] = pos
            shape[pos] = xShape[i]
            pos++
        }
        am[ax1] = dim - 2
        am[ax2] = dim - 2

        return Triple(am, offsets, shape)

    }

    fun <T> diagonal(x: Tensor<T>, axis1: Int, axis2: Int, offset: Int): Tensor<T> {
        val (am, offsets, shape) = prepareDiag(x, axis1, axis2, offset)
        return IndexMapView(x, am, offsets, shape)
    }

    fun <T> diagonal(x: MutableTensor<T>, axis1: Int, axis2: Int, offset: Int): MutableTensor<T> {
        val (am, offsets, shape) = prepareDiag(x, axis1, axis2, offset)
        return MutableIndexMapView(x, am, offsets, shape)
    }
}

//
//fun main() {
//    val Z = integers()
//
//    val x = Tensor.zeros(Z,1,3,1)
//    println(x.squeeze())
//    println(x.squeeze(0))
//    println(x.squeeze())
//    println(Tensor.scalar(1).ravel().reshape())
////    with(Tensor.over(Z)) {
////        val t1 = zero
////        val t2 = Tensor(1, 2, 3) { 1 }
////        println(t1)
////        println(t2.slice(0,0,0))
//////        println(t1 + t2)
//////        one.sumAll()
//////        println(one.slice(Tensor.NEW_AXIS, Tensor.NEW_AXIS))
//////        println(one.reshape(1))
////    }
//}