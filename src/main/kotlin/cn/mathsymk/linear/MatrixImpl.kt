package cn.mathsymk.linear

import cn.mathsymk.model.Multinomial
import cn.mathsymk.model.NumberModels
import cn.mathsymk.model.struct.GenMatrix
import cn.mathsymk.model.struct.GenVector
import cn.mathsymk.model.struct.rowIndices
import cn.mathsymk.structure.*
import cn.mathsymk.util.IterUtils
import kotlin.math.min

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
data class AMatrix<T> internal constructor(
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
            row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T
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
            x: AMatrix<T>, y: AMatrix<T>, model: EqualPredicate<N>, f: (T, T) -> N
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
            return AMatrix(x.row, x.column, mc) { i, j -> x[i, j] }
        }

        fun <T> of(row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T): AMatrix<T> {
            return AMatrix(row, column, model, init)
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
    rowStart: Int, rowEnd: Int, colStart: Int, colEnd: Int
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
    val origin: Matrix<T>, val rowMap: IntArray, val colMap: IntArray
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
        val A = zero(n, n, model)
        for (i in 0 until n) {
            A[i, i] = model.one
        }
        return A
    }


    /**
     *
     * @return a list of strictly increasing pivots of the column. The size of it is equal to the rank of the matrix.
     */
    internal fun <T> toUpperTriangle(M: MutableMatrix<T>, model: Field<T>, column: Int = M.column): List<Int> {
        //Created by lyc at 2021-04-29
        val row = M.row
        var i = 0
        val pivots = ArrayList<Int>(min(M.row, column))
        /*
        j = pivots[i] then M[i,j] is the first non-zero element in that row
         */
        for (j in 0 until column) {
            if (i >= row) break
            var f: T? = null
            for (i2 in i until row) {
                if (model.isZero(M[i2, j])) continue

                f = M[i2, j]
                if (i2 != i) {
                    M.swapRow(i2, i)
                }
                break
            }
            if (f == null) continue
            //not found

            for (i2 in (i + 1) until row) {
                if (model.isZero(M[i2, j])) continue
                val k = model.eval { -M[i2, j] / f }
                M[i2, j] = model.zero
                M.multiplyAddRow(i, i2, k, j + 1)
            }
            pivots += j
            i++
        }
        return pivots
    }

    /**
     *
     * @return a list of strictly increasing pivots of the column. The size of it is equal to the rank of the matrix.
     */
    internal fun <T> toEchelon(M: MutableMatrix<T>, mc: Field<T>, column: Int = M.column): List<Int> {
        //Created by lyc at 2021-04-29
        val pivots = toUpperTriangle(M, mc, column)
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
                M[k, j] = mc.zero
            }
        }
        return pivots
    }


    fun <T> inverse(m: GenMatrix<T>, model: Field<T>): AMatrix<T> {
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


    fun <T> detSmall(m: GenMatrix<T>, model: Ring<T>): T {
        require(m.isSquare())
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
     * A very time-consuming method to compute the determinant of a matrix by the definition.
     */
    fun <T> detDefinition(m: GenMatrix<T>, model: Ring<T>): T {
        require(m.isSquare())
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

    fun <T> detGaussBareiss(mat: MutableMatrix<T>, mc: UnitRing<T>): T {
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
        val n: Int = mat.row
        var d = mc.one // the denominator that we store
        var positive = true
        for (k in 0 until n) {
            //locate the top-left element used for elimination first, it must be non-zero
            if (mc.isZero(mat[k, k])) {
                val r = (k + 1..<n).firstOrNull { i -> !mc.isZero(mat[i, k]) } ?: return mc.zero
                mat.swapRow(r, k, k) // swap i-th row and k-th row, changing the sign of the determinant
                positive = !positive
            }
            val p: T = mat[k, k]
            for (i in k + 1 until n) {
                val head = mat[i, k]
                for (j in k + 1 until n) { // j<=k are all zero, just ignore them
                    mat[i,j] = mc.eval {
                        exactDivide(p * mat[i, j] - head * mat[k, j], d)
                    }
                }
            }
            d = p
        }
        return if (positive) {
            mat[n - 1, n - 1]
        } else {
            mc.negate(mat[n - 1, n - 1])
        }
    }

    /**
     * Computes the 'inverse' of the given matrix over a unit ring. This method simply compute the adjugate matrix and
     * divide it with the determinant (so it is time-consuming).
     *
     * This method can be used to compute the modular inverse of a matrix on `Z/Zn`, where n is not necessarily a prime.
     */
    fun <T> inverseInRing(M: Matrix<T>, model: UnitRing<T>): Matrix<T> {
        val det = M.det()
        if (!model.isUnit(det)) throw ArithmeticException("The determinant is not invertible")
        val adj = M.adjugate()
        return adj / det
    }

    fun <T> concatCol(a: GenMatrix<T>, b: GenMatrix<T>, model: EqualPredicate<T>): AMatrix<T> {
        require(a.row == b.row)
        return AMatrix(a.row, a.column + b.column, model) { i, j ->
            if (j < a.column) a[i, j] else b[i, j - a.column]
        }
    }
}


fun main() {
    val ints = NumberModels.intAsIntegers()
//    val A = Matrix.fromRows(
//        listOf(
//            Vector.of(ints, 3, 1, 1, 1),
//            Vector.of(ints, 1, 1, 1, 2),
//            Vector.of(ints, 2, 1, 3, 3),
//            Vector.of(ints, 4, 1, 1, 4)
//        )
//    )
//    val A = Matrix.diag(ints, 3, 1, 4)
    val mult = Multinomial.from(ints)
    val A = Matrix(4, 4, mult) { i,j ->
        mult.monomial("($i$j)")}
    println(A.joinToString())
    println(MatrixImpl.detGaussBareiss(A.toMutable(), mult))
    println(MatrixImpl.detDefinition(A, mult))
}