package cn.mathsymk.linear

import cn.mathsymk.model.struct.GenMatrix
import cn.mathsymk.model.struct.colIndices
import cn.mathsymk.model.struct.rowIndices

interface Matrix<T:Any> : GenMatrix<T>{

    override fun applyAll(f: (T) -> T): Matrix<T> {
        TODO("Not yet implemented")
    }

    fun rowAt(row : Int): Vector<T>{
        TODO()
    }

    fun colAt(col : Int): Vector<T>{
        TODO()
    }

    fun rowVectors(): List<Vector<T>>{
        return rowIndices.map { rowAt(it) }
    }

    fun colVectors(): List<Vector<T>>{
        return colIndices.map { colAt(it) }
    }



    fun det() : T{
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
        TODO()
    }

    fun transpose(): Matrix<T>{
        TODO()
    }

    val T: Matrix<T>
        get() = transpose()

    /**
     * Returns the factor of this matrix as an immutable view.
     */
    fun factor(rows : IntArray, cols : IntArray): Matrix<T>{
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
    fun subMatrix(rowStart : Int, colStart : Int, rowEnd : Int, colEnd : Int): Matrix<T>{
        TODO()
    }

    fun cofactor(row : Int, col : Int): Matrix<T>{
        return cofactor(intArrayOf(row), intArrayOf(col))
    }

    fun cofactor(rows : IntArray, cols : IntArray): Matrix<T>{
        TODO()
    }

    fun adjugate() : Matrix<T>{
        TODO()
    }
}