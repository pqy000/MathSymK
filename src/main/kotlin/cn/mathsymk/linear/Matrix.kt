package cn.mathsymk.linear

import cn.mathsymk.ValueEquatable
import cn.mathsymk.ModeledMathObject
import cn.mathsymk.model.Complex
import cn.mathsymk.model.struct.*
import cn.mathsymk.structure.*
import java.util.function.Function

interface Matrix<T> : GenMatrix<T>, ModeledMathObject<T, EqualPredicate<T>>,
    AlgebraModel<T, Matrix<T>>, MulGroupModel<Matrix<T>> {

    /*
    Matrix
     */


    fun rowAt(rowIdx: Int): Vector<T> {
        return Vector(column, model) { colIdx -> this[rowIdx, colIdx] }
    }

    fun colAt(colIdx: Int): Vector<T> {
        return Vector(row, model) { rowIdx -> this[rowIdx, colIdx] }
    }

    fun rowVectors(): List<Vector<T>> {
        return rowIndices.map { rowAt(it) }
    }

    fun colVectors(): List<Vector<T>> {
        return colIndices.map { colAt(it) }
    }

    override fun applyAll(f: (T) -> T): Matrix<T> {
        return MatrixImpl.apply1(this, model, f)
    }

    /*
    MathObject
     */

    override fun <S> mapTo(newModel: EqualPredicate<S>, mapping: Function<T, S>): Matrix<S> {
        return MatrixImpl.apply1(this, newModel, mapping::apply)
    }


    override fun valueEquals(obj: ValueEquatable<T>): Boolean {
        if (obj !is Matrix) return false
        if (row != obj.row || column != obj.column) return false
        return rowIndices.all { r -> colIndices.all { c -> model.isEqual(this[r, c], obj[r, c]) } }
    }

    /*
    VectorModel
     */

    override val isZero: Boolean
        get() {
            val model = model as AddGroup<T>
            return elementSequence().all { model.isZero(it) }
        }

    override fun plus(y: Matrix<T>): Matrix<T> {
        return MatrixImpl.add(this, y, model as AddSemigroup<T>)
    }

    override fun unaryMinus(): Matrix<T> {
        return MatrixImpl.negate(this, model as AddGroup<T>)
    }

    override fun scalarMul(k: T): Matrix<T> {
        return MatrixImpl.multiply(this, k, model as MulSemigroup)
    }

    override fun scalarDiv(k: T): Matrix<T> {
        return MatrixImpl.divide(this, k, model as MulGroup)
    }


    /*
    Matrix operations
     */

    /**
     * Returns the matrix product of this matrix and the given matrix.
     * It is required that `this.column == y.row`.
     *
     *
     * Let `C = A * B`, then `C[i, j] = sum(k; A[i, k] * B[k, j])` for all `i` and `j`.
     *
     */
    infix fun matmul(y: Matrix<T>): Matrix<T> {
        return MatrixImpl.matmul(this, y, model as Ring)
    }

    /**
     * Returns the matrix product of this matrix and the given matrix.
     * It is required that `this.column == y.row`.
     *
     *
     * Let `C = A * B`, then `C[i, j] = sum(k; A[i, k] * B[k, j])` for all `i` and `j`.
     *
     * @see matmul
     *
     */
    override operator fun times(y: Matrix<T>): Matrix<T> {
        return this.matmul(y)
    }


    infix fun matmul(v: Vector<T>): Vector<T> {
        return MatrixImpl.matmul(this, v, model as Ring)
    }

    operator fun times(v: Vector<T>): Vector<T> {
        return this.matmul(v)
    }

    override val isInvertible: Boolean
        get() {
            return MatrixImpl.isInvertible(this, model as UnitRing)
        }

    override fun inv(): Matrix<T> {
        return MatrixImpl.inverse(this, model as UnitRing)
    }

    /**
     * Computes the determinant of this matrix.
     *
     * The determinant is defined as:
     *
     *     det(A) = \sum_{σ ∈ S_n} (-1)^σ \prod_{i=1}^n A_{i, σ(i)}.
     *
     */
    fun det(): T {
        return MatrixImpl.det(this, model as Ring)
    }

    /**
     * Computes the rank of this matrix.
     */
    fun rank(): Int {
        return MatrixImpl.rank(this, model as Field)
    }

    /**
     * Returns the trace if this matrix, that is, the sum of diagonal elements.
     *
     * It is required that this matrix is square.
     *
     */
    fun trace(): T {
        return MatrixImpl.trace(this, model as AddSemigroup<T>)
    }

    /**
     * Gets the diagonal of this matrix as a vector.
     */
    fun diag(): Vector<T> {
        require(isSquare)
        return Vector(row, model) { i -> this[i, i] }
    }

    /**
     * Returns the sum of all elements in this matrix.
     */
    fun sumAll(): T {
        return MatrixImpl.sumAll(this, model as AddSemigroup<T>)
    }

    /**
     * Returns a transposed view of this matrix.
     */
    fun transpose(): Matrix<T> {
        return TransposedMatrixView(this)
    }

    /**
     * Returns a transposed view of this matrix.
     */
    val T: Matrix<T>
        get() = transpose()


    /**
     * Gets a sub-matrix in this matrix as a view.
     *
     * @param rowStart the start index of the row, inclusive.
     * @param rowEnd the end index of the row, exclusive.
     * @param colStart the start index of the column, inclusive.
     * @param colEnd the end index of the column, exclusive.
     *
     */
    fun subMatrix(rowStart: Int = 0, colStart: Int = 0, rowEnd: Int = row, colEnd: Int = column): Matrix<T> {
//        require(0 <= rowStart && rowEnd <= row && rowStart < rowEnd)
//        require(0 <= colStart && colEnd <= column && colStart < colEnd)
        // checked in SubMatrixView
        return SubMatrixView(this, rowStart, colStart, rowEnd, colEnd)
    }

    /**
     * Returns a sliced matrix view of this matrix with the given row and column indices.
     */
    fun slice(rows: IntArray, cols: IntArray): Matrix<T> {
        return SlicedMatrixView(this, rows, cols)
    }

    /**
     * Returns the minor of this matrix at the position `(i, j)`,
     * namely the sub-matrix obtained by deleting the `i`-th row and `j`-th column.
     */
    fun minor(i: Int, j: Int): Matrix<T> {
        val rows = IntArray(this.row - 1) { t -> if (t < i) t else t + 1 }
        val cols = IntArray(this.column - 1) { t -> if (t < j) t else t + 1 }
        return slice(rows, cols)
    }

    fun minor(rows: IntArray, cols: IntArray): Matrix<T> {
        val remRows = remainingIndices(row, rows)
        val remCols = remainingIndices(column, cols)
        return slice(remRows, remCols)
    }



    companion object {
        operator fun <T> invoke(row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T): Matrix<T> {
            return AMatrix.of(row, column, model, init)
        }

        operator fun <T> invoke(n: Int, model: EqualPredicate<T>, init: (Int, Int) -> T): Matrix<T> {
            return invoke(n, n, model, init)
        }

        fun <T> of(row: Int, col: Int, model: EqualPredicate<T>, vararg elements: T): Matrix<T> {
            require(row * col == elements.size)
            return AMatrix.of(row, col, model, *elements)
        }

        fun <T> product(vararg matrices: Matrix<T>): Matrix<T> {
            return product(matrices.asList())
        }

        fun <T> product(matrices: List<Matrix<T>>): Matrix<T> {
            return MatrixImpl.product(matrices, matrices.first().model as Ring)
        }

        /**
         * Creates a new matrix from a list of column vectors.
         */
        fun <T> fromColumns(columns: List<Vector<T>>): Matrix<T> {
            val row = columns.first().size
            val column = columns.size
            require(columns.all { it.size == row })
            val model = columns.first().model
            return Matrix(row, column, model) { i, j -> columns[i][j] }
        }

        /**
         * Creates a new matrix from a list of vectors as rows.
         */
        fun <T> fromRows(rows: List<Vector<T>>): Matrix<T> {
            val row = rows.size
            val column = rows.first().size
            require(rows.all { it.size == column })
            val model = rows.first().model
            return Matrix(row, column, model) { i, j -> rows[i][j] }
        }

        /**
         * Concatenates two matrix `A, B` to a new matrix `(A, B)`.
         *
         * It is required that `A` and `B` have that same row count.
         */
        fun <T> concatColumn(a: Matrix<T>, b: Matrix<T>): MutableMatrix<T> {
            return MatrixImpl.concatCol(a, b, a.model)
        }

        fun <T> zero(row: Int, column: Int, model: AddMonoid<T>): Matrix<T> {
            return MatrixImpl.zero(row, column, model)
        }

        fun <T> zero(n: Int, model: AddMonoid<T>): Matrix<T> {
            return zero(n, n, model)
        }

        fun <T> diag(model: AddMonoid<T>, elements: List<T>): Matrix<T> {
            val n = elements.size
            val zero = MatrixImpl.zero(n, n, model)
            for (i in 0 until n) {
                zero[i, i] = elements[i]
            }
            return zero
        }

        fun <T> diag(model: AddMonoid<T>, vararg elements: T): Matrix<T> {
            return diag(model, elements.asList())
        }

        fun <T> scalar(n: Int, model: AddMonoid<T>, k: T): Matrix<T> {
            val mat = MatrixImpl.zero(n, n, model)
            for (i in 0..<n) {
                mat[i, i] = k
            }
            return mat
        }

        fun <T> identity(n: Int, model: UnitRing<T>): Matrix<T> {
            return MatrixImpl.identity(n, model)
        }


        private fun remainingIndices(n: Int, indices: IntArray): IntArray {
            val set = indices.toMutableSet()
            return (0 until n).filter { it !in set }.toIntArray()
        }

        /**
         * Gets the model of `n × n` matrices over the given model.
         */
        fun <T> over(n: Int, model: UnitRing<T>): UnitRingModule<T, Matrix<T>> {
            return SqMatOverURing(n, model)
        }

        /**
         * Gets the model of `n × n` matrices over the given field.
         */
        fun <T> over(n: Int, model: Field<T>): SqMatOverField<T> {
            return SqMatOverField(n, model)
        }

        /**
         * Gets the model of general linear group `GL(n)`.
         */
        fun <T> generalLinear(n: Int, model: UnitRing<T>): GeneralLinearGroup<T> {
            return GeneralLinearGroup(n, model)
        }


    }
}

