package linear

import TestUtils.assertEquals
import TestUtils.assertValueEquals
import cn.mathsymk.linear.Matrix
import cn.mathsymk.linear.MatrixImpl
import cn.mathsymk.linear.toMutable
import cn.mathsymk.model.Multinomial
import cn.mathsymk.model.NumberModels
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MatrixTest {
    val Z = NumberModels.intAsIntegers()
    val Zmod7 = NumberModels.intModP(7)

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

}