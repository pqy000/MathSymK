package io.github.ezrnest.linear

import io.github.ezrnest.linear.MatrixExt.cofactor
import io.github.ezrnest.model.Polynomial
import io.github.ezrnest.structure.*
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.ModelPatterns
import kotlin.math.min

@ConsistentCopyVisibility
data class AMatrix<T> internal constructor(
    override val row: Int, override val column: Int,
    override val model: EqualPredicate<T>,
    val data: Array<Any?>,
) : MutableMatrix<T> {

    init {
        require(row * column == data.size)
        require(data.isNotEmpty())
    }

    override val size: Int
        get() = data.size

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

    override fun setAll(row: Int, col: Int, matrix: GenMatrix<T>) {
        if (matrix !is AMatrix) {
            super.setAll(row, col, matrix)
            return
        }
        val mData = matrix.data
        for (i in matrix.rowIndices) {
            val p = matrix.toPos(i, 0)
            val destPos = toPos(i + row, col)
            mData.copyInto(data, destPos, p, p + matrix.column)
        }
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

    private inline fun inPlaceApply1(f: (T) -> T) {
        for (i in data.indices) {
            @Suppress("UNCHECKED_CAST")
            data[i] = f(data[i] as T)
        }
    }


    override fun negateInPlace() {
        val mc = model as AddGroup
        inPlaceApply1 { mc.negate(it) }
    }

    override fun plusAssign(y: Matrix<T>) {
        return MatrixImpl.addInPlace(this, y, model as AddSemigroup<T>)
    }

    override fun timesAssign(k: T) {
        val model = model as MulSemigroup
        inPlaceApply1 { model.multiply(k, it) }
    }

    override fun divAssign(k: T) {
        val model = model as UnitRing
        inPlaceApply1 { model.exactDivide(it, k) }
    }


    override fun swapRow(r1: Int, r2: Int, colStart: Int, colEnd: Int) {
        require(r1 in 0 until row && r2 in 0 until row)
        val s1 = toPos(r1, 0)
        val s2 = toPos(r2, 0)
        for (l in colStart until colEnd) {
            val t = data[s1 + l]
            data[s1 + l] = data[s2 + l]
            data[s2 + l] = t
        }
    }


    override fun swapCol(c1: Int, c2: Int, rowStart: Int, rowEnd: Int) {
        require(c1 in 0 until column && c2 in 0 until column)
        var l = toPos(rowStart, 0)
        for (r in rowStart until rowEnd) {
            val t = data[l + c1]
            data[l + c1] = data[l + c2]
            data[l + c2] = t
            l += row
        }
    }

    override fun multiplyRow(r: Int, k: T, colStart: Int, colEnd: Int) {
        require(r in 0 until row)
        val d = toPos(r, 0)
        val mc = model as MulSemigroup
        for (l in colStart until colEnd) {
            @Suppress("UNCHECKED_CAST")
            data[d + l] = mc.multiply(k, data[d + l] as T)
        }
    }

    override fun divideRow(r: Int, k: T, colStart: Int, colEnd: Int) {
        require(r in 0 until row)
        val d = toPos(r, 0)
        val mc = model as UnitRing
        for (l in colStart until colEnd) {
            @Suppress("UNCHECKED_CAST")
            data[d + l] = mc.exactDivide(data[d + l] as T, k)
        }
    }

    override fun multiplyCol(c: Int, k: T, rowStart: Int, rowEnd: Int) {
        require(c in 0 until column)
        val mc = model as MulSemigroup
        for (r in rowStart until rowEnd) {
            val pos = toPos(r, c)
            @Suppress("UNCHECKED_CAST")
            data[pos] = mc.multiply(k, data[pos] as T)
        }
    }

    override fun divideCol(c: Int, k: T, rowStart: Int, rowEnd: Int) {
        require(c in 0 until column)
        val mc = model as UnitRing
        for (r in rowStart until rowEnd) {
            val pos = toPos(r, c)
            @Suppress("UNCHECKED_CAST")
            data[pos] = mc.exactDivide(k, data[pos] as T)
        }
    }

    override fun negateRow(r: Int, colStart: Int, colEnd: Int) {
        require(r in 0 until row)
        val mc = model as AddGroup
        val d = toPos(r, 0)
        for (l in colStart until colEnd) {
            @Suppress("UNCHECKED_CAST")
            data[d + l] = mc.negate(data[d + l] as T)
        }
    }


    override fun negateCol(c: Int, rowStart: Int, rowEnd: Int) {
        require(c in 0 until column)
        val mc = model as AddGroup
        for (r in rowStart until rowEnd) {
            val pos = toPos(r, c)
            @Suppress("UNCHECKED_CAST")
            data[pos] = mc.negate(data[pos] as T)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun multiplyAddRow(r1: Int, r2: Int, k: T, colStart: Int, colEnd: Int) {
        require(r1 in 0..<row && r2 in 0..<row)
        val s1 = toPos(r1, 0)
        val s2 = toPos(r2, 0)
        val mc = model as Ring
        for (l in colStart until colEnd) {
            data[s2 + l] = mc.eval { (data[s2 + l] as T) + k * (data[s1 + l] as T) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun multiplyAddCol(c1: Int, c2: Int, k: T, rowStart: Int, rowEnd: Int) {
        require(c1 in 0..<column && c2 in 0..<column)
        val mc = model as Ring
        for (r in rowStart..<rowEnd) {
            val l = toPos(r, 0)
            data[l + c2] = mc.eval { (data[l + c2] as T) + k * (data[l + c1] as T) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun transformRows(r1: Int, r2: Int, a11: T, a12: T, a21: T, a22: T, colStart: Int, colEnd: Int) {
        require(r1 in 0..<row && r2 in 0..<row)
        require(colStart in 0..colEnd && colEnd <= column)
        val s1 = toPos(r1, 0)
        val s2 = toPos(r2, 0)
        with(model as Ring) {
            for (l in colStart..<colEnd) {
                val x = data[s1 + l] as T
                val y = data[s2 + l] as T
                data[s1 + l] = a11 * x + a12 * y
                data[s2 + l] = a21 * x + a22 * y
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun transformCols(c1: Int, c2: Int, a11: T, a12: T, a21: T, a22: T, rowStart: Int, rowEnd: Int) {
        with(model as Ring) {
            for (r in rowStart until rowEnd) {
                val pos0 = toPos(r, 0)
                val pos1 = pos0 + c1
                val pos2 = pos0 + c2
                val x = data[pos1] as T
                val y = data[pos2] as T
                data[pos1] = a11 * x + a12 * y
                data[pos2] = a21 * x + a22 * y
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

    override fun toString(): String {
        return this.joinToString()
    }

    companion object {
        fun <T> isEqual(x: AMatrix<T>, y: AMatrix<T>, predicate: EqualPredicate<T>): Boolean {
            if (x.row != y.row || x.column != y.column) return false
            val size = x.size
            return (0 until size).all {
                @Suppress("UNCHECKED_CAST")
                predicate.isEqual(x.data[it] as T, y.data[it] as T)
            }
        }


        fun <T> isZero(x: AMatrix<T>, model: AddMonoid<T>): Boolean {
            @Suppress("UNCHECKED_CAST")
            return x.data.all { model.isZero(it as T) }
        }

        internal inline fun <T> ofFlatten(row: Int, col: Int, model: EqualPredicate<T>, init: (Int) -> T): AMatrix<T> {
            val data = Array<Any?>(row * col) { init(it) }
            return AMatrix(row, col, model, data)
        }


        internal inline operator fun <T> invoke(
            row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T,
        ): AMatrix<T> {
            val data = Array<Any?>(row * column) { }
            for (i in 0..<row) {
                val pos = i * column
                for (j in 0..<column) {
                    data[pos + j] = init(i, j)
                }
            }
            return AMatrix(row, column, model, data)
        }

        internal inline fun <T, N> apply2(
            x: AMatrix<T>, y: AMatrix<T>, model: EqualPredicate<N>, f: (T, T) -> N,
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

        fun <T> addInPlace(x: AMatrix<T>, y: AMatrix<T>, model: AddSemigroup<T>) {
            val d1 = x.data
            val d2 = y.data
            for (i in 0 until x.size) {
                @Suppress("UNCHECKED_CAST")
                d1[i] = model.add(d1[i] as T, d2[i] as T)
            }
        }

        fun <T> copyOf(x: GenMatrix<T>, mc: EqualPredicate<T>): AMatrix<T> {
            if (x is AMatrix) {
                return x.copy()
            }
            return AMatrix(x.row, x.column, mc) { i, j -> x[i, j] }
        }

        fun <T> of(row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T): AMatrix<T> {
            return AMatrix(row, column, model, init)
        }

        fun <T> of(row: Int, col: Int, model: EqualPredicate<T>, vararg data: T): AMatrix<T> {
            require(row * col == data.size)
            val dataCopy = Array<Any?>(data.size) { data[it] }
            return AMatrix(row, col, model, dataCopy)
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

open class SubMatrixView<T>(
    val origin: Matrix<T>,
    rowStart: Int, rowEnd: Int, colStart: Int, colEnd: Int,
) : Matrix<T> {
    init {
        require(0 <= rowStart && rowEnd <= origin.row && rowStart < rowEnd)
        require(0 <= colStart && colEnd <= origin.column && colStart < colEnd)
    }

    final override val row = rowEnd - rowStart
    final override val column = colEnd - colStart
    val dRow = rowStart
    val dCol = colStart

    override val model: EqualPredicate<T>
        get() = origin.model

    init {
        require(dRow + row <= origin.row && dCol + column <= origin.column)
    }

    override fun get(i: Int, j: Int): T {
        require(i in 0 until row && j in 0 until column)
        return origin[i + dRow, j + dCol]
    }

    override fun subMatrix(rowStart: Int, colStart: Int, rowEnd: Int, colEnd: Int): Matrix<T> {
        require(0 <= rowStart && rowEnd <= row && rowStart < rowEnd)
        require(0 <= colStart && colEnd <= column && colStart < colEnd)
        return SubMatrixView(origin, rowStart + dRow, rowEnd + dRow, colStart + dCol, colEnd + dCol)
    }
}


open class SlicedMatrixView<T>(
    val origin: Matrix<T>, val rowMap: IntArray, val colMap: IntArray,
) : Matrix<T> {
    init {
        require(rowMap.isNotEmpty() && colMap.isNotEmpty())
        require(rowMap.all { it in 0 until origin.row })
        require(colMap.all { it in 0 until origin.column })
    }

    override val row: Int
        get() = rowMap.size
    override val column: Int
        get() = colMap.size
    override val model: EqualPredicate<T>
        get() = origin.model

    override fun get(i: Int, j: Int): T {
        require(i in 0 until row && j in 0 until column)
        return origin[rowMap[i], colMap[j]]
    }

    override fun slice(rows: IntArray, cols: IntArray): Matrix<T> {
        val newRows = IntArray(rows.size) { this.rowMap[rows[it]] }
        val newCols = IntArray(cols.size) { this.colMap[cols[it]] }
        return SlicedMatrixView(origin, newRows, newCols)
    }
}

//TODO Mutable view

/**
 * Provides Matrix-related functionalities.
 *
 * Most of the methods in this object accept [GenMatrix]'s as inputs and a `model` should be provided to specify the operations.
 */
object MatrixImpl {

    fun <T> isEqual(x: GenMatrix<T>, y: GenMatrix<T>, predicate: EqualPredicate<T>): Boolean {
        if (x is AMatrix && y is AMatrix) {
            return AMatrix.isEqual(x, y, predicate)
        }
        return x.row == y.row && x.column == y.column && x.rowIndices.all { i ->
            x.colIndices.all { j ->
                predicate.isEqual(x[i, j], y[i, j])
            }
        }
    }

    fun <T> isZero(x: GenMatrix<T>, scalars: AddMonoid<T>): Boolean {
        if (x is AMatrix) {
            return AMatrix.isZero(x, scalars)
        }
        return x.elementSequence().all(scalars::isZero)
    }

    internal inline fun <T, N> apply2(
        x: GenMatrix<T>, y: GenMatrix<T>,
        model: EqualPredicate<N>, f: (T, T) -> N,
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

    fun <T> addInPlace(x: MutableMatrix<T>, y: GenMatrix<T>, model: AddSemigroup<T>) {
        if (x is AMatrix && y is AMatrix) {
            AMatrix.addInPlace(x, y, model)
        } else {
            for (i in 0 until x.row) {
                for (j in 0 until x.column) {
                    x[i, j] = model.add(x[i, j], y[i, j])
                }
            }
        }
    }

    fun <T> negate(x: GenMatrix<T>, model: AddGroup<T>): AMatrix<T> {
        return apply1(x, model, model::negate)
    }

    fun <T> subtract(x: GenMatrix<T>, y: GenMatrix<T>, model: AddGroup<T>): AMatrix<T> {
        return apply2(x, y, model, model::subtract)
    }

    fun <T> sum(mats: List<GenMatrix<T>>, model: AddSemigroup<T>): AMatrix<T> {
        require(mats.isNotEmpty())
        val row = mats.first().row
        val column = mats.first().column
        val result = AMatrix.copyOf(mats.first(), model)
        for (i in 1 until mats.size) {
            val m = mats[i]
            require(m.row == row && m.column == column)
            addInPlace(result, m, model)
        }
        return result
    }

    fun <T> matmul(x: GenMatrix<T>, y: GenMatrix<T>, model: Ring<T>): AMatrix<T> {
        require(x.column == y.row) {
            "Shape mismatch in matmul: (${x.row}, ${x.column}) * (${y.row}, ${y.column})"
        }
        return AMatrix(x.row, y.column, model) { i, j ->
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
    fun <T> matmul(A: GenMatrix<T>, y: GenVector<T>, model: Ring<T>): AVector<T> {
        require(A.column == y.size) {
            "Shape mismatch in matmul: (${A.row}, ${A.column}) * (${y.size})"
        }
        return AVector(A.row, model) { i ->
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
    fun <T> matmul(v: GenVector<T>, A: GenMatrix<T>, model: Ring<T>): AVector<T> {
        require(v.size == A.row) {
            "Shape mismatch in matmul: (${v.size}) * (${A.row}, ${A.column})"
        }
        return AVector(A.column, model) { j ->
            var sum = model.zero
            for (k in 0..<A.row) {
                sum = model.eval { sum + v[k] * A[k, j] }
            }
            sum
        }
    }

    fun <T> product(mats: List<Matrix<T>>, model: Ring<T>): Matrix<T> {
        require(mats.isNotEmpty())
        return ModelPatterns.reduceDP<Matrix<T>, IntArray>(0, mats.size,
            get = { i -> mats[i] },
            operation = { m1, m2 -> matmul(m1, m2, model) },
            toModel = { m -> intArrayOf(m.row, m.column) },
            modelOperation = { x, y -> intArrayOf(x[0], y[1]) },
            modelTimeCost = { x, y -> x[0] * y[0] * y[1] }
        )
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

    fun <T> zero(row: Int, column: Int, model: AddMonoid<T>): AMatrix<T> {
        val zero = model.zero
        return AMatrix.ofFlatten(row, column, model) { zero }
    }

    fun <T> identity(n: Int, model: UnitRing<T>): AMatrix<T> {
        return scalar(n, model, model.one)
    }

    fun <T> scalar(n: Int, model: AddMonoid<T>, k: T): AMatrix<T> {
        val A = zero(n, n, model)
        for (i in 0 until n) {
            A[i, i] = k
        }
        return A
    }

    fun <T> concatCol(a: GenMatrix<T>, b: GenMatrix<T>, model: EqualPredicate<T>): AMatrix<T> {
        require(a.row == b.row)
        return AMatrix(a.row, a.column + b.column, model) { i, j ->
            if (j < a.column) a[i, j] else b[i, j - a.column]
        }
    }

    fun <T> concatRow(a: GenMatrix<T>, b: GenMatrix<T>, model: EqualPredicate<T>): AMatrix<T> {
        require(a.column == b.column)
        return AMatrix(a.row + b.row, a.column, model) { i, j ->
            if (i < a.row) a[i, j] else b[i - a.row, j]
        }
    }

    fun <T> addDiagonal(m: MutableMatrix<T>, a: T, model: AddSemigroup<T>) {
        for (i in 0..<min(m.row, m.column)) {
            m[i, i] = model.add(m[i, i], a)
        }
    }


//    fun <T> minor(A: Matrix<T>, rows: IntArray, cols: IntArray): Matrix<T> {
//        val remRows = remainingIndices(A.row, rows)
//        val remCols = remainingIndices(A.column, cols)
//        return A.slice(remRows, remCols)
//    }


    private fun <T> colVectors(A: GenMatrix<T>, model: EqualPredicate<T>): List<Vector<T>> {
        return A.colIndices.map { j ->
            AVector(A.row, model) { i -> A[i, j] }
        }
    }

    /**
     * Returns the kronecker product `C = A ⊗ B` of `A` and `B`.
     * The result matrix `C` has the shape `(A.row * B.row, A.column * B.column)` and its elements are computed as:
     * ```
     * C[i1 * B.row + i2, j1 * B.column + j2] = A[i1, j1] * B[i2, j2]
     * ```
     * Alternatively, `C` can be expressed in the block matrix form:
     * ```
     * C = [A[i,j] * B], where i = 0 until A.row, j = 0 until A.column
     * ```
     *
     * We have the following properties:
     * - **Associativity** - `(A ⊗ B) ⊗ C = A ⊗ (B ⊗ C)`
     * - **Distributivity** - `A ⊗ (B + C) = A ⊗ B + A ⊗ C`
     * - Mixed with matrix multiplication: `(A ⊗ B)(C ⊗ D) = (AC) ⊗ (BD)`
     */
    fun <T> kronecker(A: GenMatrix<T>, B: GenMatrix<T>, model: Ring<T>): AMatrix<T> {
        val res = zero(A.row * B.row, A.column * B.column, model)
        for (i in 0 until A.row) {
            for (j in 0 until A.column) {
                for (i2 in 0 until B.row) {
                    for (j2 in 0 until B.column) {
                        res[i * B.row + i2, j * B.column + j2] = model.eval { A[i, j] * B[i2, j2] }
                    }
                }
            }
        }
        return res
    }

    /*
    Statistics
     */
    fun <T> trace(A: GenMatrix<T>, model: AddSemigroup<T>): T {
        require(A.isSquare)
        var res = A[0, 0]
        for (i in 1 until A.row) {
            res = model.add(res, A[i, i])
        }
        return res
    }

    fun <T> diag(A: GenMatrix<T>, model: EqualPredicate<T>): AVector<T> {
        require(A.isSquare)
        return AVector(A.row, model) { i -> A[i, i] }
    }

    fun <T> sumAll(A: GenMatrix<T>, model: AddSemigroup<T>): T {
        return A.elementSequence().reduce(model::add)
    }

    /*
    Inverse
     */

    fun <T> rank(A: GenMatrix<T>, model: Field<T>): Int {
        val pivots = toEchelon(AMatrix.copyOf(A, model), model)
        return pivots.size
    }

    fun <T> columnSpace(A: GenMatrix<T>, model: Field<T>): VectorSpace<T> {
        val mutable = AMatrix.copyOf(A, model)
        val pivots = toUpperTriangle(mutable, model)
        val indepVectors = pivots.map { mutable.colAt(it) }
        return VectorSpace.fromBasis(indepVectors, A.row, model)
    }


    fun <T> spanOf(vectors: List<GenVector<T>>, vecLength: Int, model: Field<T>): VectorSpace<T> {
        if (vectors.isEmpty()) return VectorSpace.zero(vecLength, model)
        val mutable = AMatrix(vecLength, vectors.size, model) { i, j -> vectors[j][i] }
        val pivots = toUpperTriangle(mutable, model)
        val indepVectors = pivots.map { mutable.colAt(it) }
        return VectorSpace.fromBasis(indepVectors)
    }

    /**
     * Computes the 'inverse' of the given matrix over a unit ring. This method simply compute the adjugate matrix and
     * divide it with the determinant (so it is time-consuming).
     *
     * This method can be used to compute the modular inverse of a matrix on `Z/Zn`, where n is not necessarily a prime.
     */
    fun <T> inverseInRing(M: GenMatrix<T>, model: UnitRing<T>): AMatrix<T> {
        val (p, adj) = charPolyAndAdj(M, model)
        val det = p.constantCoef
        if (!model.isUnit(det)) throw ArithmeticException("The determinant is not invertible")
        adj /= det
        return adj
    }

    fun <T> inverseInField(m: GenMatrix<T>, model: Field<T>): AMatrix<T> {
        val n = m.row
        val expanded = zero(n, 2 * n, model)
        expanded.setAll(0, 0, m)
        for (i in 0 until n) {
            expanded[i, i + n] = model.one
        }
        val pivots = toEchelon(expanded, model, column = n)
        if (pivots.size != n) {
            throw ArithmeticException("The matrix is not invertible")
        }
        return AMatrix.copyOf(expanded.subMatrix(0, n, n, 2 * n), model)
    }

//    /**
//     * Computes the inverse of the matrix on an Euclidean domain.
//     *
//     * It is required that the calculator of `M` is an instance of EUDCalculator.
//     */
//    fun <T> inverseInEUD(M: GenMatrix<T>, mc: EuclideanDomain<T>): Matrix<T> {
//        //TODO re-implement this method
//        M.requireSquare()
//        val n = M.column
//
//        val A = zero(n, 2 * n, mc)
//        A.setAll(0, 0, M)
//        for (i in 0 until n) {
//            A[i, i + n] = mc.one
//        }
////        Printer.printMatrix(A)
//        // to upper triangle
//        for (j in 0 until n) {
//            var i = j
//            while (mc.isZero(A[i, j]) && i < n) {
//                i++
//            }
//            if (i == n) throw ArithmeticException("The matrix is not invertible")
//            if (i != j) A.swapRow(i, j)
//
//            i++
//            outer@
//            while (true) {
//                val p = A[j, j]
//                while (i < n) {
//                    // gcd
//                    val (q, r) = mc.divideAndRemainder(A[i, j], p)
//                    A.multiplyAddRow(j, i, mc.negate(q), j)
////                    Printer.printMatrix(A)
//                    if (mc.isZero(r)) {
//                        i++
//                        continue
//                    }
//                    A.swapRow(j, i, j)
//                    continue@outer
//                }
//                if (!mc.isUnit(p)) {
//                    throw ArithmeticException("The matrix is not invertible")
//                }
//                A[j, j] = mc.one
//                A.divideRow(j, p, j + 1)
//                break
//            }
//
//        }
//
//        for (j1 in (n - 1) downTo 1) {
//            for (j2 in 0 until j1) {
//                val k = mc.negate(A[j2, j1])
//                A.multiplyAddRow(j1, j2, k, j1)
//            }
//        }
//        return A.subMatrix(0, n, A.row, A.column)
//    }


    fun <T> inverse(m: GenMatrix<T>, model: UnitRing<T>): AMatrix<T> {
        require(m.isSquare)
        return when (model) {
            is Field -> inverseInField(m, model)
//            else -> throw UnsupportedOperationException("The inverse of a matrix is not supported for the given model: $model")
            else -> inverseInRing(m, model)
        }
    }

    fun <T> isInvertible(m: GenMatrix<T>, model: UnitRing<T>): Boolean {
        require(m.isSquare)
        return model.isUnit(det(m, model))
    }

    fun <T> detSmall(m: GenMatrix<T>, model: Ring<T>): T {
        require(m.isSquare)
        return when (m.row) {
            1 -> m[0, 0]
            2 -> model.eval {
                m[0, 0] * m[1, 1] - m[0, 1] * m[1, 0]
            }

            3 -> model.eval {
                m[0, 0] * m[1, 1] * m[2, 2] + m[0, 1] * m[1, 2] * m[2, 0] + m[0, 2] * m[1, 0] * m[2, 1] -
                        m[0, 0] * m[1, 2] * m[2, 1] - m[0, 1] * m[1, 0] * m[2, 2] - m[0, 2] * m[1, 1] * m[2, 0]
            }

            else -> throw IllegalArgumentException("The matrix is too large")
        }
    }

    /**
     * A very time-consuming method to compute the determinant of a matrix by the definition:
     *
     *     det(A) = \sum_{σ ∈ S_n} (-1)^σ \prod_{i=1}^n A_{i, σ(i)}
     *
     * This method is provided to test the correctness of other determinant algorithms.
     */
    fun <T> detDefinition(m: GenMatrix<T>, model: Ring<T>): T {
        require(m.isSquare)
        var result = model.zero
        val n = m.row
        for ((idx, rev) in IterUtils.permRev(n, copy = false)) {
            var t = m[0, idx[0]]
            for (i in 1 until n) {
                t = model.eval { t * m[i, idx[i]] }
            }
            result = if (rev % 2 == 0) {
                model.add(result, t)
            } else {
                model.subtract(result, t)
            }
        }
        return result
    }

    /**
     * Computes the determinant of the given matrix using the Gauss-Bareiss algorithm.
     *
     * Complexity: `O(n^3)`
     */
    fun <T> detGaussBareiss(matrix: GenMatrix<T>, model: UnitRing<T>): T {
        // Created by lyc at 2020-03-05 19:18
        // Reimplemented by lyc at 2024-09-06
        /*
        Refer to 'A Course in Computational Algebraic Number Theory' Algorithm 2.2.6

        Explanation of the algorithm:
        We still use the primary transformation to eliminate elements in the matrix, but here we store the potential
        denominator and divide them only when necessary.

        Recall the vanilla elimination process, assuming the element at (k,k) is non-zero, we multiply a factor to
        the first row and subtract it from i-th row. The factor is equal to m[i,k] / m[k,k]. This row transformation
        will affect the i-th row, changing it element m[i,j] to m[i,j] - m[k,j] * m[i,k] / m[k,k].
        However, since the division m[i,k] / m[k,k] may not be exact, so we multiply m[k,k] to all rows i > k,
        which results in (m[k,k])^{n-k-1} in the denominator that we keep in mind.
        Then, the resulting element is just
            m[i,j]' = m[i,j] * m[k,k] - m[k,j] * m[i,k] = det([m[k,k], m[k,j]; m[i,k], m[i,j]])
        Indeed, we can keep all the denominators in mind and divide them at the end of the process, but we can improve it.
        In fact, for the (k+1)-th loop, we will encounter also the term det(X) = det([m[k+1,k+1]', m[k+1,j]'; m[i,k+1]', m[i,j]']).
        Now, by the update in the last loop, we have X = m[k,k] X0 - X1, where
        X0 = [ m[k+1,k+1], m[k+1,j]; m[i,k+1], m[i,j] ],
        X1 = [m[k,k+1] * m[k+1,k], m[k,j] * m[k+1,k];
              m[k,k+1] * m[i,  k], m[k,j] * m[i,  k]].
        Now, if det(B) = 0, det(rA + B) can be divided by r (using the definition of determinant).
        Also, it is easy to see that det(X1) = 0, and thus det(X) can be divided by m[k,k].
        Hence, for the (k+1)-th loop, for each row j > k+1, we can divide the resulting element by m[k,k],
        cancelling out (m[k,k])^{n-k-2} and leaving on one m[k,k] in the final det.

        Finally, we should divide the final det by m[k,k] for all k < n-1.
        Since det is the product of diagonal elements, we can just leave those m[k,k] and return m[n-1,n-1].
         */
        val mat = AMatrix.copyOf(matrix, model)
        val n: Int = mat.row
        var d = model.one // the denominator that we store
        var positive = true
        for (k in 0..<n) {
            //locate the top-left element used for elimination first, it must be non-zero
            if (model.isZero(mat[k, k])) {
                val r = (k + 1..<n).firstOrNull { i -> !model.isZero(mat[i, k]) } ?: return model.zero
                mat.swapRow(r, k, k) // swap i-th row and k-th row, changing the sign of the determinant
                positive = !positive
            }
            val p: T = mat[k, k]
            for (i in k + 1..<n) {
                val head = mat[i, k]
                for (j in k + 1..<n) { // j<=k are all zero, just ignore them
                    mat[i, j] = model.eval {
                        exactDivide(p * mat[i, j] - head * mat[k, j], d)
                    }
                }
            }
            d = p
        }
        val det = mat[n - 1, n - 1]
        return if (positive) det else model.negate(det)
    }

    fun <T> det(mat: GenMatrix<T>, mc: Ring<T>): T {
        require(mat.isSquare)
        if (mat.row <= 3) return detSmall(mat, mc)
        if (mc is UnitRing) {
            return detGaussBareiss(mat, mc)
        }
        return detDefinition(mat, mc)
    }


    /**
     * Gets the characteristic polynomial of the given matrix, which is defined as `det(xI - A)`,
     * and the adjugate matrix `A^*`.
     */
    fun <T> charPolyAndAdj(matrix: GenMatrix<T>, mc: UnitRing<T>): Pair<Polynomial<T>, AMatrix<T>> {
        /* Written by Ezrnest at 2024/9/12
        f(x) = det(xI - A) = \sum a_r x^r = \prod (x - λ_i), a_n = 1, a_{n-1} = -Tr(A),..., a_0 = (-1)^n det(A)
        Let us denote by α a multi-index with distinct elements and λ^α = λ_{α_1} λ_{α_2} ... λ_{α_k}.
        Then, a_{n-k} = (-1)^k \sum_{|α| = k} λ^α.
        We recall the fact: Tr(A) = \sum λ_i,  Tr(g(A)) = \sum g(λ_i).
        We aim to obtain matrices A_k = g_k(A) with
            λ_i(A_k) = (-1)^k \sum_{|α| = k, i ∈ α} λ^α, => Tr(A_k) = k a_{n-k} (since |α| = k and each 'i' appears k times)
        Let us deduce the recursive formula for A_k: A_1 = -A
        λ_i(A_{k+1}) = (-1)^{k+1} \sum_{|α| = k+1, i ∈ α} λ^α = (-1)^{k+1} \sum_{|α| = k, i ∉ α} λ^α λ_i
                     = (-1)^{k+1} (\sum_{|α| = k} λ^α - \sum_{|α| = k, i ∈ α} λ^α) λ_i
                     = (- a_{n-k} + λ_i(A_k)) λ_i
        Therefore, we have the recursive formula:
            A_{k+1} = (A_k - a_{n-k} I) A, a_{n-k} = Tr(A_k) / k
            g_{k+1}(x) = (-a_{n-k} + g_k(x)) x
        Now, the recursive formula of g_k actually shows that g_{k+1} = -(a_{n-k} + a_{n-k+1} x + ... + a_n x^{n-k}) x
        Remark: The recurrence relation is also related to the Newton's identities.

        Now, for the adjugate A^*, we have A^* A = det(A) I, while f(A) = a_0 + a_1 A + ... + a_n A^n = 0, a_0 = (-1)^n det(A)
        so A (a_1 + a_2 A + ... + a_n A^{n-1}) = (-1)^{n+1} det(A) I, namely
        A^* = (-1)^{n+1} (a_1 + a_2 A + ... + a_n A^{n-1}) = (-1)^n (-a_1 + g_{n-1}(x))
         */
        require(matrix.isSquare)
        val n = matrix.row
        if (n == 1) return Polynomial.linear(mc, mc.one, mc.negate(matrix[0, 0])) to identity(1, mc)
        val A = matrix
        val aList = ArrayList<T>(n + 1) // a list of coefficients in reverse order
        aList += mc.one // a_n = 1

        var k = 1
        var Ak = negate(A, mc)
        var Bk: AMatrix<T> = Ak // B_{k+1} = A_k - a_{n-k} I, the initial value is discarded
        while (true) {
            val tr = trace(Ak, mc)
            val ak = mc.exactDivide(tr, k.toLong())
            aList += ak
            if (k == n) break
            Bk = Ak
            addDiagonal(Bk, mc.negate(ak), mc)
            Ak = matmul(Bk, A, mc)
            k++
        }
        val f = Polynomial.fromList(mc, aList.asReversed())
        val adj = if (n % 2 == 0) Bk else negate(Bk, mc)
        return f to adj
    }


    /**
     * Computes the adjugate matrix of the given matrix by the definition:
     *
     *     adj(A) = C^T, where C_{i,j} = (-1)^(i+j) det(A_{i,j})
     */
    fun <T> adjugateDefinition(matrix: GenMatrix<T>, mc: Ring<T>): AMatrix<T> {
        require(matrix.isSquare)
        val n = matrix.row
        val A = AMatrix.copyOf(matrix, mc)
        val adj = AMatrix(n, n, mc) { i, j -> A.cofactor(i, j) }
        return adj
    }

    fun <T> adjugate(matrix: GenMatrix<T>, mc: Ring<T>): AMatrix<T> {
        if (mc is UnitRing) {
            return charPolyAndAdj(matrix, mc).second
        }
        return adjugateDefinition(matrix, mc)
    }

    fun <T> charPolyDefinition(matrix: GenMatrix<T>, mc: UnitRing<T>): Polynomial<T> {
        require(matrix.isSquare)
        val polyRing = Polynomial.over(mc)
        val n = matrix.row
        // det(xI - A)
        val matPoly = AMatrix(n, n, polyRing) { i, j ->
            polyRing.constant(mc.negate(matrix[i, j]))
        }
        addDiagonal(matPoly, polyRing.x, polyRing)
        return detGaussBareiss(matPoly, polyRing)
    }

    fun <T> charPoly(matrix: GenMatrix<T>, mc: UnitRing<T>): Polynomial<T> {
//        return charPolyDefinition(matrix, mc) //
        return charPolyAndAdj(matrix, mc).first //
    }

    /*
    Decomposition
     */
    fun <T> decompRank(x: GenMatrix<T>, model: Field<T>): Pair<Matrix<T>, Matrix<T>> {
        val m = AMatrix.copyOf(x, model)
        val pivots = toEchelon(m, model)
        val L = AMatrix(x.row, pivots.size, model) { i, j -> x[i, pivots[j]] }
        val R = m.subMatrix(0, 0, pivots.size, m.column)
        return L to R
    }


    private fun <T> decompQR0(A: GenMatrix<T>, mc: Reals<T>): Pair<Matrix<T>, MutableMatrix<T>> {
        //Re-written by lyc at 2021-04-30 13:00
        A.requireSquare()
        val vs = colVectors(A, mc)
        val R = zero(A.row, A.column, mc)
        val ws = ArrayList<MutableVector<T>>(A.row)
        val Q = zero(A.row, A.column, mc)
        for (i in 0 until A.row) {
            val u: MutableVector<T> = VectorImpl.copyOf(vs[i], mc)
            for (j in 0 until i) {
                val k = u.inner(ws[j])
                u.minusAssignTimes(k, ws[j])
                R[j, i] = k
            }
            if (!u.isZero) {
                val length = u.norm()
                R[i, i] = length
                u.divAssign(length)
            }
            ws += u
        }
//        val Q = Matrix.fromColumns(ws)
        return Q to R
    }


    /**
     * Returns the QR-decomposition of a square matrix `A = QR`, where `Q` is an orthogonal matrix and `R` is an
     * upper-triangle matrix. If this matrix is invertible, there is only one decomposition.
     *
     * @return `(Q, R)` as a pair
     */
    fun <T> decompQR(A: GenMatrix<T>, model: Reals<T>): Pair<Matrix<T>, Matrix<T>> {
        return decompQR0(A, model)
    }

    /**
     *
     */
    fun <T> decompKAN(A: GenMatrix<T>, model: Reals<T>): Triple<Matrix<T>, Vector<T>, Matrix<T>> {
        //Created by lyc at 2021-05-11 20:25
        //TODO: re-implement this method for general cases
        val (Q, R) = decompQR0(A, model)
        val d = R.diag()
        val one = model.one
        for (i in 0 until R.row) {
            R[i, i] = one
            R.divideRow(i, d[i], i + 1)
        }
        return Triple(Q, d, R)
    }

    private fun <T> checkSymmetric(A: GenMatrix<T>, mc: EqualPredicate<T>) {
        A.requireSquare()
        for (i in 0..<A.row) {
            for (j in 0..<i) {
                require(mc.isEqual(A[i, j], A[j, i])) { "Not symmetric!" }
            }
        }
    }


    /**
     * Computes the LU decomposition of the given matrix `A` , returns a pair of matrices `(L,U)` such that
     * `A = LU`, `P` is a permutation matrix, `L` is a lower triangular matrix with 1 as diagonal elements, and
     * `U` is an upper triangular matrix.
     *
     * It is required that the matrix is invertible.
     *
     * **Note**: This method is not designed for numerical computation.
     *
     * @return
     */
    fun <T> decompLU(m: GenMatrix<T>, mc: Field<T>): Pair<Matrix<T>, Matrix<T>> {
        require(m.isSquare) {
            "The matrix must be square!"
        }
        val n = m.row
        val upper = AMatrix.copyOf(m, mc)
        val lower = zero(n, n, mc)
        for (k in 0 until m.row) {
            lower[k, k] = mc.one
            for (i in (k + 1) until m.row) {
                val lambda = mc.eval {
                    upper[i, k] / upper[k, k]
                }
                lower[i, k] = lambda
                upper[i, k] = mc.zero
                upper.multiplyAddRow(k, i, mc.negate(lambda), k + 1)
            }
        }
        return lower to upper
    }

    /**
     * Decomposes a symmetric semi-positive definite matrix `A = L L^T`, where
     * `L` is a lower triangular matrix.
     *
     * @return a lower triangular matrix `L`.
     */
    fun <T> decompCholesky(A: GenMatrix<T>, mc: Reals<T>): Matrix<T> {
        require(A.isSquare) {
            "The matrix must be square!"
        }
        val n = A.row

        val L = zero(n, n, mc)
        for (j in 0 until n) {
            var t = A[j, j]
            for (k in 0 until j) {
                t = mc.eval { t - L[j, k] * L[j, k] }
            }
            t = mc.sqrt(t)
            L[j, j] = t
            // l_{jj} = sqrt(a_{jj} - sum(0,j-1, l_{jk}^2))

            for (i in (j + 1) until n) {
                var a = A[i, j]
                for (k in 0 until j) {
                    a = mc.eval { a - L[i, k] * L[j, k] }
                }
                a = mc.eval { a / t }
                L[i, j] = a
                // l_{ij} = (a_{ij} - sum(0,j-1,l_{il}l_{jl}))/l_{jj}
            }
        }
        return L
    }

    /**
     * Decomposes a symmetric matrix `A = L D L^T`, where
     * `L` is a lower triangular matrix and `D` is a diagonal matrix.
     *
     * @return `(L, diag(D))`, where `L` is a lower triangular matrix, `diag(D)` is a vector of diagonal elements
     * of `D`.
     */
    fun <T> decompCholeskyD(A: GenMatrix<T>, mc: Field<T>): Pair<Matrix<T>, Vector<T>> {
        require(A.isSquare) {
            "The matrix must be square!"
        }
        val n = A.row

        val L = zero(n, n, mc)
        val d = ArrayList<T>(n)

        for (j in 0 until n) {
            var t = A[j, j]
            for (k in 0 until j) {
                t = mc.eval { t - L[j, k] * L[j, k] * d[k] }
            }
            d += t
            // d_j = a_{jj} - sum(0,j-1, l_{jk}^2)
            L[j, j] = mc.one
            // l_{jj} = a_{jj} - sum(0,j-1, l_{jk}^2)

            for (i in (j + 1) until n) {
                var a = A[i, j]
                for (k in 0 until j) {
                    a = mc.eval { a - L[i, k] * L[j, k] * d[k] }
                }
                L[i, j] = mc.eval { a / t }
                // l_{ij} = (a_{ij} - sum(0,j-1,d_k * l_{ik}l_{jk}))
            }
        }
        return L to Vector.of(d, mc)
    }


    /*
    Normal forms
     */


    /**
     * Transforms the given matrix `M` over a field to an upper triangular form using row operations (Gaussian elimination).
     *
     *
     * @return a list of strictly increasing pivots of the column. The size of it is equal to the rank of the matrix.
     */
    fun <T> toUpperTriangle(M: MutableMatrix<T>, model: Field<T>, column: Int = M.column): List<Int> {
        //Created by lyc at 2021-04-29
        val row = M.row
        var r = 0
        val pivots = ArrayList<Int>(min(M.row, column))
        val zero = model.zero
        /*
        c = pivots[r] then M[r,c] is the first non-zero element in that row
         */
        for (c in 0 until column) {
            if (r >= row) break
            val rStart = (r..<row).firstOrNull { !model.isZero(M[it, c]) } ?: continue
            if (rStart != r) {
                M.swapRow(r, rStart)
            }
            val f = M[r, c]
            for (i in (rStart + 1)..<row) {
                if (model.isZero(M[i, c])) continue
                val k = model.eval { -M[i, c] / f }
                M[i, c] = zero
                M.multiplyAddRow(r, i, k, c + 1)
            }
            pivots += c
            r++
        }
        return pivots
    }

    /**
     *
     * @return a list of strictly increasing pivots of the column. The size of it is equal to the rank of the matrix.
     */
    fun <T> toEchelon(M: MutableMatrix<T>, mc: Field<T>, column: Int = M.column): List<Int> {
        //Created by lyc at 2021-04-29
        val pivots = toUpperTriangle(M, mc, column)
        val zero = mc.zero
        for (i in pivots.lastIndex downTo 0) {
            val j = pivots[i]
            if (!mc.isOne(M[i, j])) {
                M.divideRow(i, M[i, j], j + 1)
                M[i, j] = mc.one
            }
            for (k in (i - 1) downTo 0) {
                if (mc.isZero(M[k, j])) {
                    continue
                }
                val q = mc.eval { -M[k, j] }
                M.multiplyAddRow(i, k, q, j + 1)
                M[k, j] = zero
            }
        }
        return pivots
    }

    /**
     * Transforms the given matrix `M` over a Euclidean domain to an upper triangular form.
     */
    internal fun <T> toUpperEUD0(M: MutableMatrix<T>, mc: EuclideanDomain<T>, column: Int = M.column): List<Int> {
        val row = M.row
        var r = 0
        val pivots = ArrayList<Int>(min(M.row, column))
        for (c in 0 until column) {
            if (r >= row) break
            if (reduceByRowEUD(M, mc, r, c) == 0) continue
            pivots += c
            r++
        }
        return pivots
    }


    internal fun <T> toHermitForm0(M: MutableMatrix<T>, mc: Integers<T>, column: Int = M.column): List<Int> {
        val pivots = toUpperEUD0(M, mc, column)
        for (i in pivots.lastIndex downTo 0) {
            val j = pivots[i]
            if (mc.isNegative(M[i, j])) {
                M.negateRow(i)
            }
            val d = M[i, j]
            for (k in (i - 1) downTo 0) {
                if (mc.isZero(M[k, j])) {
                    continue
                }
                var q = mc.eval { -divideToInteger(M[k, j], d) }
                if (mc.isNegative(M[k, j])) {
                    q = mc.increase(q)
                }
                M.multiplyAddRow(i, k, q, j)
            }
        }
        return pivots
    }


    /**
     * Transform this matrix to (row) Hermit Form.
     */
    fun <T> toHermitForm(A: GenMatrix<T>, mc: Integers<T>): Matrix<T> {
        val M = AMatrix.copyOf(A, mc)
        toHermitForm0(M, mc)
        return M
    }

    /**
     * Transforms the matrix `M` over a Euclidean domain to its Smith Normal Form.
     * The Smith Normal Form is a diagonal matrix where the diagonal entries `d_i` satisfy:
     * * `d_i | d_{i+1}` for all `i`;
     * * `d_i` is unique up to multiplication by a unit.
     * * `d_i` is the greatest common divisor of all `i x i` minors of `M`.
     *
     *
     * The entries `d_i` are the invariant factors of the matrix.
     *
     * @param A The matrix to be transformed
     */
    fun <T> toSmithForm(A: GenMatrix<T>, mc: EuclideanDomain<T>): AMatrix<T> {
        val factors = invariantFactors(A, mc)
        // build a diagonal matrix
        val M = zero(A.row, A.column, mc)
        for (i in factors.indices) {
            M[i, i] = factors[i]
        }
        return M
    }

    /**
     * Computes the **non-zero** invariant factors of the given matrix `A` over a Euclidean domain.
     *
     * The `i`-th invariant factors are the greatest common divisors of all `i x i` minors of the matrix.
     */
    fun <T> invariantFactors(A: GenMatrix<T>, mc: EuclideanDomain<T>): List<T> {
        return toSmithForm0(AMatrix.copyOf(A, mc), mc)
    }


    fun <T> detDivisors(A: GenMatrix<T>, mc: EuclideanDomain<T>): List<T> {
        val invFactors = invariantFactors(A, mc)
        // d_k = a_k * a_{k-1} * ... * a_1
        val result = ArrayList<T>(invFactors.size)
        var d = mc.one
        for (i in invFactors.indices) {
            d = mc.multiply(d, invFactors[i])
            result += d
        }
        return result
    }

    /**
     * Transforms the given matrix `M` over a Euclidean domain to its Smith Normal Form.
     * Returns a list of invariant factors (non-zero diagonal entries).
     *
     * @param M The matrix to be transformed (must implement `MutableMatrix<T>`).
     * @param mc The Euclidean domain calculator for the matrix entries.
     * @return List of invariant factors (non-zero diagonal entries) of the Smith Normal Form.
     */
    internal fun <T> toSmithForm0(M: MutableMatrix<T>, mc: EuclideanDomain<T>): List<T> {
        val rows = M.row
        val cols = M.column
        val invariants = mutableListOf<T>()
        var c = 0

        for (r in 0..<rows) {
            if (c < cols && reduceByRowEUD(M, mc, r, c) == 0) {
                // the elements in the column are all zero, move to the next column
                c++
            }
            if (c >= cols) break // all columns are reduced
            // M[r,c] is non-zero and the elements below it are all zero
            while (reduceByColEUD(M, mc, r, c, zeroed = true) == 2) {
                // the column is changed, so we have to reduce the by row again
                reduceByRowEUD(M, mc, r, c)
            }
            // now we have to reduce the rest of the elements with i >= r, j > c
            for (i in r..<rows) {
                for (j in (c + 1)..<cols) {
                    val (g, _, v) = mc.gcdUV(M[r, c], M[i, j])
                    if (mc.isZero(v)) continue // M[r,c] | M[i,j], continue
                    // make a gcd at M[r,j] by row and col trans
                    // then swap col c and j
                    M.multiplyAddRow(i, r, v, j + 1)
                    // col trans can be omitted since elements below M[r,c] are all zero
                    M.swapCol(c, j, r + 1)
                    M[r, j] = mc.negate(M[r, c]) // negate so det is not changed by swapping
                    M[r, c] = g
                    // clean up the elements in the column c and row r
                    do {
                        reduceByRowEUD(M, mc, r, c)
                    } while (reduceByColEUD(M, mc, r, c, zeroed = true) == 2)
                }
            }
            invariants += M[r, c]
            c++
        }

        return invariants
    }

    /**
     *
     * @return 0 if the row is all zero, 1 if any row is reduced, 2 if the original row is changed.
     */
    private fun <T> reduceByRowEUD(
        M: MutableMatrix<T>, mc: EuclideanDomain<T>, r: Int, c: Int, zeroed: Boolean = false, colStart: Int = c + 1
    ): Int {
        return templateReduceInEUD(
            mc, M.row, r, c, zeroed, colStart, M::get, M::set,
            M::swapRow, M::multiplyAddRow, M::multiplyRow, M::transformRows
        )
    }


    private fun <T> reduceByColEUD(
        M: MutableMatrix<T>, mc: EuclideanDomain<T>, r: Int, c: Int, zeroed: Boolean = false, rowStart: Int = r + 1
    ): Int {
        return templateReduceInEUD(
            mc, M.column, c, r, zeroed, rowStart,
            mGet = { i, j -> M[j, i] },
            mSet = { i, j, v -> M[j, i] = v },
            M::swapCol, M::multiplyAddCol, M::multiplyCol, M::transformCols
        )
    }

    private inline fun <T> templateReduceInEUD(
        mc: EuclideanDomain<T>,
        rowEnd: Int, r: Int, c: Int,
        zeroed0: Boolean, colStart: Int,
        mGet: (Int, Int) -> T, mSet: (Int, Int, T) -> Unit,
        mSwapRow: (Int, Int, Int) -> Unit,
        mMulAddRow: (Int, Int, T, Int) -> Unit, mMulRow: (Int, T, Int) -> Unit,
        mTransformRows: (Int, Int, T, T, T, T, Int) -> Unit
    ): Int {
        val rStart = (r until rowEnd).firstOrNull { !mc.isZero(mGet(it, c)) } ?: return 0
        if (rStart != r) {
            mSwapRow(r, rStart, c)
        }
        // the rows in (r, rStart] are all zero
        var res = 1
        var zeroed = zeroed0
        for (i in (rStart + 1) until rowEnd) {
            val b = mGet(i, c)
            if (mc.isZero(b)) continue
            val h = mGet(r, c)
            val (gcd, u, v, hd, bd) = mc.gcdExtendedFull(h, b)
            // uh + vb = gcd(h, b)
            // uni-modular transform: [u, v; -b/d, h/d], det = (au - bv)/d = 1
            // M[r] = u M[r] + v M[i] = gcd(a,b), M[r,c] = gcd
            // M[i] = -b/d M[r] + a/d M[i], M[i,c] = 0
            if (mc.isZero(v)) {
                // h | b, so u = 1, v = 0, hd = 1, bd = b/d = b/h
                // just to optimize the computation: M[i] = M[i] - b/h M[r], while M[r] is not changed
                if (!zeroed) {
                    mMulAddRow(r, i, mc.negate(bd), colStart)
                }
            } else {
                if (zeroed) {
                    mMulAddRow(i, r, v, colStart) // M[r] = u M[r] + v M[i] = v M[i]
                    mMulRow(i, hd, colStart) // M[i] = hd M[i] - bd M[r] = hd M[i]
                } else {
                    mTransformRows(r, i, u, v, mc.negate(bd), hd, colStart)
                }
                res = 2
                zeroed = false
            }
            mSet(r, c, gcd)
            mSet(i, c, mc.zero)

        }
        return res
    }


    /**
     * Returns the congruence diagonal normal form `J` of matrix `A` and the corresponding transformation `P`,
     * which satisfies
     *
     *     P.T * A * P = J
     *
     * @return `(J, P)`.
     */
    fun <T> toCongDiagonalForm(A: GenMatrix<T>, mc: Field<T>): Pair<Matrix<T>, Matrix<T>> {
        checkSymmetric(A, mc)
        //Re-written by lyc at 2021-04-30 13:00
        val n = A.row
        val x = zero(2 * n, n, mc)
        x.setAll(0, 0, A)
        val one = mc.one
        for (i in 0 until n) {
            x[i + n, i] = one
        }
        var pos = 0
        while (pos < n) {
            if (mc.isZero(x[pos, pos])) {
                var pi = -1
                var pj = -1
                SEARCH@ for (i in pos until n) {
                    for (j in pos..i) {
                        if (!mc.isZero(x[j, j])) {
                            pi = i
                            pj = j
                            break@SEARCH
                        }
                    }
                }
                if (pj < 0) {
                    break
                }
                if (pj != pos) {
                    x.multiplyAddRow(pj, pos, one)
                    x.multiplyAddCol(pj, pos, one)
                }
                x.multiplyAddRow(pi, pos, one)
                x.multiplyAddCol(pi, pos, one)

            }
            for (i in pos + 1 until n) {
                if (mc.isZero(x[pos, i])) {
                    continue
                }
                val k = mc.negate(mc.divide(x[pos, i], x[pos, pos]))
                x.multiplyAddRow(pos, i, k)
                x.multiplyAddCol(pos, i, k)
            }
            pos++
        }
        val m1 = x.subMatrix(0, 0, n, n)
        val m2 = x.subMatrix(n, 0, 2 * n, n)
        return m1 to m2
    }


    fun <T> toHessenberg(matrix: GenMatrix<T>, mc: Field<T>): Matrix<T> {
        require(matrix.isSquare)
        val H = AMatrix.copyOf(matrix, mc)
        val n = matrix.row

        for (m in 0 until (n - 1)) {
            val i0 = (m + 2 until n).firstOrNull { !mc.isZero(H[it, m]) } ?: continue
            if (mc.isZero(H[m + 1, m])) {
                H.swapRow(i0, m + 1, m)
                H.swapCol(i0, m + 1)
            }

            val t = H[m + 1, m]
            for (i in (m + 2) until n) {
                if (mc.isZero(H[i, m])) continue
                val u = mc.eval { H[i, m] / t }
                H.multiplyAddRow(m + 1, i, mc.negate(u), m)
                H[i, m] = mc.zero
                H.multiplyAddCol(i, m + 1, mc.reciprocal(u))
            }
        }
        return H

    }


    /*
    Solve linear equations
     */


    private fun <T> nullSpaceOf(
        expanded: GenMatrix<T>, column: Int, pivots: List<Int>, mc: Field<T>
    ): VectorSpace<T> {

        val k = column - pivots.size
        if (k == 0) return VectorSpace.zero(column, mc)

        val basis = ArrayList<Vector<T>>(k)
        val minusOne = mc.negate(mc.one)
        var pivotIndex = 0

        for (j in 0 until column) {
            if (pivotIndex < pivots.size && pivots[pivotIndex] == j) {
                pivotIndex++
            } else {
                val v = Vector.zero(column, mc).apply { this[j] = minusOne }
                pivots.forEachIndexed { i, pivot -> v[pivot] = expanded[i, j] }
                basis += v
            }
        }
        return DVectorSpace(mc, column, basis)
    }

    private fun <T> specialSolutionOf(expanded: GenMatrix<T>, column: Int, pivots: List<Int>, mc: Field<T>): Matrix<T> {
        val special = zero(column, expanded.column - column, mc)
        for (k in pivots.indices) {
            val pk = pivots[k]
            for (j in special.colIndices) {
                special[pk, j] = expanded[k, j + column]
            }
        }
        return special
    }

    /**
     * Solve the linear equation `AX = B` with `expanded = (A, B)` in the augmented matrix form.
     * The column `colSep` is the column separating `A` and `B`, namely `A.column = colSep`.
     *
     */
    fun <T> solveLinear(augmented: MutableMatrix<T>, colSep: Int, mc: Field<T>):
            Triple<Matrix<T>, VectorSpace<T>, Boolean> {
        val pivots = toEchelon(augmented, mc, colSep)
        val r = pivots.size
        val special = specialSolutionOf(augmented, colSep, pivots, mc)
        val basis = nullSpaceOf(augmented, colSep, pivots, mc)
        val solvable = (colSep until augmented.column).all { j ->
            (r until augmented.row).all { i ->
                mc.isZero(augmented[i, j])
            }
        }
        return Triple(special, basis, solvable)
    }

    fun <T> solveLinear(A: GenMatrix<T>, B: GenMatrix<T>, model: Field<T>): Triple<Matrix<T>, VectorSpace<T>, Boolean> {
        require(A.row == B.row)
        val augmented = concatCol(A, B, model)
        return solveLinear(augmented, A.column, model)
    }

    /**
     * Solves the linear equation `Ax = b` with the given matrix `A` and vector `b`.
     */
    fun <T> solveLinear(A: GenMatrix<T>, b: GenVector<T>, model: Field<T>): LinearEquationSolution<T>? {
        require(A.row == b.size)
        val augmented = zero(A.row, A.column + 1, model)
        val col = A.column
        augmented.setAll(0, 0, A)
        for (i in b.indices) {
            augmented[i, col] = b[i]
        }
        val (special, basis, sol) = solveLinear(augmented, col, model)
        if (!sol) return null
        val v = special.colAt(0)
        return LinearEquationSolution(v, basis)
    }

    /**
     * Solves the homogeneous linear equation `Ax = 0` with the given matrix `A`.
     */
    fun <T> solveHomo(A: GenMatrix<T>, model: Field<T>): VectorSpace<T> {
        val expanded = AMatrix.copyOf(A, model)
        val pivots = toEchelon(expanded, model)
        return nullSpaceOf(expanded, A.column, pivots, model)
    }

}


//fun main() {
//    val Z = NumberModels.intAsIntegers()
//    val n = 4
//    val rng = Random(11)
//    val A = Matrix(n, Z) { i, j ->
//        rng.nextInt(10)
//    }
//    println(A)
//    println("Computing the invariant factors")
//    val invFactors = MatrixImpl.invariantFactors(A, Z)
//    println(invFactors)
//    val accProd = invFactors.scan(1) { acc, factor -> acc * factor }.drop(1)
//    println(accProd)
//    val gcds = mutableListOf<Int>()
//    for (k in 1..n) {
//        val rows = IterUtils.comb(A.row, k, false)
//        val cols = IterUtils.comb(A.column, k, false)
//        val minors = IterUtils.prod2(rows, cols).map { A.slice(it.first, it.second).det() }.toList().toIntArray()
//        val gcd = NTFunctions.gcd(*minors)
//        if (gcd == 0) {
//            break
//        }
//        gcds.add(gcd)
//    }
//    println(gcds)
//}

