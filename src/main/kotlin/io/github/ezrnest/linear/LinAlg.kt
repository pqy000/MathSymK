package io.github.ezrnest.linear

import io.github.ezrnest.structure.Field
import io.github.ezrnest.structure.Integers


///**
// * Transform this matrix to Hermit normal form (HNF).
// * The HNF of a matrix `A` is a matrix `M` such that
// * * `M` is a row-echelon matrix, meaning all non-zero rows are above any rows of all zeros;
// * * for each leading non-zero entry (called a pivot) in a row, all entries below it in the same column are zero;
// * * the pivot elements are positive integers;
// * * all entries in the pivot columns above the pivot are non-negative integers that are less than the pivot.
// *
// *
// * For example, the HNF of a 4x4 matrix `A` is like
// *
// *    [1 2 3 4]
// *    [0 5 6 7]
// *    [0 0 8 0]
// *    [0 0 0 9]
// *
// *
// * It is required that the `model` of this matrix is an [Integers].
// */
//fun Matrix<T>.toHermitForm(): Matrix<T> {
//    return MatrixImpl.toHermitForm(this, model as Integers<T>)
//}

/**
 * Provides linear algebra functionalities.
 */
object LinAlg {
    // Created at 2024/9/21
    /**
     * Solves the linear equation `Ax = b`.
     *
     * @return the solution to the equation, or `null` if there is no solution.
     */
    fun <T> solveLinear(A: Matrix<T>, b: Vector<T>, model : Field<T>): LinearEquationSolution<T>? {
        return MatrixImpl.solveLinear(A, b, model)
    }

    /**
     * Solves the matrix linear equation `AX = B`.
     */
    fun <T> solveLinear(A : Matrix<T>, B : Matrix<T>, model : Field<T>) : Pair<Matrix<T>, VectorSpace<T>>?{
        return MatrixImpl.solveLinear(A, B, model)
    }


    /**
     * Solves the homogeneous linear equation `Ax = 0`, returning the null space of `A`.
     */
    fun <T> solveHomo(A: Matrix<T>, model : Field<T>): VectorSpace<T> {
        return MatrixImpl.solveHomo(A, model)
    }
}