//fun <T> Matrix<T>.times(v: Vector<T>): Vector<T> {
//    return MatrixImpl.matmul(this, v, model as Ring)
//}


fun <T> RowVector<T>.matmul(m: Matrix<T>): RowVector<T> {
    return RowVector(MatrixImpl.matmul(this, m, this.v.model as Ring))
}

operator fun <T> RowVector<T>.times(m: Matrix<T>): RowVector<T> {
    return this.matmul(m)
}

//fun <K> Matrix<Complex<K>>.conjugate(): Matrix<Complex<K>> {
//    val model = model as ComplexNumbers<*,Complex<K>>
//    return applyAll(model::conj)
//}

/**
 * Returns the transpose conjugate of this matrix.
 *
 * It is required that this matrix is a matrix of complex numbers with a model of [ComplexNumbers].
 */
fun <K> Matrix<Complex<K>>.transposeConjugate(): Matrix<Complex<K>> {
    val model = model as ComplexNumbers<*,Complex<K>>
    return TransposedMatrixView(this).applyAll(model::conj)
}

/**
 * The same as [transposeConjugate].
 *
 * @see transposeConjugate
 */
val <K> Matrix<Complex<K>>.H: Matrix<Complex<K>> get() = transposeConjugate()




@JvmRecord
data class VectorAsColMatrix<T>(val v: Vector<T>) : Matrix<T> {
    override val model: EqualPredicate<T>
        get() = v.model
    override val row: Int
        get() = v.size
    override val column: Int
        get() = 1

