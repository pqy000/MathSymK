package cn.mathsymk.linear

import cn.mathsymk.model.struct.GenMatrix
import cn.mathsymk.model.struct.GenVector
import cn.mathsymk.structure.*

data class AMatrix<T>(
    override val row: Int, override val column: Int,
    override val model: EqualPredicate<T>,
    val data: Array<Any?>
) : MutableMatrix<T> {

    init {
        require(row * column == data.size)
        require(data.isNotEmpty())
    }

    private fun checkIndex(i: Int, j: Int) {
        require(i in 0 until row && j in 0 until column)
    }

    private fun toPos(i: Int, j: Int): Int {
        return i * column + j
    }


    private fun getChecked(i: Int, j: Int): T {
        @Suppress("UNCHECKED_CAST")
        return data[toPos(i, j)] as T
    }

    override fun get(i: Int, j: Int): T {
        checkIndex(i, j)
        return getChecked(i, j)
    }

    override fun rowAt(rowIdx: Int): Vector<T> {
        val pos0 = toPos(rowIdx, 0)
        return AVector(data.copyOfRange(pos0, pos0 + column), model)
    }


    override fun copy(): AMatrix<T> {
        return AMatrix(row, column, model, data.clone())
    }

    override fun set(i: Int, j: Int, value: T) {
        checkIndex(i, j)
        data[toPos(i, j)] = value
    }

    override fun setRow(i: Int, row: Vector<T>) {
        require(row.size == column)
        require(i in 0..<this.row)
        if (row !is AVector) {
            super.setRow(i, row)
            return
        }
        row.data.copyInto(data, toPos(i, 0))
    }


    override fun negateInPlace() {
        val mc = model as AddGroup
        for (i in data.indices) {
            @Suppress("UNCHECKED_CAST")
            data[i] = mc.negate(data[i] as T)
        }
    }


    override fun swapRow(r1: Int, r2: Int, colStart: Int, colEnd: Int) {
        val s1 = toPos(r1, 0)
        val s2 = toPos(r2, 0)
        for (l in colStart until colEnd) {
            val t = data[s1 + l]
            data[s1 + l] = data[s2 + l]
            data[s2 + l] = t
        }
    }


    override fun swapCol(c1: Int, c2: Int, rowStart: Int, rowEnd: Int) {
        var l = toPos(rowStart, 0)
        for (r in rowStart until rowEnd) {
            val t = data[l + c1]
            data[l + c1] = data[l + c2]
            data[l + c2] = t
            l += row
        }
    }

    override fun multiplyRow(r: Int, k: T, colStart: Int, colEnd: Int) {
        val d = toPos(r, 0)
        val mc = model as MulSemigroup
        for (l in colStart until colEnd) {
            @Suppress("UNCHECKED_CAST")
            data[d + l] = mc.multiply(k, data[d + l] as T)
        }
    }

    override fun divideRow(r: Int, k: T, colStart: Int, colEnd: Int) {
        val d = toPos(r, 0)
        val mc = model as UnitRing
        for (l in colStart until colEnd) {
            @Suppress("UNCHECKED_CAST")
            data[d + l] = mc.exactDivide(data[d + l] as T, k)
        }
    }

    override fun multiplyCol(c: Int, k: T, rowStart: Int, rowEnd: Int) {
        val mc = model as MulSemigroup
        for (r in rowStart until rowEnd) {
            val pos = toPos(r, c)
            @Suppress("UNCHECKED_CAST")
            data[pos] = mc.multiply(k, data[pos] as T)
        }
    }

    override fun divideCol(c: Int, k: T, rowStart: Int, rowEnd: Int) {
        val mc = model as UnitRing
        for (r in rowStart until rowEnd) {
            val pos = toPos(r, c)
            @Suppress("UNCHECKED_CAST")
            data[pos] = mc.exactDivide(k, data[pos] as T)
        }
    }

    override fun negateRow(r: Int, colStart: Int, colEnd: Int) {
        val mc = model as AddGroup
        val d = toPos(r, 0)
        for (l in colStart until colEnd) {
            @Suppress("UNCHECKED_CAST")
            data[d + l] = mc.negate(data[d + l] as T)
        }
    }


    override fun negateCol(c: Int, rowStart: Int, rowEnd: Int) {
        val mc = model as AddGroup
        for (r in rowStart until rowEnd) {
            val pos = toPos(r, c)
            @Suppress("UNCHECKED_CAST")
            data[pos] = mc.negate(data[pos] as T)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun multiplyAddRow(r1: Int, r2: Int, k: T, colStart: Int, colEnd: Int) {
        val s1 = toPos(r1, 0)
        val s2 = toPos(r2, 0)
        val mc = model as Ring
        for (l in colStart until colEnd) {
            data[s2 + l] = mc.eval { (data[s2 + l] as T) + k * (data[s1 + l] as T) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun multiplyAddCol(c1: Int, c2: Int, k: T, rowStart: Int, rowEnd: Int) {
        val mc = model as Ring
        for (r in rowStart until rowEnd) {
            val l = toPos(r, 0)
            data[l + c2] = mc.eval { (data[l + c2] as T) + k * (data[l + c1] as T) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun transformRows(r1: Int, r2: Int, a11: T, a12: T, a21: T, a22: T, colStart: Int, colEnd: Int) {
        val s1 = toPos(r1, 0)
        val s2 = toPos(r2, 0)
        val model = model as Ring
        model.eval {
            for (l in colStart until colEnd) {
                val x = data[s1 + l] as T
                val y = data[s2 + l] as T
                data[s1 + l] = a11 * x + a12 * y
                data[s2 + l] = a21 * x + a22 * y
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AMatrix<*>

        return row == other.row && column == other.column && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    companion object {

        internal inline fun <T> ofFlatten(row: Int, col: Int, model: EqualPredicate<T>, init: (Int) -> T): AMatrix<T> {
            val data = Array<Any?>(row * col) { init(it) }
            return AMatrix(row, col, model, data)
        }

        internal inline operator fun <T> invoke(
            row: Int,
            col: Int,
            model: EqualPredicate<T>,
            init: (Int, Int) -> T
        ): AMatrix<T> {
            return of(row, col, model, init)
        }

        internal inline fun <T, N> apply2(
            x: AMatrix<T>, y: AMatrix<T>,
            model: EqualPredicate<N>, f: (T, T) -> N
        ): AMatrix<N> {
//            require(x.shapeMatches(y))
            val d1 = x.data
            val d2 = y.data
            return ofFlatten(x.row, x.column, model) { k ->
                @Suppress("UNCHECKED_CAST")
                f(d1[k] as T, d2[k] as T)
            }
        }

        internal inline fun <T, N> apply1(x: AMatrix<T>, model: EqualPredicate<N>, f: (T) -> N): AMatrix<N> {
            val data = x.data
            return ofFlatten(x.row, x.column, model) { k ->
                @Suppress("UNCHECKED_CAST")
                f(data[k] as T)
            }
        }

        fun <T> copyOf(x: GenMatrix<T>, mc: EqualPredicate<T>): AMatrix<T> {
            if (x is AMatrix) {
                return x.copy()
            }
            return of(x.row, x.column, mc) { i, j -> x[i, j] }
        }

        inline fun <T> of(row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T): AMatrix<T> {
            val data = Array<Any?>(row * column) { }
            for (i in 0..<row) {
                val pos = i * column
                for (j in 0..<column) {
                    data[pos + j] = init(i, j)
                }
            }
            return AMatrix(row, column, model, data)
        }


    }
}

open class TransposedMatrixView<T>(open val origin: Matrix<T>) : Matrix<T> {
    override val model: EqualPredicate<T>
        get() = origin.model
    override val row: Int
        get() = origin.column
    override val column: Int
        get() = origin.row

    override fun get(i: Int, j: Int): T {
        require(i in 0..<row && j in 0..<column)
        return origin[j, i]
    }

    override fun transpose(): Matrix<T> {
        return origin
    }

    override fun rowAt(rowIdx: Int): Vector<T> {
        require(rowIdx in 0..<row)
        return origin.colAt(rowIdx)
    }

    override fun colAt(colIdx: Int): Vector<T> {
        require(colIdx in 0..<column)
        return origin.rowAt(colIdx)
    }

    override fun rowVectors(): List<Vector<T>> {
        return origin.colVectors()
    }

    override fun colVectors(): List<Vector<T>> {
        return origin.rowVectors()
    }

    override val isZero: Boolean
        get() = origin.isZero

    override fun det(): T {
        return origin.det()
    }

    override fun rank(): Int {
        return origin.rank()
    }

    override fun trace(): T {
        return origin.trace()
    }

    override fun diag(): Vector<T> {
        return origin.diag()
    }

    override fun sumAll(): T {
        return origin.sumAll()
    }
}

//TODO Mutable view

object MatrixImpl {


    internal inline fun <T, N> apply2(
        x: GenMatrix<T>, y: GenMatrix<T>,
        model: EqualPredicate<N>, f: (T, T) -> N
    ): AMatrix<N> {
        require(x.shapeMatches(y))
        if (x is AMatrix && y is AMatrix) {
            return AMatrix.apply2(x, y, model, f) // flattened version
        }
        return AMatrix(x.row, x.column, model) { i, j -> f(x[i, j], y[i, j]) }
    }

    internal inline fun <T, N> apply1(x: GenMatrix<T>, model: EqualPredicate<N>, f: (T) -> N): AMatrix<N> {
        if (x is AMatrix) {
            return AMatrix.apply1(x, model, f)// flattened version
        }
        return AMatrix(x.row, x.column, model) { i, j -> f(x[i, j]) }
    }


    fun <T> hadamard(x: GenMatrix<T>, y: GenMatrix<T>, model: MulSemigroup<T>): AMatrix<T> {
        return apply2(x, y, model, model::multiply)
    }

    fun <T> add(x: GenMatrix<T>, y: GenMatrix<T>, model: AddSemigroup<T>): AMatrix<T> {
        return apply2(x, y, model, model::add)
    }

    fun <T> negate(x: Matrix<T>, model: AddGroup<T>): Matrix<T> {
        return apply1(x, model, model::negate)
    }

    fun <T> subtract(x: GenMatrix<T>, y: GenMatrix<T>, model: AddGroup<T>): AMatrix<T> {
        return apply2(x, y, model, model::subtract)
    }

    fun <T> matmul(x: GenMatrix<T>, y: GenMatrix<T>, model: Ring<T>): AMatrix<T> {
        require(x.column == y.row) {
            "Shape mismatch in matmul: (${x.row}, ${x.column}) * (${y.row}, ${y.column})"
        }
        return AMatrix.of(x.row, y.column, model) { i, j ->
            var sum = model.zero
            for (k in 0 until x.column) {
                sum = model.eval { sum + x[i, k] * y[k, j] }
            }
            sum
        }
    }

    /**
     * Matrix-vector multiplication: `Ay`, where `y` is a column vector.
     *
     */
    fun <T> matmul(A: GenMatrix<T>, y: GenVector<T>, model: Ring<T>): MutableVector<T> {
        require(A.column == y.size) {
            "Shape mismatch in matmul: (${A.row}, ${A.column}) * (${y.size})"
        }
        return Vector(A.row, model) { i ->
            var sum = model.zero
            for (k in 0 until A.column) {
                sum = model.eval { sum + A[i, k] * y[k] }
            }
            sum
        }
    }

    /**
     * Matrix-vector multiplication: `v.T A`, where `v` is a column vector.
     *
     * The result will be a vector
     */
    fun <T> matmul(v: GenVector<T>, A: GenMatrix<T>, model: Ring<T>): MutableVector<T> {
        require(v.size == A.row) {
            "Shape mismatch in matmul: (${v.size}) * (${A.row}, ${A.column})"
        }
        return Vector(A.column, model) { j ->
            var sum = model.zero
            for (k in 0..<A.row) {
                sum = model.eval { sum + v[k] * A[k, j] }
            }
            sum
        }
    }


    fun <T> multiply(x: GenMatrix<T>, k: T, model: MulSemigroup<T>): AMatrix<T> {
        return apply1(x, model) { model.multiply(k, it) }
    }

    fun <T> multiplyLong(x: GenMatrix<T>, k: Long, model: AddGroup<T>): AMatrix<T> {
        return apply1(x, model) { model.multiplyLong(it, k) }
    }

    fun <T> divide(x: GenMatrix<T>, k: T, model: MulGroup<T>): AMatrix<T> {
        return apply1(x, model) { model.divide(it, k) }
    }


    fun <T> transpose(x: Matrix<T>): TransposedMatrixView<T> {
        return TransposedMatrixView(x)
    }

    fun <T> inverse(m: Matrix<T>): Matrix<T> {
        require(m.isSquare())
        TODO()
//        val mc = m.model
//        if (mc is FieldCalculator) {
//            return inverseInField(m)
//        }
//        if (mc is EUDCalculator) {
//            return MatrixUtils.inverseInEUD(m)
//        }
//        return MatrixUtils.inverseInRing(m)
    }

}