package cn.mathsymk.linear

import cn.mathsymk.ValueEquatable
import cn.mathsymk.ModeledMathObject
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

    fun det(): T {
        return MatrixImpl.det(this, model as Ring)
    }

    fun rank(): Int {
        TODO()
    }

    /**
     * Returns the trace if this matrix, that is, the sum of diagonal elements.
     *
     * It is required that this matrix is square.
     *
     * @return `tr(this)`
     */
    fun trace(): T {
        return MatrixImpl.trace(this, model as AddSemigroup<T>)
    }

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

    fun minor(rows : IntArray, cols : IntArray): Matrix<T> {
        val remRows = remainingIndices(row, rows)
        val remCols = remainingIndices(column, cols)
        return slice(remRows, remCols)
    }

    /**
     * Returns the cofactor of this matrix at the position `(i, j)`.
     * The cofactor is the determinant of the minor matrix at `(i, j)` with a sign determined by `(-1)^(i+j)`.
     *
     *     C(i, j) = (-1)^(i+j) * det(minor(i, j))
     */
    fun cofactor(i: Int, j: Int): T {
        val t = minor(i, j).det()
        val model = model as Ring
        return if ((i + j) % 2 == 0) t else model.negate(t)
    }

    fun adjugate(): Matrix<T> {
        return MatrixImpl.adjugate(this, model as Ring)
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
            return matrices.reduce { acc, matrix -> acc.matmul(matrix) }
            // TODO optimize
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

        fun <T> diag(elements: List<T>, model: AddMonoid<T>): Matrix<T> {
            val n = elements.size
            val zero = MatrixImpl.zero(n, n, model)
            for (i in 0 until n) {
                zero[i, i] = elements[i]
            }
            return zero
        }

        fun <T> diag(model: AddMonoid<T>, vararg elements: T): Matrix<T> {
            return diag(elements.asList(), model)
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
        fun <T> over(n : Int, model : UnitRing<T>): UnitRingModule<T, Matrix<T>> {
            return object : UnitRingModule<T, Matrix<T>> {
                override val scalars: UnitRing<T>
                    get() = model

                override fun fromScalar(r: T): Matrix<T> {
                    return Matrix.identity(n, model) * r
                }

                override fun scalarMul(k: T, v: Matrix<T>): Matrix<T> {
                    return v * k
                }

                override fun isEqual(x: Matrix<T>, y: Matrix<T>): Boolean {
                    return x.valueEquals(y)
                }

                override fun negate(x: Matrix<T>): Matrix<T> {
                    return -x
                }

                override val zero: Matrix<T>
                    get() = zero(n, model)

                override fun contains(x: Matrix<T>): Boolean {
                    return x.row == n && x.column == n
                }

                override fun add(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
                    return x + y
                }

                override fun multiply(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
                    return x matmul y
                }

                override val one: Matrix<T>
                    get() = identity(n, model)
            }
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
//    return applyAll { it.conjugate() }
//}


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