    override fun get(i: Int, j: Int): T {
        require(j == 0)
        return v[i]
    }
}

@JvmRecord
data class VectorAsRowMatrix<T>(val v: Vector<T>) : Matrix<T> {
    override val model: EqualPredicate<T>
        get() = v.model
    override val row: Int
        get() = 1
    override val column: Int
        get() = v.size

    override fun get(i: Int, j: Int): T {
        require(i == 0)
        return v[j]
    }
}


val <T> Vector<T>.asMatrix: Matrix<T> get() = VectorAsColMatrix(this)
val <T> RowVector<T>.asMatrix: Matrix<T> get() = VectorAsRowMatrix(this.v)

/**
 * Returns a mutable copy of this matrix.
 */
fun <T> Matrix<T>.toMutable(): MutableMatrix<T> {
    return MutableMatrix.copyOf(this)
}

interface MutableMatrix<T> : Matrix<T> {
    operator fun set(i: Int, j: Int, value: T)

    fun setRow(i: Int, row: Vector<T>) {
        for (j in 0..<column) {
            this[i, j] = row[j]
        }
    }

    fun setCol(j: Int, col: Vector<T>) {
        for (i in 0..<row) {
            this[i, j] = col[i]
        }
    }

    fun setAll(row: Int, col: Int, matrix: GenMatrix<T>) {
        for (i in 0..<matrix.row) {
            for (j in 0..<matrix.column) {
                this[i + row, j + col] = matrix[i, j]
            }
        }
    }

    fun copy(): MutableMatrix<T>

    operator fun plusAssign(y: Matrix<T>) {
        val model = model as AddSemigroup
        transform { i, j, t -> model.add(t, y[i, j]) }
    }

