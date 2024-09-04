package cn.mathsymk.linear

import cn.mathsymk.IMathObject
import cn.mathsymk.MathObject
import cn.mathsymk.model.struct.*
import cn.mathsymk.structure.*
import java.util.function.Function

interface Matrix<T : Any> : GenMatrix<T>, MathObject<T, EqualPredicate<T>>, AlgebraModel<T, Matrix<T>> {

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

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Matrix<N> {
        return MatrixImpl.apply1(this, newCalculator, mapper::apply)
    }


    override fun valueEquals(obj: IMathObject<T>): Boolean {
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

    override fun times(k: T): Matrix<T> {
        return MatrixImpl.multiply(this, k, model as MulSemigroup)
    }

    override fun div(k: T): Matrix<T> {
        return MatrixImpl.divide(this, k, model as MulGroup)
    }


    /*
    Matrix operations
     */

    /**
     * Returns the matrix product of this matrix and the given matrix.
     */
    override operator fun times(y: Matrix<T>): Matrix<T> {
        return MatrixImpl.matmul(this, y, model as Ring)
    }


    infix fun matmul(y: Matrix<T>): Matrix<T> {
        return this * y
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
        TODO()
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


    }
}

interface MutableMatrix<T : Any> : Matrix<T> {
    operator fun set(i: Int, j: Int, value: T)

    fun setRow(i: Int, row: Vector<T>){
        for (j in 0 ..< column){
            this[i, j] = row[j]
        }
    }

    fun setCol(j: Int, col: Vector<T>){
        for(i in 0 ..< row){
            this[i, j] = col[i]
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
}