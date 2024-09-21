package io.github.ezrnest.linear

import io.github.ezrnest.structure.Field

object LinAlg {


    /**
     * Solves the linear equation `Ax = b`.
     *
     * @return the solution to the equation, or `null` if there is no solution.
     */
    fun <T> solveLinear(A: Matrix<T>, b: Vector<T>): LinearEquationSolution<T>? {
        val model = A.model as Field<T>
        return MatrixImpl.solveLinear(A, b, model)
    }


    /**
     * Solves the homogeneous linear equation `Ax = 0`, returning the null space of `A`.
     */
    fun <T> solveHomo(A: Matrix<T>): VectorSpace<T> {
        val model = A.model as Field<T>
        return MatrixImpl.solveHomo(A, model)
    }
}