    operator fun timesAssign(k: T) {
        val model = model as MulSemigroup
        transform { _, _, t -> model.multiply(t, k) }
    }

    operator fun divAssign(k: T) {
        val model = model as UnitRing
        transform { _, _, t -> model.exactDivide(t, k) }
    }

    /**
     * Swaps the rows `r1` and `r2` with the given column range `[colStart, colEnd)`.
     */
    fun swapRow(r1: Int, r2: Int, colStart: Int = 0, colEnd: Int = column)

    /**
     * Swaps the columns `c1` and `c2` with the given row range `[rowStart, rowEnd)`.
     */
    fun swapCol(c1: Int, c2: Int, rowStart: Int = 0, rowEnd: Int = row)

    /**
     * Negates the row `r` with the given column range `[colStart, colEnd)`.
     */
    fun negateRow(r: Int, colStart: Int = 0, colEnd: Int = column)

    /**
     * Negates the column `c` with the given row range `[rowStart, rowEnd)`.
     */
    fun negateCol(c: Int, rowStart: Int = 0, rowEnd: Int = row)

    /**
     * Multiplies the row `r` by `k` with the given column range `[colStart, colEnd)`.
     */
    fun multiplyRow(r: Int, k: T, colStart: Int = 0, colEnd: Int = column)

    /**
     * Divides the row `r` by `k` with the given column range `[colStart, colEnd)`.
     */
    fun divideRow(r: Int, k: T, colStart: Int = 0, colEnd: Int = column)

    /**
     * Multiplies the column `c` by `k` with the given row range `[rowStart, rowEnd)`.
     */
    fun multiplyCol(c: Int, k: T, rowStart: Int = 0, rowEnd: Int = row)

    /**
     * Divides the column `c` by `k` with the given row range `[rowStart, rowEnd)`.
     */
    fun divideCol(c: Int, k: T, rowStart: Int = 0, rowEnd: Int = row)

    /**
     * Adds the row `r1` multiplied by `k` to the row `r2` with the given column range `[colStart, colEnd)`:
     *
     *    this[r2,j] = this[r2,j] + k * this[r1,j]     for j in [colStart, colEnd)
     */
    fun multiplyAddRow(r1: Int, r2: Int, k: T, colStart: Int = 0, colEnd: Int = column)

    /**
     * Adds the column `c1` multiplied by `k` to the column `c2` with the given row range `[rowStart, rowEnd)`:
     *
     *    this[i,c2] = this[i,c2] + k * this[i,c1]     for i in [rowStart, rowEnd)
     */
    fun multiplyAddCol(c1: Int, c2: Int, k: T, rowStart: Int = 0, rowEnd: Int = row)

    /**
     *     v1 = this[r1], v2 = this[r2]
     *     this[r1] = a11 * v1 + a12 * v2
     *     this[r2] = a21 * v1 + a22 * v2
     */
    fun transformRows(
        r1: Int, r2: Int, a11: T, a12: T, a21: T, a22: T,
        colStart: Int = 0, colEnd: Int = column
    )

    fun transformCols(
        c1: Int, c2: Int, a11: T, a12: T, a21: T, a22: T,
        rowStart: Int = 0, rowEnd: Int = row
    ) {
        val model = model as Ring
        val A = this
        for (i in rowStart until rowEnd) {
            val v1 = A[i, c1]
            val v2 = A[i, c2]
            with(model) {
                A[i, c1] = a11 * v1 + a12 * v2
                A[i, c2] = a21 * v1 + a22 * v2
            }
        }
    }

    fun negateInPlace()

    companion object {
        operator fun <T> invoke(
            row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T
        ): MutableMatrix<T> {
            return AMatrix.of(row, column, model, init)
        }

        fun <T> copyOf(matrix: Matrix<T>): MutableMatrix<T> {
            return AMatrix.copyOf(matrix, matrix.model)
        }

        fun <T> zero(row: Int, column: Int, model: AddMonoid<T>): MutableMatrix<T> {
            return MatrixImpl.zero(row, column, model)
        }

        fun <T> zero(n : Int, model: AddMonoid<T>): MutableMatrix<T> {
            return zero(n, n, model)
        }

        fun <T> identity(n: Int, model: UnitRing<T>): MutableMatrix<T> {
            return MatrixImpl.identity(n, model)
        }

        fun <T> concatColumn(a: Matrix<T>, b: Matrix<T>): MutableMatrix<T> {
            return MatrixImpl.concatCol(a, b, a.model)
        }
    }
}

inline fun <T> MutableMatrix<T>.transform(f: (Int, Int, T) -> T) {
    for (i in 0 until row) {
        for (j in 0 until column) {
            this[i, j] = f(i, j, this[i, j])
        }
    }
}

