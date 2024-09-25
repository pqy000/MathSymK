package io.github.ezrnest.linear

import io.github.ezrnest.AbstractMathObject
import io.github.ezrnest.linear.Tensor.Companion.checkShape
import io.github.ezrnest.numberTheory.NTFunctions
import io.github.ezrnest.structure.*
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.MathUtils
import java.util.*
import java.util.function.Function
import kotlin.math.min


abstract class AbstractTensor<T>(
    mc: EqualPredicate<T>,
    /**
     * The shape of the tensor, it should not be modified
     */
    protected val sh: IntArray
) : AbstractMathObject<T, EqualPredicate<T>>(mc), Tensor<T> {

    //Created by lyc at 2021-03-31 20:39


    override val shape: IntArray
        get() = sh.clone()

    override fun lengthAt(axis: Int): Int {
        require(axis in 0 until dim)
        return sh[axis]
    }

    override fun isSameShape(y: Tensor<*>): Boolean {
        if (y is AbstractTensor) {
            return sh.contentEquals(y.sh)
        }
        return sh.contentEquals(y.shape)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val dim = dim

        sb.append("Tensor(").append(sh.joinToString()).append("): \n")
        val limits = IntArray(dim) { 1 }
        limits[dim - 1] = 10
        if(dim >= 2) limits[dim - 2] = 10
        this.joinTo(sb,limits = limits)
        return sb.toString()
    }


    final override val dim: Int get() = sh.size


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
            if (!(0 <= idx[i] && idx[i] < sh[i])) {
                throw IndexOutOfBoundsException(
                    "Tensor index out of bound at axis $i: " +
                            "Shape=${sh.contentToString()}, Index=${idx.contentToString()}"
                )
            }
        }
    }

    /**
     * Gets the element in this tensor. The index is already checked valid.
     * The index should not be modified.
     */
    protected abstract fun getChecked(idx: Index): T


    /**
     * Gets an element in this tensor according to the index.
     *
     * @param idx the index, it is required that `0 <= idx < shape`
     */
    override operator fun get(idx: Index): T {
        checkIdx(idx)
        return getChecked(idx)
    }

    /*
    Math operations:
     */

    /**
     * Determines whether this tensor is all-zero.
     */
    override val isZero: Boolean
        get() {
            val mc = model as Ring<T>
            return elementSequence().all { mc.isZero(it) }
        }

    //    override fun permute(p: Permutation): Tensor<T> {
//        require(p.size() == dim)
//        val sh = this.shape
//        val ranges = shape.map { 0 until it }
//        return SlicedView(this, ranges, p.array, p.apply(sh))
//    }


    /*
    General methods for MathObject
     */

//    override fun <N> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Tensor<N> {
//        return ATensor.buildFromSequence(newCalculator, sh, elementSequence().map { mapper.apply(it) })
//    }
}

abstract class AbstractMutableTensor<T>(mc: EqualPredicate<T>, shape: IntArray) : AbstractTensor<T>(mc, shape),
    MutableTensor<T> {


    override fun plus(y: Tensor<T>): MutableTensor<T> {
        return super<MutableTensor>.plus(y)
    }

    override fun times(y: Tensor<T>): MutableTensor<T> {
        return super<MutableTensor>.times(y)
    }

    override fun unaryMinus(): MutableTensor<T> {
        return super<MutableTensor>.unaryMinus()
    }

    override fun minus(y: Tensor<T>): MutableTensor<T> {
        return super<MutableTensor>.minus(y)
    }

    override fun div(y: Tensor<T>): MutableTensor<T> {
        return super<MutableTensor>.div(y)
    }

    override fun permute(vararg reorderedAxes: Int): MutableTensor<T> {
        return super<MutableTensor>.permute(*reorderedAxes)
    }

    override fun transpose(axis1: Int, axis2: Int): MutableTensor<T> {
        return super<MutableTensor>.transpose(axis1, axis2)
    }
}


/**
 * An array-implementation of tensor.
 */
