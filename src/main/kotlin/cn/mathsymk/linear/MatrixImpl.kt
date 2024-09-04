package cn.mathsymk.linear

//internal object MatrixImpl {
//
//    fun <T> copyOf(matrix: GenMatrix<T>, mc: RingCalculator<T>): MutableMatrix<T> {
//        if (matrix is MutableMatrix) {
//            return matrix.copy()
//        }
//        return AMatrix.copyOf(matrix, mc)
//    }
//
//    fun <T> copyOf(matrix: AbstractMatrix<T>): MutableMatrix<T> {
//        return copyOf(matrix, matrix.model)
//    }
//
//    internal fun <T> det(m: AbstractMatrix<T>): T {
//        m.requireSquare()
//        if (m.row == 1) {
//            return m[0, 0]
//        }
//        val mc = m.model
//        if (m.row == 2) {
//            return mc.eval {
//                m[0, 0] * m[1, 1] - m[0, 1] * m[1, 0]
//            }
//        }
//        if (m.row == 3) {
//            return mc.eval {
//                m[0, 0] * m[1, 1] * m[2, 2] +
//                        m[0, 1] * m[1, 2] * m[2, 0] +
//                        m[0, 2] * m[1, 0] * m[2, 1] -
//                        m[0, 0] * m[1, 2] * m[2, 1] -
//                        m[0, 1] * m[1, 0] * m[2, 2] -
//                        m[0, 2] * m[1, 1] * m[2, 0]
//            }
//        }
//        if (mc is FieldCalculator) {
//            return detGaussBareiss(copyOf(m), mc, mc::divide)
//        }
//        if (mc is EUDCalculator) {
//            return detGaussBareiss(copyOf(m), mc, mc::divideToInteger)
//        }
//        return detSlow(m)
//    }
//
//    //    /**
//    //     * Return det(this), this method computes the determinant of this
//    //     * matrix by the definition.
//    //     *
//    //     * @return det(this)
//    //     * @throws ArithmeticException if this Matrix is not a square matrix.
//    //     */
//    //    public T calDetDefault() {
//    //        //just calculate the value by recursion definition.
//    //
//    //    }
//    private inline fun <T> detGaussBareiss(mat: MutableMatrix<T>, mc: UnitRingCalculator<T>, division: (T, T) -> T): T {
//        //Created by lyc at 2020-03-05 19:18
//        /*
//        Refer to 'A Course in Computational Algebraic Number Theory' Algorithm 2.2.6
//
//        Explanation of the algorithm:
//        We still use the primary transformation to eliminate elements in the matrix, but here we store the potential
//        denominator and divide them only when necessary.
//        For each loop, we eliminate the size of the matrix by one, but we still use the same array and the top-left
//        element of remaining matrix is at the position (k,k).
//
//        Recall the vanilla elimination process, assuming the element at (k,k) is non-zero, we multiply a factor to
//        the first row and subtract it from i-th row. The factor is equal to m[i,k] / m[k,k]. This row transformation
//        will affect the i-th row, changing it element m[i,j] to m[i,j] - m[k,j] * m[i,k] / m[k,k]. However, since
//        we don't want to do division, we extract the denominator m[k,k] and so the resulting element is
//            m[i,j] * m[k,k] - m[k,j] * m[i,k]
//        After a loop, all element below m[k,k] are effectively zero, and the determinant of the original matrix is
//        equal to the determinant of the remaining matrix.
//
//         */
//        val n: Int = mat.row
//        var d = mc.one // the denominator that we store
//        var positive = true
//        for (k in 0 until n) {
//            //locate the top-left element used for elimination first, it must be non-zero
//            if (mc.isZero(mat[k, k])) {
//                var allZero = true
//                var i = k
//                while (i < n) {
//                    if (mc.isZero(mat[i, k])) {
//                        i++
//                        continue
//                    }
//                    allZero = false
//                    break
//                }
//                if (allZero) {
//                    return mc.zero
//                }
//                // row swap
//                mat.swapRow(i, k, k)
//                positive = !positive
//            }
//            val p: T = mat[k, k]
//            for (i in k + 1 until n) {
//                for (j in k + 1 until n) {
//                    val t = mc.eval {
//                        p * mat[i, j] - mat[i, k] * mat[k, j]
//                    }
//                    mat[i, j] = division(t, d) //
//                }
//            }
//            d = p
//        }
//        return if (positive) {
//            mat[n - 1, n - 1]
//        } else {
//            mc.negate(mat[n - 1, n - 1])
//        }
//    }
//
//    fun <T> detSlow(m: AbstractMatrix<T>): T {
//        val mc = m.model
//        var result = mc.zero
//        val n = m.row
//        for ((idx, rev) in IterUtils.permRev(n, false)) {
//            var t = m[0, idx[0]]
//            for (i in 1 until n) {
//                t = mc.eval { t * m[i, idx[i]] }
//            }
//            result = if (rev % 2 == 0) {
//                mc.add(result, t)
//            } else {
//                mc.subtract(result, t)
//            }
//        }
//        return result
//    }
//
//    internal fun <T> multiply(x: Matrix<T>, y: Matrix<T>): AMatrix<T> {
//        require(x.column == y.row) {
//            "Shape mismatch in multiplication: (${x.row},${x.column}) (${y.row},${y.column})"
//        }
//        val mc = x.model
//        val result = AMatrix.zero(x.row, y.column, mc)
//        for (i in x.rowIndices) {
//            for (j in y.colIndices) {
//                var t = mc.zero
//                for (k in x.colIndices) {
//                    t = mc.eval { t + x[i, k] * y[k, j] }
//                }
//                result.setChecked(i, j, t)
//            }
//        }
//        return result
//    }
//
//    fun <T> hadamard(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
//        val mc = x.model
//        return apply2(x, y, mc::multiply)
//    }
//
//    fun <T> kronecker(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
//        val mc = x.model
//        val r1 = x.row
//        val r2 = y.row
//        val c1 = x.column
//        val c2 = y.column
//        val result = AMatrix.zero(r1 * r2, c1 * c2, mc)
//        for (i in x.rowIndices) {
//            for (j in x.colIndices) {
//                result.setAll(i * r2, j * c2, y.multiply(x[i, j]))
//            }
//        }
//        return result
//    }
//
////    fun <T> khatriRao(x: Matrix<T>, y: Matrix<T>): Matrix<T> {
////
////    }
//
//    private inline fun <T> apply2(x: Matrix<T>, y: Matrix<T>, f: (T, T) -> T): AMatrix<T> {
//        require(x.isSameShape(y))
//        val mc = x.model
//        val result = AMatrix.zero(x.row, x.column, mc)
//        for (i in x.rowIndices) {
//            for (j in x.colIndices) {
//                result.setChecked(i, j, f(x[i, j], y[i, j]))
//            }
//        }
//        return result
//    }
//
//    internal inline fun <T, N> apply1(x: AbstractMatrix<T>, nc: RingCalculator<N>, f: (T) -> N)
//            : AMatrix<N> {
//        val result = AMatrix.zero(x.row, x.column, nc)
//        for (i in x.rowIndices) {
//            for (j in x.colIndices) {
//                result.setChecked(i, j, f(x[i, j]))
//            }
//        }
//        return result
//    }
//
//
//    internal fun <T> add(x: Matrix<T>, y: Matrix<T>): AMatrix<T> {
//        return apply2(x, y, x.model::add)
//    }
//
//    internal fun <T> subtract(x: Matrix<T>, y: Matrix<T>): AMatrix<T> {
//        return apply2(x, y, x.model::subtract)
//    }
//
//    internal fun <T> negate(x: Matrix<T>): AMatrix<T> {
//        return apply1(x, x.model, x.model::negate)
//    }
//
//
//    internal fun <T> multiply(x: Matrix<T>, k: T): AMatrix<T> {
//        val mc = x.model
//        return apply1(x, mc) {
//            mc.multiply(k, it)
//        }
//    }
//
//    internal fun <T> divide(x: Matrix<T>, k: T): AMatrix<T> {
//        val mc = x.model as UnitRingCalculator
//        return apply1(x, mc) {
//            mc.exactDivide(k, it)
//        }
//    }
//
//
//    /**
//     *
//     * @return a list of strictly increasing pivots of the column. The size of it is equal to the rank of the matrix.
//     */
//    internal fun <T> toUpperTriangle(
//        M: MutableMatrix<T>,
//        operations: MutableList<MatrixOperation<T>>? = null,
//        column: Int = M.column
//    ): List<Int> {
//        //Created by lyc at 2021-04-29
//        val mc = M.model as FieldCalculator
//        val row = M.row
//        var i = 0
//        val pivots = ArrayList<Int>(min(M.row, column))
//        /*
//        j = pivots[i] then M[i,j] is the first non-zero element in that row
//         */
//        for (j in 0 until column) {
//            if (i >= row) {
//                break
//            }
//            var f: T? = null
//            for (i2 in i until row) {
//                if (mc.isZero(M[i2, j])) {
//                    continue
//                }
//                f = M[i2, j]
//                if (i2 != i) {
//                    M.swapRow(i2, i)
//                    operations?.add(MatrixOperation.exchangeRow(i2, i))
//                }
//                break
//            }
//            if (f == null) {
//                //not found
//                continue
//            }
//            for (i2 in (i + 1) until row) {
//                if (mc.isZero(M[i2, j])) {
//                    continue
//                }
//                val k = mc.eval { -M[i2, j] / f }
//                M[i2, j] = mc.zero
//                M.multiplyAddRow(i, i2, k, j + 1)
//                operations?.add(MatrixOperation.multiplyAddRow(i, i2, k))
//            }
//            pivots += j
//            i++
//        }
//        return pivots
//    }
//
//
//    fun <T> rank(matrix: AbstractMatrix<T>): Int {
//        val copy = copyOf(matrix)
//        return toUpperTriangle(copy).size
//    }
//
//    /**
//     *
//     * @return a list of strictly increasing pivots of the column. The size of it is equal to the rank of the matrix.
//     */
//    internal fun <T> toEchelon(
//        M: MutableMatrix<T>,
//        column: Int = M.column,
//        operations: MutableList<MatrixOperation<T>>? = null
//    ): List<Int> {
//        //Created by lyc at 2021-04-29
//        val pivots = toUpperTriangle(M, operations, column)
//        val mc = M.model as FieldCalculator
//        for (i in pivots.lastIndex downTo 0) {
//            val j = pivots[i]
//            if (!mc.isEqual(M[i, j], mc.one)) {
//                M.divideRow(i, M[i, j], j + 1)
//                M[i, j] = mc.one
//                operations?.add(MatrixOperation.multiplyRow(i, mc.reciprocal(M[i, j])))
//            }
//            for (k in (i - 1) downTo 0) {
//                if (mc.isZero(M[k, j])) {
//                    continue
//                }
//                val q = mc.eval { -M[k, j] }
//                M.multiplyAddRow(i, k, q, j + 1)
//                M[k, j] = mc.zero
//                operations?.add(MatrixOperation.multiplyAddRow(i, k, q))
//            }
//        }
//        return pivots
//    }
//
//    internal fun <T> nullSpaceGenerator(matrix: MutableMatrix<T>, column: Int, pivots: List<Int>): List<Vector<T>> {
//        val r = pivots.size
//        val dim = column
//        val k = dim - r
//        if (k == 0) {
//            return emptyList()
//        }
//        val mc = matrix.model as UnitRingCalculator
//        val vectors = ArrayList<Vector<T>>(k)
//        val negativeOne = mc.negate(mc.one)
//        fun makeVector(j: Int) {
//            val v = Vector.zero(dim, mc)
//            v[j] = negativeOne
//            for (i in pivots.indices) {
//                v[pivots[i]] = matrix[i, j]
//            }
//            vectors += v
//        }
//
//        var l = 0
//        for (j in 0 until pivots.last()) {
//            if (j < pivots[l]) {
//                makeVector(j)
//            } else {
//                l++
//            }
//        }
//        for (j in (pivots.last() + 1) until column) {
//            makeVector(j)
//        }
//        return vectors
//    }
//
//    internal fun <T> nullSpaceOf(expanded: MutableMatrix<T>, column: Int, pivots: List<Int>): VectorBasis<T> {
//        val generators = nullSpaceGenerator(expanded, column, pivots)
//        val mc = expanded.model as FieldCalculator
//        if (generators.isEmpty()) {
//            return VectorBasis.zero(column, mc)
//        }
//        return VectorBasis.createBaseWithoutCheck(generators)
//    }
//
//    fun <T> specialSolutionOf(expanded: MutableMatrix<T>, column: Int, pivots: List<Int>): Matrix<T> {
//        val mc = expanded.model
//        val special = Matrix.zero(column, expanded.column - column, mc)
//        for (k in pivots.indices) {
//            val pk = pivots[k]
//            for (j in special.colIndices) {
//                special[pk, j] = expanded[k, j + column]
//            }
//        }
//        return special
//    }
//
//    fun <T> solveLinear(expanded: MutableMatrix<T>, colSep: Int): Triple<Matrix<T>, VectorBasis<T>, Boolean> {
//        val pivots = toEchelon(expanded, colSep, null)
//        val r = pivots.size
//        val mc = expanded.model
//        val special = specialSolutionOf(expanded, colSep, pivots)
//        val basis = nullSpaceOf(expanded, colSep, pivots)
//        val solvable = (r until expanded.row).all { i ->
//            (colSep until expanded.column).all { j -> mc.isZero(expanded[i, j]) }
//        }
//        return Triple(special, basis, solvable)
//    }
//
//    fun <T> solveLinear(m: AbstractMatrix<T>, b: AbstractMatrix<T>): Triple<Matrix<T>, VectorBasis<T>, Boolean> {
//        require(m.row == b.row)
//        val expanded = Matrix.concatColumn(m, b)
//        return solveLinear(expanded, m.column)
//    }
//
//    fun <T> solveLinear(m: AbstractMatrix<T>, b: AbstractVector<T>): Triple<Vector<T>, VectorBasis<T>, Boolean> {
//        require(m.row == b.size)
//        val expanded = AMatrix.zero(m.row, m.column + 1, m.model)
//        val col = m.column
//        expanded.setAll(0, 0, m)
//        for (i in b.indices) {
//            expanded[i, col] = b[i]
//        }
//        val (special, basis, sol) = solveLinear(expanded, col)
//        val v = special.getColumn(0)
//        return Triple(v, basis, sol)
//    }
//
//    fun <T> solveHomo(m: AbstractMatrix<T>): VectorBasis<T> {
//        val expanded = Matrix.copyOf(m)
//        val pivots = toEchelon(expanded)
//        return nullSpaceOf(expanded, m.column, pivots)
//    }
//
//    private fun <T> inverseInField(m: AbstractMatrix<T>): Matrix<T> {
//        val n = m.row
//        val mc = m.model as FieldCalculator
//        val expanded = AMatrix.zero(n, 2 * n, m.model)
//        expanded.setAll(0, 0, m)
//        for (i in 0 until n) {
//            expanded[i, i + n] = mc.one
//        }
//        val pivots = toEchelon(expanded, column = n)
//        if (pivots.size != n) {
//            ExceptionUtil.notInvertible()
//        }
//        return expanded.subMatrix(0, n, n, 2 * n)
//    }
//
//    fun <T> inverse(m: AbstractMatrix<T>): Matrix<T> {
//        require(m.isSquare())
//        val mc = m.model
//        if (mc is FieldCalculator) {
//            return inverseInField(m)
//        }
//        if (mc is EUDCalculator) {
//            return MatrixUtils.inverseInEUD(m)
//        }
//        return MatrixUtils.inverseInRing(m)
//    }
//
//    fun <T> decompRank(x: Matrix<T>): Pair<Matrix<T>, Matrix<T>> {
//        val m = Matrix.copyOf(x)
//        val pivots = toEchelon(m)
//        val a = Matrix.fromVectors(pivots.map { x.getColumn(it) })
//        val b = m.subMatrix(0, 0, pivots.size, m.column)
//        return a to b
//    }
//
//
//    fun <T> charMatrix(m: AbstractMatrix<T>, pc: RingCalculator<Polynomial<T>>): Matrix<Polynomial<T>> {
//        val mc = m.model as FieldCalculator
//        m.requireSquare()
//        val n = m.row
//        val result = Matrix.zero(n, n, pc)
//        for (i in 0 until n) {
//            for (j in 0 until n) {
//                result[i, j] = if (i == j) {
//                    Polynomial.ofRoot(mc, m[i, j])
//                } else {
//                    Polynomial.constant(mc, mc.negate(m[i, j]))
//                }
//            }
//        }
//        return result
//    }
//
//    fun <T> columnSpace(A: AbstractMatrix<T>): VectorBasis<T> {
//        val matrix = A.toMutable()
//        val pivots = toUpperTriangle(matrix)
//        return VectorBasis.createBaseWithoutCheck(pivots.map { A.getColumn(it) })
//    }
//
//    fun <T> adjointOf(matrix: AbstractMatrix<T>): Matrix<T> {
//        matrix.requireSquare()
//        if (matrix.size == 1) {
//            return Matrix.identity(1, matrix.model as UnitRingCalculator<T>)
//        }
//        val mc = matrix.model
//        if (mc is UnitRingCalculator) {
//            try {
//                return adjointAndCharPoly(matrix, mc).first
//            } catch (ignore: ArithmeticException) {
//            }
//        }
//
//        val n = matrix.row
//        return Matrix(n, n, mc) { i, j ->
//            val cof = matrix.cofactor(j, i)
//            val d = cof.det()
//            if ((i + j) % 2 == 0) {
//                d
//            } else {
//                mc.negate(d)
//            }
//        }
//    }
//
//    fun <T> charPolyOf(matrix: AbstractMatrix<T>): Polynomial<T> {
//        matrix.requireSquare()
//        val mc = matrix.model
//        if (mc is UnitRingCalculator) {
//            try {
//                return adjointAndCharPoly(matrix, mc).second
//            } catch (ignore: ArithmeticException) {
//            }
//        }
//        val ch = charMatrix(matrix, Polynomial.calculatorRing(matrix.model))
//        return ch.det() //slow det
//    }
//
//    fun <T> adjointAndCharPoly(matrix: AbstractMatrix<T>, mc: UnitRingCalculator<T>): Pair<Matrix<T>, Polynomial<T>> {
//        /*
//        Reference: A course in computational algebraic number theory, Algorithm 2.2.7
//
//         */
//        val M = matrix.asMatrix()
//        val n = M.row
//        var C = Matrix.identity(n, mc)
//        val a = ArrayList<T>(n + 1)
//        a += mc.one
//        for (i in 1 until n) {
//            C = multiply(M, C)
//            val ai = mc.eval { exactDivide(-C.trace(), of(i.toLong())) }
//            for (j in 0 until n) {
//                mc.eval { C[j, j] += ai }
//            }
//            a += ai
//        }
//        a += mc.eval { -exactDivide((M * C).trace(), of(n.toLong())) }
//        val p = Polynomial.of(mc, a.asReversed())
//        if (n % 2 == 0) {
//            C.negateInplace()
//        }
//        return C to p
//    }
//
//    fun <T> toHessenberg(matrix: AbstractMatrix<T>): Matrix<T> {
//        require(matrix.isSquare())
//        val H = matrix.toMutable()
//        val n = matrix.row
//        val mc = matrix.model as FieldCalculator
//
//        for (m in 0 until (n - 1)) {
//            println(H)
//            var i0 = m + 2
//            while (i0 < n) {
//                if (!mc.isZero(H[i0, m])) {
//                    break
//                }
//                i0++
//            }
//            if (i0 >= n) {
//                continue
//            }
//            if (!mc.isZero(H[m + 1, m])) {
//                i0 = m + 1
//            }
////            val t = H[i, m]
//            if (i0 > m + 1) {
//                H.swapRow(i0, m + 1, m)
//                H.swapCol(i0, m + 1)
//            }
//            val t = H[m + 1, m]
//            println(H)
//            for (i in (m + 2) until n) {
//                if (mc.isZero(H[i, m])) {
//                    continue
//                }
//                val u = mc.eval { H[i, m] / t }
//                H.multiplyAddRow(m + 1, i, mc.negate(u), m)
//                H[i, m] = mc.zero
//                H.multiplyAddCol(i, m + 1, mc.reciprocal(u))
//            }
//        }
//        return H
//
//    }
//
//
//}