open class MatOverModel<T>(val row: Int, val col: Int, open val model: EqualPredicate<T>) {
    fun mat(init: (Int, Int) -> T): Matrix<T> {
        return Matrix(row, col, model, init)
    }

    fun mat(vararg elements: T): Matrix<T> {
        return Matrix.of(row, col, model, *elements)
    }
}


open class MatOverAGroup<T>(row: Int, col: Int, override val scalars: Ring<T>) : MatOverModel<T>(row, col, scalars),
    Module<T, Matrix<T>> {
    override fun contains(x: Matrix<T>): Boolean {
        return x.row == row && x.column == col
    }

    override fun isEqual(x: Matrix<T>, y: Matrix<T>): Boolean {
        require(x in this && y in this)
        return MatrixImpl.isEqual(x, y, scalars)
    }

    override fun scalarMul(k: T, v: Matrix<T>): Matrix<T> {
        require(v in this)
        return MatrixImpl.multiply(v, k, scalars)
    }

    override val zero: Matrix<T>
        get() = Matrix.zero(row, col, scalars)

    override fun isZero(x: Matrix<T>): Boolean {
        require(x in this)
        return MatrixImpl.isZero(x, scalars)
    }

    override fun negate(x: Matrix<T>): Matrix<T> {
        require(x in this)
        return MatrixImpl.negate(x, scalars)
    }

    override fun add(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
        require(x in this && y in this)
        return MatrixImpl.add(x, y, scalars)
    }

    override fun subtract(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
        require(x in this && y in this)
        return MatrixImpl.subtract(x, y, scalars)
    }

    override fun multiplyLong(x: Matrix<T>, n: Long): Matrix<T> {
        require(x in this)
        return MatrixImpl.multiplyLong(x, n, scalars)
    }
}


open class SqMatOverURing<T>(n: Int, override val scalars: UnitRing<T>) :
    MatOverAGroup<T>(n, n, scalars),
    UnitRingModule<T, Matrix<T>> {

    inline val n: Int get() = row

    override fun contains(x: Matrix<T>): Boolean {
        return x.row == n && x.column == n
    }

    override val one: Matrix<T>
        get() = Matrix.identity(n, scalars)

    override fun multiply(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
        require(x in this && y in this)
        return MatrixImpl.matmul(x, y, scalars)
    }

    override fun fromScalar(r: T): Matrix<T> {
        return Matrix.scalar(n, scalars, r)
    }

    override fun product(ps: List<Matrix<T>>): Matrix<T> {
        if (ps.isEmpty()) return one
        require(ps.all { it in this })
        return MatrixImpl.product(ps, scalars)
    }

    override fun sum(elements: List<Matrix<T>>): Matrix<T> {
        require(elements.all { it in this })
        return MatrixImpl.sum(elements, scalars)
    }

    override fun isUnit(x: Matrix<T>): Boolean {
        return scalars.isUnit(x.det())
    }

    fun isInvertible(x: Matrix<T>): Boolean {
        return MatrixImpl.isInvertible(x, scalars)
    }

    fun inverse(x: Matrix<T>): Matrix<T> {
        return MatrixImpl.inverse(x, scalars)
    }
}

open class SqMatOverField<T>(n: Int, override val scalars: Field<T>) :
    SqMatOverURing<T>(n, scalars), Algebra<T, Matrix<T>> {
    override fun scalarDiv(x: Matrix<T>, k: T): Matrix<T> {
        require(x in this)
        return MatrixImpl.divide(x, k, scalars)
    }


}

open class GeneralLinearGroup<T>(val n: Int, override val model: UnitRing<T>) :
    MatOverModel<T>(n, n, model), MulGroup<Matrix<T>> {
    override fun contains(x: Matrix<T>): Boolean {
        return x.row == n && x.column == n && MatrixImpl.isInvertible(x, model)
    }

    override fun isEqual(x: Matrix<T>, y: Matrix<T>): Boolean {
        require(x in this && y in this)
        return MatrixImpl.isEqual(x, y, model)
    }

    override fun multiply(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
        require(x in this && y in this)
        return MatrixImpl.matmul(x, y, model)
    }

    override fun reciprocal(x: Matrix<T>): Matrix<T> {
        return MatrixImpl.inverse(x, model)
    }

    override val one: Matrix<T>
        get() = Matrix.identity(n, model)

    override fun product(ps: List<Matrix<T>>): Matrix<T> {
        if (ps.isEmpty()) return one
        require(ps.all { it in this })
        return MatrixImpl.product(ps, model)
    }
}