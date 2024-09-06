package linear

import TestUtils.assertEquals
import cn.mathsymk.linear.Matrix
import cn.mathsymk.linear.MatrixImpl
import cn.mathsymk.linear.joinToString
import cn.mathsymk.linear.toMutable
import cn.mathsymk.model.Multinomial
import cn.mathsymk.model.NumberModels
import org.junit.jupiter.api.Test

class MatrixTest {
    val Z = NumberModels.intAsIntegers()

    @Test
    fun testDetGB() {
        val mults = Multinomial.from(Z)
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
}