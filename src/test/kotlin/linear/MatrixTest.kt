package linear

import TestUtils.assertEquals
import TestUtils.assertValueEquals
import io.github.ezrnest.linear.*
import io.github.ezrnest.linear.MatrixExt.charPoly
import io.github.ezrnest.linear.MatrixExt.decompLDL
import io.github.ezrnest.linear.MatrixExt.decompLU
import io.github.ezrnest.linear.MatrixExt.decompRank
import io.github.ezrnest.linear.MatrixExt.image
import io.github.ezrnest.linear.MatrixExt.kernel
import io.github.ezrnest.linear.MatrixExt.toCongDiagForm
import io.github.ezrnest.model.Multinomial
import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.model.Polynomial
import io.github.ezrnest.numberTheory.NTFunctions
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.MathUtils
import io.github.ezrnest.util.pow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatrixTest {
    val Z = NumberModels.integers()
    val Zmod7 = NumberModels.intModP(7)
    val Zmod97 = NumberModels.intModP(97)

    @Test
    fun testDetGB() {
        val mults = Multinomial.over(Z)
        (1..3).forEach { n ->
            val A = Matrix(n, mults) { i, j -> mults.monomial("($i$j)") } // 3x3 matrix with variables (ij)
            val detGB = MatrixImpl.detGaussBareiss(A.toMutable(), mults)
            mults.assertEquals(MatrixImpl.detDefinition(A, mults), detGB)
            mults.assertEquals(MatrixImpl.detSmall(A, mults), detGB)
        }
        (4..5).forEach { n ->
            val A = Matrix(n, mults) { i, j -> mults.monomial("($i$j)") } // 4x4 matrix with variables (ij)
            mults.assertEquals(MatrixImpl.detDefinition(A, mults), MatrixImpl.detGaussBareiss(A.toMutable(), mults))
        }
    }

    @Test
    fun scalarDivisionOfMatrix() {
        val A = Matrix(2, 2, Zmod7) { i, j -> (i + j) * 2 }
        val scalar = 2
        val expected = Matrix(2, 2, Zmod7) { i, j -> i + j }
        assertValueEquals(expected, A / scalar)
    }

    @Test
    fun additionOfMatricesWithDifferentDimensions() {
        val A = Matrix(2, 2, Z) { i, j -> i + j }
        val B = Matrix(3, 3, Z) { i, j -> (i + 1) * (j + 1) }
        assertThrows<IllegalArgumentException> { A + B }
    }

    @Test
    fun subtractionOfMatricesWithDifferentDimensions() {
        val A = Matrix(2, 2, Z) { i, j -> i + j }
        val B = Matrix(3, 3, Z) { i, j -> (i + 1) * (j + 1) }
        assertThrows<IllegalArgumentException> { A - B }
    }

    @Test
    fun multiplicationOfMatricesWithIncompatibleDimensions() {
        val A = Matrix(2, 3, Z) { i, j -> i + 1 }
        val B = Matrix(2, 2, Z) { i, j -> j + 1 }
        assertThrows<IllegalArgumentException> { A * B }
    }

    @Test
    fun inverseOfIdentityMatrix() {
        val identity = Matrix.identity(3, Zmod7)
        val expected = Matrix.identity(3, Zmod7)
        assertValueEquals(expected, identity.inv())
    }

    @Test
    fun inverseOfNonSingularMatrix() {
        val A = Matrix(2, Zmod7) { i, j -> if (i == j) 1 else 2 }
        assertValueEquals(Matrix.identity(2, Zmod7), A * A.inv())
        assertValueEquals(Matrix.identity(2, Zmod7), A.inv() * A)
    }

    @Test
    fun inverseOfSingularMatrixThrowsException() {
        val singular = Matrix(2, 2, Zmod7) { _, _ -> 1 }
        assertThrows<ArithmeticException> { singular.inv() }
    }

    @Test
    fun inverseOfNonSquareMatrixThrowsException() {
        val nonSquare = Matrix(2, 3, Zmod7) { i, j -> i + j }
        assertThrows<IllegalArgumentException> { nonSquare.inv() }
    }

    @Test
    fun testMatrixCharPoly() {
        val ℤ = NumberModels.integers()
        val n = 4
        val A = Matrix(n, ℤ) { i, j -> i + 2 * j }
        val p = A.charPoly() // the characteristic polynomial of A, p(λ) = det(λI - A)
        ℤ.assertEquals(A.trace(), -p[n - 1])
        ℤ.assertEquals(A.det(), (-1).pow(n) * p[0])

        // another way to compute the characteristic polynomial
        // sum of all principal minors of A
        run {
            val coef = (0..n).map { k ->
                if (k == n) return@map ℤ.one
                var res = ℤ.zero
                for (rows in IterUtils.comb(A.row, n - k, false)) {
                    val major = A.slice(rows, rows).det()
                    res += major
                }
                res * MathUtils.powOfMinusOne(k)
            }
            val p2 = Polynomial.fromList(ℤ, coef)
            assertValueEquals(p, p2)
        }

        val matOverZ = Matrix.over(n, ℤ)
        assertTrue(p.substitute(A, matOverZ).isZero) // p(A) = 0, a matrix of zeros
    }

    @Test
    fun testDet() {
        val mult = Multinomial.over(Z)
        val A = Matrix(3, 3, mult) { i, j ->
            mult.monomial("($i$j)")
        }
        val det3 = MatrixImpl.detSmall(A, mult)
        val detGB = MatrixImpl.detGaussBareiss(A, mult)
        val detDef = MatrixImpl.detDefinition(A, mult)
        mult.assertEquals(det3, detGB)
        mult.assertEquals(det3, detDef)
    }

    @Test
    fun testInvariantFactorsOverIntegers() {
        // 3x3 matrix over integers
        val Z = NumberModels.integers()
        val n = 4
//    val A = Matrix(n, Z) { i, j -> (i + 1) * (j + 2) }
        for (seed in 10..12) {
            val rng = Random(seed)
            val A = Matrix(n, Z) { i, j ->
                rng.nextInt(10)
            }
            val invFactors = MatrixImpl.invariantFactors(A, Z)
            val accProd = invFactors.scan(1) { acc, factor -> acc * factor }.drop(1)
            val gcds = mutableListOf<Int>()
            for (k in 1..n) {
                val rows = IterUtils.comb(A.row, k, false)
                val cols = IterUtils.comb(A.column, k, false)
                val minors =
                    IterUtils.prod2(rows, cols).map { A.slice(it.first, it.second).det() }.toList().toIntArray()
                val gcd = NTFunctions.gcd(*minors)
                if (gcd == 0) {
                    break
                }
                gcds.add(gcd)
            }
            assertEquals(gcds, accProd)
        }
    }

    @Test
    fun testSolveLinear() {
        val F = Zmod97
        run {
            val A = Matrix.zero(3, 5, F)
            val b = Vector.unitVector(3, 0, F)
            val sol = LinAlg.solveLinear(A, b)
            assertTrue(sol == null)
        }
        run {
            val rng = Random(10)
            val A = Matrix(4, 4, F) { i, j -> rng.nextInt(100) }
            val b = Vector(4) { i -> rng.nextInt(100) }
            val sol = LinAlg.solveLinear(A, b)!! // A is full rank
            assertEquals(A * sol.solution, b)
        }

        run {
            val rng = Random(10)
            val A = Matrix(4, 4, F) { i, j -> rng.nextInt(100) }
            val sol = LinAlg.solveHomo(A)
            assertEquals(0, sol.dim) // A is full rank
        }
    }

    @Test
    fun columnSpaceOfZeroMatrix() {
        val A = Matrix.zero(3, 3, Zmod7)
        val columnSpace = MatrixImpl.columnSpace(A, Zmod97)
        assertEquals(0, columnSpace.dim)
    }

    @Test
    fun columnSpaceOfFullRank() {
        val A = Matrix.identity(3, Zmod7)
        val columnSpace = MatrixImpl.columnSpace(A, Zmod97)
        assertEquals(3, columnSpace.dim)
    }


    @Test
    fun spaceDecomposition() {
        val rng = Random(10)
        repeat(3) {
            val n = 5
            val A = Matrix(n, Zmod97) { _, _ -> rng.nextInt(97) }
            val kernel = A.kernel()
            val image = A.image()
            assertEquals(n, kernel.dim + image.dim)
        }
    }

    @Test
    fun decompCholeskyDOfPositiveDefiniteMatrix() {
        val F = Zmod7
        val A = Matrix(3, 3, F) { i, j -> if (i == j) 4 else 1 }
        val (L, D) = A.decompLDL()
        assertValueEquals(A, L * Matrix.diag(D) * L.T)
    }

    @Test
    fun testCholeskyDRandom() {
        val rng = Random(10)
        val A = Matrix(4, 4, Zmod97) { i, j -> rng.nextInt(100) }.let { it * it.T }
        val (L, D) = A.decompLDL()
        println(L)
        println(D)
        assertValueEquals(A, L * Matrix.diag(D) * L.T)
    }

    @Test
    fun decompLUOfSquareMatrix() {
        val rng = Random(10)
        val n = 5
        val A = Matrix(n, Zmod97) { _, _ -> rng.nextInt(97) }
        val (L, U) = A.decompLU()
        assertValueEquals(A, L * U)
    }

    @Test
    fun decompRankOfFullRankMatrix() {
        val A = Matrix(3, 3, Zmod7) { i, j -> if (i == j) 1 else 0 }
        val (L, R) = A.decompRank()
        assertEquals(3, L.column)
        assertEquals(3, R.row)
        assertValueEquals(A, L * R)
    }

    @Test
    fun decompRankOfRankDeficientMatrix() {
        val A = Matrix(3, 3, Zmod7) { _, _ -> 1 }
        val (L, R) = A.decompRank()
        assertEquals(1, L.column)
        assertEquals(1, R.row)
        assertValueEquals(A, L * R)
    }

    @Test
    fun decompRankOfNonSquareMatrix() {
        val A = Matrix(3, 2, Zmod7) { i, j -> i + j }
        val (L, R) = A.decompRank()
        assertEquals(2, L.column)
        assertEquals(2, R.row)
        assertValueEquals(A, L * R)
    }

    @Test
    fun decompRankOfZeroMatrix() {
        val A = Matrix.zero(3, 3, Zmod7)
        val (L, R) = A.decompRank()
        assertEquals(0, L.column)
        assertEquals(0, R.row)
        assertValueEquals(A, L * R)
    }

    @Test
    fun toCongDiagFormOfIdentityMatrix() {
        val A = Matrix.identity(3, Zmod7)
        val (Lambda, P) = A.toCongDiagForm()
        assertValueEquals(P.T * A * P, Matrix.diag(Lambda))
    }

    @Test
    fun toCongDiagFormOfZeroMatrix() {
        val A = Matrix.zero(3, 3, Zmod7)
        val (Lambda, P) = A.toCongDiagForm()
        assertTrue(Lambda.all { it == 0 })
        assertValueEquals(Matrix.identity(3, Zmod7), P)
    }

    @Test
    fun toCongDiagFormOfSymmetricMatrix() {
        val A = Matrix(3, 3, Zmod7) { i, j -> if (i == j) 2 else 1 }
        val (Lambda, P) = A.toCongDiagForm()
        assertValueEquals(P * A * P.T, Matrix.diag(Lambda))
    }

    @Test
    fun toCongDiagFormRandom() {
        val rng = Random(10)
        val A = Matrix(4, 4, Zmod97) { i, j -> rng.nextInt(100) }.run { this + this.T }
        val (Lambda, P) = A.toCongDiagForm()
        assertValueEquals(P * A * P.T, Matrix.diag(Lambda))
    }

    @Test
    fun toCongDiagFormRandom2() {
        val rng = Random(10)
        val A = Matrix(4, 4, Zmod97) { i, j -> rng.nextInt(100) }
            .run { this + this.T }
            .let { it - Matrix.diag(it.diag()) }
        val (Lambda, P) = A.toCongDiagForm()
        assertValueEquals(P * A * P.T, Matrix.diag(Lambda))
    }


    @Test
    fun toCongDiagFormOfNonSquareMatrixThrowsException() {
        val A = Matrix(2, 3, Zmod7) { i, j -> i + j }
        assertThrows<IllegalArgumentException> { A.toCongDiagForm() }
    }

    @Test
    fun toCongDiagFormOfNonSymmetricMatrixThrowsException() {
        val A = Matrix(3, 3, Zmod7) { i, j -> if (i < j) 1 else 0 }
        assertThrows<IllegalArgumentException> { A.toCongDiagForm() }
    }

}

fun main() {
    val A = Matrix.zero(3, 3, NumberModels.intModP(7))
    val (L, R) = A.decompRank()
    println(L)
    println(R)
//        assertEquals(0, L.column)
//        assertEquals(0, R.row)
//        assertEquals(A, L * R)
}