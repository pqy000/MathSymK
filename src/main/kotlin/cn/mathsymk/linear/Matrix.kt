package cn.mathsymk.linear

import cn.mathsymk.ValueEquatable
import cn.mathsymk.ModeledMathObject
import cn.mathsymk.model.struct.*
import cn.mathsymk.structure.*
import java.util.function.Function

interface Matrix<T> : GenMatrix<T>, ModeledMathObject<T, EqualPredicate<T>>, AlgebraModel<T, Matrix<T>> {

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


    fun det(): T {
        TODO()
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
        TODO()
    }

    fun diag(): Vector<T> {
        TODO()
    }

    /**
     * Returns the sum of all elements in this matrix.
     */
    fun sumAll(): T {
        val model = model as AddSemigroup
        return elementSequence().reduce { acc, t -> model.add(acc, t) }
    }

    fun transpose(): Matrix<T> {
        return TransposedMatrixView(this)
    }

    val T: Matrix<T>
        get() = transpose()

    /**
     * Returns the factor of this matrix as an immutable view.
     */
    fun factor(rows: IntArray, cols: IntArray): Matrix<T> {
        TODO()
    }

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
        return SubMatrixView(this, rowStart, colStart, rowEnd, colEnd)
    }

    /**
     *
     */
    fun slice(rows: IntArray, cols: IntArray): Matrix<T> {
        return SlicedMatrixView(this, rows, cols)
    }

    fun cofactor(row: Int, col: Int): Matrix<T> {
        return cofactor(intArrayOf(row), intArrayOf(col))
    }

    fun cofactor(rows: IntArray, cols: IntArray): Matrix<T> {
        TODO()
    }

    fun adjugate(): Matrix<T> {
        TODO()
    }

    companion object {
        operator fun <T> invoke(row: Int, column: Int, model: EqualPredicate<T>, init: (Int, Int) -> T): Matrix<T> {
            return AMatrix.of(row, column, model, init)
        }

        operator fun <T> invoke(n : Int, model: EqualPredicate<T>, init: (Int, Int) -> T): Matrix<T> {
            return invoke(n, n, model, init)
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
//            val expanded = MutableMatrix.zero(a.row, a.column + b.column, a.model)
//            val col = a.column
//            expanded.setAll(0, 0, a)
//            expanded.setAll(0, col, b)
//            return expanded
        }

        fun <T> zero(row: Int, column: Int, model: AddMonoid<T>): Matrix<T> {
            return MatrixImpl.zero(row, column, model)
        }

        fun <T> zero(n : Int, model: AddMonoid<T>): Matrix<T> {
            return zero(n, n, model)
        }

        fun <T> diag(elements : List<T>, model : AddMonoid<T>): Matrix<T> {
            val n = elements.size
            val zero = MatrixImpl.zero(n,n, model)
            for (i in 0 until n) {
                zero[i,i] = elements[i]
            }
            return zero
        }

        fun <T> diag(model : AddMonoid<T>, vararg elements : T): Matrix<T> {
            return diag(elements.asList(), model)
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
        for (i in 0 ..< matrix.row) {
            for (j in 0 ..< matrix.column) {
                this[i + row, j + col] = matrix[i, j]
            }
        }
    }

    fun copy(): MutableMatrix<T>

    fun swapRow(r1: Int, r2: Int, colStart: Int = 0, colEnd: Int = column)
    fun swapCol(c1: Int, c2: Int, rowStart: Int = 0, rowEnd: Int = row)

    fun negateRow(r: Int, colStart: Int = 0, colEnd: Int = column)
    fun negateCol(c: Int, rowStart: Int = 0, rowEnd: Int = row)

    fun multiplyRow(r: Int, k: T, colStart: Int = 0, colEnd: Int = column)
    fun divideRow(r: Int, k: T, colStart: Int = 0, colEnd: Int = column)

    fun multiplyCol(c: Int, k: T, rowStart: Int = 0, rowEnd: Int = row)
    fun divideCol(c: Int, k: T, rowStart: Int = 0, rowEnd: Int = row)


    fun multiplyAddRow(r1: Int, r2: Int, k: T, colStart: Int = 0, colEnd: Int = column)
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
            TODO()
        }
    }
}