class ATensor<T>
internal constructor(mc: EqualPredicate<T>, shape: IntArray, val data: Array<Any?>) :
    AbstractMutableTensor<T>(mc, shape) {
    private val shifts: IntArray = IntArray(dim)

    init {
        var s = 1
        for (i in (dim - 1) downTo 0) {
            shifts[i] = s
            s *= shape[i]
        }
    }

    override val size: Int
        get() = data.size

    private fun toPos(idx: Index): Int {
        var pos = 0
        for (i in 0 until dim) {
            pos += idx[i] * shifts[i]
        }
        return pos
    }


    override fun getChecked(idx: Index): T {
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
        return ATensor(model, sh, data.clone())
    }

    override fun <S> map(mapping: (T) -> S): ATensor<S> {
        TODO()
    }

    override fun set(idx: Index, v: T) {
        checkIdx(idx)
        data[toPos(idx)] = v
    }

    override fun setAll(v: T) {
        Arrays.fill(data, v)
    }

    private inline fun inlineApplyAll(f: (T) -> T): ATensor<T> {
        for (i in 0 until size) {
            @Suppress("UNCHECKED_CAST")
            data[i] = f(data[i] as T)
        }
        return this
    }


    override fun transform(f: (T) -> T) {
        inlineApplyAll(f)
    }

    override val isZero: Boolean
        get() {
            val mc = model as Ring
            return data.all {
                @Suppress("UNCHECKED_CAST")
                mc.isZero(it as T)
            }
        }


    override fun plus(y: Tensor<T>): MutableTensor<T> {
        if (y is ATensor && isSameShape(y)) {
            val mc = model as AddSemigroup<T>
            return apply2(this, y, mc::add)
        }
        return super.plus(y)

    }

    override fun unaryMinus(): ATensor<T> {
        val mc = model as AddGroup<T>
        return copy().inlineApplyAll(mc::negate)
    }

    override fun minus(y: Tensor<T>): MutableTensor<T> {
        if (y is ATensor && isSameShape(y)) {
            val mc = model as AddGroup<T>
            return apply2(this, y, mc::subtract)
        }
        return super.plus(y)
    }

    override fun scalarMul(k: T): ATensor<T> {
        val mc = model as Ring<T>
        return map { t -> mc.multiply(k, t) }
    }

    override fun scalarDiv(k: T): ATensor<T> {
        val mc = model as Field<T>
        return map { t -> mc.divide(k, t) }
    }

    override fun times(y: Tensor<T>): MutableTensor<T> {
        if (y is ATensor && isSameShape(y)) {
            val mc = model as Ring<T>
            return apply2(this, y, mc::multiply)
        }
        return super.times(y)
    }

    override fun div(y: Tensor<T>): MutableTensor<T> {
        if (y is ATensor && isSameShape(y)) {
            val mc = model as Field
            return apply2(this, y, mc::divide)
        }
        return super.div(y)
    }

    private inline fun apply2InPlace(y: Tensor<T>, f: (T, T) -> T) {
        checkShape(this, y)
        if (y is ATensor) {
            val d1 = data
            val d2 = y.data
            for (i in 0 until size) {
                @Suppress("UNCHECKED_CAST")
                d1[i] = f(d1[i] as T, d2[i] as T)
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

    override fun plusAssign(y: Tensor<T>) {
        val y1 = y.broadcastTo(*sh)
        return apply2InPlace(y1, (model as AddSemigroup<T>)::add)
    }

    override fun minusAssign(y: Tensor<T>) {
        val y1 = y.broadcastTo(*sh)
        return apply2InPlace(y1, (model as AddGroup<T>)::subtract)
    }

    override fun timesAssign(y: Tensor<T>) {
        val y1 = y.broadcastTo(*sh)
        return apply2InPlace(y1, (model as Ring<T>)::multiply)
    }

    override fun divAssign(y: Tensor<T>) {
        val y1 = y.broadcastTo(*sh)
        return apply2InPlace(y1, (model as Field<T>)::divide)
    }


    override fun reshape(vararg newShape: Int): MutableTensor<T> {
        val sh = newShape.clone()
        TensorImpl.prepareNewShape(this, sh)
        return ATensor(model, sh, data)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S> mapTo(newModel: EqualPredicate<S>, mapping: Function<T, S>): ATensor<S> {
        val ndata = Array<Any?>(size) { i ->
            mapping.apply(data[i] as T)
        }
        return ATensor(newModel, sh, ndata)
    }



    companion object {

        fun <T> buildFromSequence(mc: EqualPredicate<T>, shape: IntArray, sequence: Sequence<T>): ATensor<T> {
            val size = MathUtils.product(shape)
            val seqIt = sequence.iterator()
            val data = Array<Any?>(size) { seqIt.next() }
            return ATensor(mc, shape, data)
        }

        private inline fun <T> apply2(x: ATensor<T>, y: ATensor<T>, f: (T, T) -> T): ATensor<T> {
            checkShape(x, y)
            val d1 = x.data
            val d2 = y.data
            val ndata = Array<Any?>(x.size) {
                @Suppress("UNCHECKED_CAST")
                f(d1[it] as T, d2[it] as T)
            }
            return ATensor(x.model, x.sh, ndata)
        }

        fun <T> copyOf(tensor: Tensor<T>): ATensor<T> {
            val shape = tensor.shape
            if (tensor is ATensor) {
                return tensor.copy()
            }
            return buildFromSequence(tensor.model, shape, tensor.elementSequence())
        }

        fun <T> constant(c: T, shape: IntArray, mc: EqualPredicate<T>): ATensor<T> {
            val size = MathUtils.product(shape)
            // create an array of the given type
            val data = Array<Any?>(size) { c }
            return ATensor(mc, shape, data)
        }

        fun <T> zeros(shape: IntArray, mc: Ring<T>): ATensor<T> {
            return constant(mc.zero, shape, mc)
        }

//        fun <T> fromMatrix(m: AbstractMatrix<T>): ATensor<T> {
//            var pos = 0
//            val r = m.row
//            val c = m.column
//            val data = arrayOfNulls<Any>(r * c)
//            for (i in 0 until r) {
//                for (j in 0 until c) {
//                    data[pos++] = m[i, j]
//                }
//            }
//            @Suppress("UNCHECKED_CAST")
//            return ATensor(m.calculator, intArrayOf(r, c), data as Array<T>)
//        }

        fun <T> wedge(x: ATensor<T>, y: ATensor<T>): ATensor<T> {
            val mc = x.model as Ring<T>
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
            return ATensor(mc, shape, data)
        }


        private fun <T> recurAdd(
            list: List<Any?>, shape: IntArray, level: Int, pos: Int,
            dest: Array<Any?>, clz: Class<T>
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
                p = recurAdd(e as List<Any?>, shape, level + 1, p, dest, clz)
            }
            return p
        }

        fun <T> fromNestingList(list: List<Any>, mc: EqualPredicate<T>, clazz: Class<T>): ATensor<T> {
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
            return ATensor(mc, shape, data)
        }
    }

}


internal object TensorImpl {

    fun addIfNegative(a: Int, m: Int): Int {
        return if (a < 0) {
            a + m
        } else {
            a
        }
    }


    fun <T> add(_x: Tensor<T>, _y: Tensor<T>, mc : AddSemigroup<T>): MutableTensor<T> {
        val (x, y) = broadcast(_x, _y)
        return ATensor.buildFromSequence(mc, x.shape, x.indices.map { idx -> mc.add(x[idx], y[idx]) })
    }

    /**
     * Returns the negation of this tensor.
     *
     */
    fun <T> negate(x: Tensor<T>, mc : AddGroup<T>): MutableTensor<T> {
        return ATensor.buildFromSequence(mc, x.shape, x.indices.map { idx -> mc.negate(x[idx]) })
    }

    /**
     * Returns the sum of this tensor and `y`.
     *
     * The sum of two tensor `x,y` has the
     * shape of `max(x.shape, y.shape)`, here `max` means element-wise maximum of two arrays.
     */
    fun <T> subtract(x0: Tensor<T>, y0: Tensor<T>, model : AddGroup<T>): MutableTensor<T> {
        val (x, y) = broadcast(x0, y0)
        return ATensor.buildFromSequence(model, x.shape, x.indices.map { idx -> model.subtract(x[idx], y[idx]) })
    }

    /**
     * Returns the result of multiplying this tensor with a scalar.
     */
    fun <T> multiply(x: Tensor<T>, k: T, mc : MulSemigroup<T>): MutableTensor<T> {
        return ATensor.buildFromSequence(mc, x.shape, x.indices.map { idx -> mc.multiply(k, x[idx]) })
    }

    fun <T> multiplyLong(x: Tensor<T>, k: Long, mc : AddSemigroup<T>): MutableTensor<T> {
        return ATensor.buildFromSequence(mc, x.shape, x.indices.map { idx -> mc.multiplyLong(x[idx], k) })
    }

    /**
     * Returns the result of dividing this tensor with a scalar.
     */
    fun <T> divide(x: Tensor<T>, k: T, mc : MulGroup<T>): MutableTensor<T> {
        return ATensor.buildFromSequence(mc, x.shape, x.indices.map { idx -> mc.divide(k, x[idx]) })
    }

    /**
     * Returns the **element-wise** product of this tensor and `y`.
     *
     */
    fun <T> multiply(x0: Tensor<T>, y0: Tensor<T>, mc : MulSemigroup<T>): MutableTensor<T> {
        val (x, y) = broadcast(x0, y0)
        return ATensor.buildFromSequence(mc, x.shape, x.indices.map { idx -> mc.multiply(x[idx], y[idx]) })
    }

    /**
     * Returns the **element-wise** division of this tensor and `y`.
     *
     * @throws ArithmeticException if zero-division happens
     */
    fun <T> divide(x0: Tensor<T>, y0: Tensor<T>, mc : MulGroup<T>): MutableTensor<T> {
        val (x, y) = broadcast(x0, y0)
        return ATensor.buildFromSequence(mc, x.shape, x.indices.map { idx -> mc.divide(x[idx], y[idx]) })
    }

    fun <T> inner(x: Tensor<T>, y: Tensor<T>, mc : Ring<T>): T {
        require(x.isSameShape(y)) {
            "Two tensor must have the same shape for inner!" +
                    "Given shapes: ${x.shape.contentToString()}, ${y.shape.contentToString()}."
        }
        return x.elementSequence().zip(y.elementSequence()).fold(mc.zero) { re, (a, b) ->
            mc.eval { re + a * b }
        }
    }

    fun <T> wedge(x: Tensor<T>, y: Tensor<T>, mc : Ring<T>): MutableTensor<T> {
        if (x is ATensor && y is ATensor) {
            return ATensor.wedge(x, y)
        }
        val shape = x.shape + y.shape
        val result = ATensor.constant(mc.zero, shape, mc)
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

    fun <T> isLinearDependent(x: Tensor<T>, y: Tensor<T>, mc : Field<T>): Boolean {
        checkShape(x, y)
//        val idx = IntArray(x.dim)
        if (x.isZero || y.isZero) {
            return true
        }
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
        if (ns.isEmpty()) {
            // return a 1-d tensor instead
            am.add(-1)
            ns.add(1)
        }
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

    /**
     * Returns the general einsum of several tensors, assume `R = resShape.size`,
     * `M = mulShape.size`, then the general formula is
     *
     *      result[i_1,...,i_R] = sum(j_1,...,j_M; prod(k; t_k[tIdx]))
     *      where tIdx[l] = i_{tToResList[k][l]} or j_{tToMulList[k][l]}
     *
     *
     * For example, let us be given tensors `t1, t2, t3` with shapes `s1=(2,3), s2=(3,4), s3=(3,4,5)`,
     * The following code provides
     *
     *     val r = einsum(listOf(t1, t2, t3), resShape = intArrayOf(2, 5), mulShape = intArrayOf(3,4),
     *                    tToResList = listOf(listOf(0 to 0), listOf(), listOf(2 to 1)),
     *                    tToMulList = listOf(listOf(1 to 0), listOf(0 to 0, 1 to 1), listOf(0 to 0, 1 to 1))
     *                    )
     *     r.shape // (2,5)
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
        mc: UnitRing<T>
    ): ATensor<T> {
        val n = ts.size
        val result = ATensor.constant(mc.zero, resShape, mc)
        val data = result.data
        val tIdxList = Array(ts.size) { IntArray(ts[it].dim) }

        val mIndices = IterUtils.prodIdxN(mulShape)
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
                var mul = mc.one
                for (k in 0 until n) {
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


    fun <T> einsum(ts: List<Tensor<T>>, expr: String): MutableTensor<T> {
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
        val mc = ts[0].model as UnitRing
        return einsum(ts, resShape, mulShape, tToResList, tToMulList, mc)
    }


    fun <T> sumInOneAxis(t: Tensor<T>, sumAxis: Int): MutableTensor<T> {
        val mc = t.model as Ring
        val axis = addIfNegative(sumAxis, t.dim)
        require(axis in 0 until t.dim)
        if (t.dim == 1) {
            return Tensor.scalar(t.sumAll(), t.model)
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
    fun <T> sumInAxes(t: Tensor<T>, sumAxes: IntArray, remAxes: IntArray): MutableTensor<T> {
        val mc = t.model as Ring
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
        val result = ATensor.zeros(resShape, mc)
        val data = result.data
        val tIdx = IntArray(t.dim)

        fun placeIdx(idx: Index, am: IntArray) {
            for (i in am.indices) {
                tIdx[am[i]] = idx[i]
            }
        }

        var pos = 0
        val sumIndices = IterUtils.prodIdxN(sumShape)
        for (idx in result.indices) {
            placeIdx(idx, remAxes)
            var re = mc.zero
            for (sumIdx in sumIndices) {
                placeIdx(sumIdx, sumAxes)
                re = mc.eval { re + t[tIdx] }
            }
            data[pos++] = re
        }
        return result
    }


    fun <T> sum(t: Tensor<T>, sumAxesList: List<Int>): MutableTensor<T> {
        if (sumAxesList.isEmpty()) {
            return Tensor.scalar(t.sumAll(), t.model)
        }
        if (sumAxesList.size == 1) {
            return sumInOneAxis(t, sumAxesList.first())
        }
        val axesSet = sumAxesList.asSequence().map {
            val axis = addIfNegative(it, t.dim)
            require(axis in 0 until t.dim)
            axis
        }.toSet()
        if (axesSet.size == t.dim) {
            return Tensor.scalar(t.sumAll(), t.model)
        }
        val sumAxes = axesSet.toMutableList()
        val remAxes = (0 until t.dim).filterNotTo(arrayListOf()) { it in axesSet }
        sumAxes.sortBy { axis -> t.lengthAt(axis) } // place axes of bigger length backwards
        return sumInAxes(t, sumAxes.toIntArray(), remAxes.toIntArray())
    }

    fun <T> prepareConcat(ts: List<Tensor<T>>, axis: Int): Pair<Int, IntArray> {
        require(ts.isNotEmpty())
        val dim = ts[0].dim
        val shape = ts[0].shape
        val ax = addIfNegative(axis, dim)
        require(ax in 0 until dim) {
            "Axis $axis out of bound."
        }
        shape[axis] = 0
        for ((k, t) in ts.withIndex()) {
            require(t.dim == dim) {
                "Tensor dim mismatch for ${k + 1}-th tensor: required dim=$dim, but ${t.dim} is given."
            }
            for (l in shape.indices) {
                if (l == ax) {
                    shape[l] += t.lengthAt(l)
                } else {
                    require(shape[l] == t.lengthAt(l)) {
                        "Tensor shape mismatch for ${k + 1}-th tensor at axis ${l}: " +
                                "required length=${shape[l]}, but ${t.lengthAt(l)} is given."
                    }
                }
            }
        }
        return ax to shape
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

    //    fun <T> reshape(x : T)
    fun prepareNewShape(t: Tensor<*>, ns: IntArray) {
        val size = t.size
        require(ns.isNotEmpty())
        var n1Idx = -1
        var s = 1
        for (i in ns.indices) {
            val len = ns[i]
            if (len == -1) {
                if (n1Idx != -1) {
                    throw IllegalArgumentException("Only one -1 is allowed in the shape array: ${ns.contentToString()}!")
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

    private fun <T> broadcast0(t1: Tensor<T>, t2: Tensor<T>): Pair<Tensor<T>, Tensor<T>> {
        val nDim = t2.dim
        val newShape = IntArray(nDim)
        val d2 = t2.dim - t1.dim
        val s1 = t1.shape
        val s2 = t2.shape
        val extended1 = arrayListOf<Int>()
        val extended2 = arrayListOf<Int>()
        for (l in s1.lastIndex downTo 0) {
            if (s1[l] == s2[l + d2]) {
                newShape[l + d2] = s1[l]
                continue
            }
            if (s1[l] == 1) {
                newShape[l + d2] = s2[l]
                extended1.add(l)
                continue
            }
            if (s2[l + d2] == 1) {
                newShape[l + d2] = s1[l]
                extended2.add(l + d2)
                continue
            }
            throw IllegalArgumentException(
                "Cannot broadcast ${s1.contentToString()} with ${s2.contentToString()}, " +
                        "shape mismatch at axis ${l} ."
            )
        }
        s2.copyInto(newShape, endIndex = d2)
        val r1 = BroadcastView(t1, newShape, d2, extended1.toIntArray())
        val r2 = if (extended2.isEmpty()) {
            t2
        } else {
            BroadcastView(t2, newShape, 0, extended2.toIntArray())
        }
        return r1 to r2
//        val newAxes2 = intArrayOf()

    }

    fun <T> broadcast(t1: Tensor<T>, t2: Tensor<T>): Pair<Tensor<T>, Tensor<T>> {
        if (t1.isSameShape(t2)) {
            return t1 to t2
        }
        return if (t1.dim <= t2.dim) {
            broadcast0(t1, t2)
        } else {
            broadcast0(t2, t1)
        }

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
    fun <T> matmul(x: Tensor<T>, y: Tensor<T>, r: Int): MutableTensor<T> {
        val shape1 = x.shape
        val shape2 = y.shape
        val dim1 = shape1.size
        val dim2 = shape2.size
        val mc = x.model as Ring
        require(dim1 >= r && dim2 >= r)
        if (dim1 == r && dim2 == r) {
            return Tensor.scalar(x.inner(y), mc)
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
        val mIndices = IterUtils.prodIdxN(mShape)
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
    fun <T> tensorDot(x: Tensor<T>, y: Tensor<T>, r: Int): MutableTensor<T> {
        val shape1 = x.shape
        val shape2 = y.shape
        val dim1 = shape1.size
        val dim2 = shape2.size
        val rem1 = dim1 - r
        val rem2 = dim2 - r
        require(rem1 >= 0 && rem2 >= 0)
        val mc = x.model as Ring
        if (dim1 == r && dim2 == r) {
            return Tensor.scalar(x.inner(y), mc)
        }
        val mShape = shape1.sliceArray(rem1 until dim1)
        require(mShape.contentEquals(shape2.sliceArray(rem2 until dim2)))
        val rShape = shape1.sliceArray(0 until rem1) + shape2.sliceArray(0 until rem2)
        val result = ATensor.zeros(rShape, mc)
        val data = result.data
        val xIdx = IntArray(dim1)
        val yIdx = IntArray(dim2)
        val mIndices = IterUtils.prodIdxN(mShape)
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
