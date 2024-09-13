package cn.mathsymk.linear

import cn.mathsymk.linear.MatrixUtils.decompQR
import cn.mathsymk.model.Polynomial
import cn.mathsymk.structure.*

object MatrixUtils {

    /**
     * Returns the characteristic polynomial of the given square matrix.
     * The characteristic polynomial is defined as `f(λ) = det(λI-A)`.
     *
     *
     * The characteristic polynomial is a polynomial of degree `n` where `n` is the row count (= column count) of the matrix.
     * The leading coefficient is `1`, and the constant term is the determinant of the matrix.
     *
     * The roots of the characteristic polynomial are defined to be the eigenvalues of the matrix.
     *
     * It is required that the `this.model` is actually a [UnitRing].
     *
     * @see [Matrix.det]
     *
     */
    fun <T> Matrix<T>.charPoly(): Polynomial<T> {
        return MatrixImpl.charPoly(this, this.model as UnitRing<T>)
    }

    /**
     * Returns the rank decomposition of a matrix `A = LR`, where `L` is a column full-rank matrix and `R` is a row full-rank matrix.
     *
     * Let `A` be a matrix of shape `(n,m)` and rank `r`.
     * Then, `L` is of shape `(n,r)` and `R` is of shape `(r,m)` with `rank(L) = rank(R) = r`.
     *
     *
     * @return a pair of `(L, R)`.
     */
    fun <T> Matrix<T>.decompRank(): Pair<Matrix<T>, Matrix<T>> {
        return MatrixImpl.decompRank(this, this.model as Field)
    }

    /**
     * Returns the LU decomposition of this matrix: `A = LU`.
     *
     * The LU decomposition is defined as `A = LU` where `L` is a lower triangular matrix and `U` is an upper triangular matrix.
     *
     * It is required that the `model` of this matrix is a [Field].
     *
     * @return a pair of `(L, U)`.
     */
    fun <T> Matrix<T>.decompLU() : Pair<Matrix<T>, Matrix<T>> {
        return MatrixImpl.decompLU(this, this.model as Field<T>)
    }

    /**
     * Returns the Cholesky decomposition of this matrix: `A = L L.T`, where `L` is a lower triangular matrix.
     *
     * It is required that the `model` of this matrix is a [Reals].
     */
    fun <T> Matrix<T>.decompCholesky() : Matrix<T> {
        return MatrixImpl.decompCholesky(this, this.model as Reals<T>)
    }

    /**
     * Returns the Cholesky decomposition of this matrix: `A = L D L.T`, where `L` is a lower triangular matrix and `D` is a diagonal matrix.
     *
     * It is required that the `model` of this matrix is a [Field].
     *
     * @return a pair of `(L, diag(D))`.
     */
    fun <T> Matrix<T>.decompCholeskyD() : Pair<Matrix<T>, Vector<T>> {
        return MatrixImpl.decompCholeskyD(this, this.model as Field<T>)
    }

    /**
     * Returns the QR decomposition of this matrix: `A = QR`, where `Q` is an orthogonal matrix and `R` is an upper triangular matrix.
     *
     * It is required that the `model` of this matrix is a [Reals].
     *
     * @return a pair of `(Q, R)`.
     */
    fun <T> Matrix<T>.decompQR() : Pair<Matrix<T>, Matrix<T>> {
        return MatrixImpl.decompQR(this, this.model as Reals)
    }

    /**
     * Returns the KAN decomposition (also known as Iwasawa decomposition) of this square matrix.
     *
     * Here, `M = K * A * N`:
     * * `K` is an orthogonal matrix,
     * * `A` is a diagonal matrix,
     * * `N` is an upper triangular matrix.
     *
     *
     * The KAN decomposition comes from factorization for Lie groups, particularly semisimple Lie groups.
     * The abbreviation KAN comes from:
     * * `K` for maximal compact subgroup;
     * * `A` for abelian subgroup;
     * * `N` for nilpotent subgroup.
     */
    fun <T> Matrix<T>.decompKAN() :Triple<Matrix<T>, Vector<T>, Matrix<T>> {
        return MatrixImpl.decompKAN(this, this.model as Reals<T>)
    }
    /**
     * Returns the (row) echelon form of this matrix.
     *
     * The row echelon form of a matrix is a matrix in which
     * * all zero rows are at the bottom of the matrix;
     * * the leading coefficient of each row is to the right of the leading coefficient of the row above it;
     * * all entries in the column below a leading coefficient are zeros.
     *
     *
     * An example of a row echelon form of a 4x4 matrix `A` is like
     *
     *    [1 2 3 4]
     *    [0 0 1 2]
     *    [0 0 0 1]
     *    [0 0 0 0]
     *
     *
     *
     * It is required that the `model` of this matrix is a [Field].
     */
    fun <T> Matrix<T>.toEchelonForm() : Matrix<T> {
        val m = MutableMatrix.copyOf(this)
        MatrixImpl.toEchelon(m, this.model as Field<T>)
        return m
    }

    /**
     * Returns the congruence diagonal normal form `J` of this matrix `A` and the corresponding transformation `P`,
     * which satisfies
     *
     *     P.T * A * P = J
     *
     * @return `(J, P)`.
     */
    fun <T> Matrix<T>.toCongDiagForm(): Pair<Matrix<T>, Matrix<T>> {
        return MatrixImpl.toCongDiagonalForm(this, this.model as Field<T>)
    }

    /**
     * Transform this matrix to Hermit normal form (HNF).
     * The HNF of a matrix `A` is a matrix `M` such that
     * * `M` is a row-echelon matrix, meaning all non-zero rows are above any rows of all zeros;
     * * for each leading non-zero entry (called a pivot) in a row, all entries below it in the same column are zero;
     * * the pivot elements are positive integers;
     * * all entries in the pivot columns above the pivot are non-negative integers that are less than the pivot.
     *
     *
     * For example, the HNF of a 4x4 matrix `A` is like
     *
     *    [1 2 3 4]
     *    [0 5 6 7]
     *    [0 0 8 0]
     *    [0 0 0 9]
     *
     *
     * It is required that the `model` of this matrix is an [Integers].
     */
    fun <T> Matrix<T>.toHermitForm(): Matrix<T> {
        return MatrixImpl.toHermitForm(this, this.model as Integers<T>)
    }

    /**
     * Transforms this matrix to Smith normal form, a diagonal matrix with the following property:
     *
     *     m[i,i] | m[i+1,i+1]  for i <= r,
     *     m[i,i] = 0, for i > r
     *
     *
     * It is required that the `model` of this matrix is an [EuclideanDomain].
     *
     * For example, the Smith normal form of matrix `[[1 2 3][4 5 6][7 8 9]]` can be
     * `diag(1,3,0)`
     */
    fun <T> Matrix<T>.toSmithForm(): Matrix<T> {
        //Created by lyc at 2020-03-10 14:54
        TODO()
//        return MatrixImpl.toSmithForm(this, this.model as EuclideanDomain<T>)
    }

    /**
     * Transforms this matrix to (upper) Hessenberg form.
     *
     * The Hessenberg form of a matrix `A` is a matrix `H` such that all elements below the first subdiagonal are zero.
     * For example, the Hessenberg form of a 4x4 matrix `A` is like
     *
     *    [a b c d]
     *    [e f g h]
     *    [0 i j k]
     *    [0 0 l m]
     *
     *
     */
    fun <T> Matrix<T>.toHessenbergForm(): Matrix<T> {
        return MatrixImpl.toHessenberg(this, this.model as Field<T>)
    }
}