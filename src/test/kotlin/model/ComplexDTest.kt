package model

import io.github.ezrnest.model.Complex128
import io.github.ezrnest.model.Complex128.Companion.ONE
import io.github.ezrnest.model.Complex128.Companion.cos
import io.github.ezrnest.model.Complex128.Companion.cosh
import io.github.ezrnest.model.Complex128.Companion.cot
import io.github.ezrnest.model.Complex128.Companion.exp
import io.github.ezrnest.model.Complex128.Companion.ln
import io.github.ezrnest.model.Complex128.Companion.sin
import io.github.ezrnest.model.Complex128.Companion.sinh
import io.github.ezrnest.model.Complex128.Companion.tan
import io.github.ezrnest.model.Fraction
import kotlin.math.abs
import kotlin.test.*

class ComplexDTest {
    private fun doubleEquals(expected: Double, actual: Double, absoluteTolerance: Double = 1e-10): Boolean {
        return expected.toBits() == actual.toBits() || abs(expected - actual) < absoluteTolerance
    }

    fun assertEquals(expected: Complex128, actual: Complex128, absoluteTolerance: Double = 1e-10) {
        val isEqual = doubleEquals(expected.re, actual.re, absoluteTolerance) && doubleEquals(
            expected.im,
            actual.im,
            absoluteTolerance
        )
        asserter.assertTrue({ "Expected: $expected, Actual: $actual" }, isEqual)
    }


    @Test
    fun toString_PositiveImaginary() {
        val complex = Complex128(3.0, 4.0)
        assertEquals("3.0+4.0i", complex.toString())
    }

    @Test
    fun toString_NegativeImaginary() {
        val complex = Complex128(3.0, -4.0)
        assertEquals("3.0-4.0i", complex.toString())
    }

    @Test
    fun isZero_True() {
        val complex = Complex128(0.0, 0.0)
        assertTrue(complex.isZero)
    }

    @Test
    fun isZero_False() {
        val complex = Complex128(1.0, 0.0)
        assertTrue(!complex.isZero)
    }

    @Test
    fun arg_Positive() {
        val complex = Complex128(1.0, 1.0)
        assertEquals(Math.PI / 4, complex.arg, 1e-10)
    }

    @Test
    fun arg_Negative() {
        val complex = Complex128(1.0, -1.0)
        assertEquals(-Math.PI / 4, complex.arg, 1e-10)
    }

    @Test
    fun mod() {
        val complex = Complex128(3.0, 4.0)
        assertEquals(5.0, complex.mod, 1e-10)
    }

    @Test
    fun modSq() {
        val complex = Complex128(3.0, 4.0)
        assertEquals(25.0, complex.modSq, 1e-10)
    }

    @Test
    fun plus() {
        val complex1 = Complex128(1.0, 2.0)
        val complex2 = Complex128(3.0, 4.0)
        assertEquals(Complex128(4.0, 6.0), complex1 + complex2)
    }

    @Test
    fun unaryMinus() {
        val complex = Complex128(1.0, -2.0)
        assertEquals(Complex128(-1.0, 2.0), -complex)
    }

    @Test
    fun minus() {
        val complex1 = Complex128(1.0, 2.0)
        val complex2 = Complex128(3.0, 4.0)
        assertEquals(Complex128(-2.0, -2.0), complex1 - complex2)
    }

    @Test
    fun times() {
        val complex1 = Complex128(1.0, 2.0)
        val complex2 = Complex128(3.0, 4.0)
        assertEquals(Complex128(-5.0, 10.0), complex1 * complex2)
    }

    @Test
    fun times_Scalar() {
        val complex = Complex128(1.0, 2.0)
        assertEquals(Complex128(2.0, 4.0), complex * 2.0)
    }

    @Test
    fun div() {
        val complex1 = Complex128(1.0, 2.0)
        val complex2 = Complex128(3.0, 4.0)
        assertEquals(Complex128(0.44, 0.08), complex1 / complex2)
    }

    @Test
    fun div_Scalar() {
        val complex = Complex128(1.0, 2.0)
        assertEquals(Complex128(0.5, 1.0), complex / 2.0)
    }

    @Test
    fun div_ByZero() {
        val complex1 = Complex128(1.0, 2.0)
        val complex2 = Complex128(0.0, 0.0)
        assertEquals(Complex128(Double.NaN, Double.NaN), complex1 / complex2)
    }

    @Test
    fun inv() {
        val complex = Complex128(1.0, 2.0)
        assertEquals(Complex128(0.2, -0.4), complex.inv())
    }

    @Test
    fun conj() {
        val complex = Complex128(1.0, 2.0)
        assertEquals(Complex128(1.0, -2.0), complex.conj)
    }

    @Test
    fun pow_Integer() {
        val complex = Complex128(1.0, 2.0)
        assertEquals(Complex128(-11.0, -2.0), complex.pow(3))
    }

    @Test
    fun sqrt() {
        val complex = Complex128(3.0, 4.0)
        assertEquals(Complex128(2.0, 1.0), complex.sqrt())
    }

    @Test
    fun pow() {
        val complex = Complex128(3.0, 4.0)
        assertEquals(25.0, complex.pow(2).mod, 1e-10)
        assertEquals(Complex128(-7.0, 24.0), complex.pow(2))

        assertEquals(125.0, complex.pow(3).mod, 1e-10)
        assertEquals(Complex128(-117.0, 44.0), complex.pow(3))

        assertEquals(complex.inv(), complex.pow(-1))
        assertEquals(Complex128(0.12, -0.16), complex.pow(-1))
    }


    @Test
    fun root() {
        val complex = Complex128(3.0, 4.0)
        assertEquals(complex, complex.root(1))
        assertEquals(complex.sqrt(), complex.root(2))
        assertEquals(complex.pow(1.0 / 3.0), complex.root(3))


        assertEquals(complex, complex.root(4).pow(4))

//        assertEquals(ComplexD(1.455346690225355, 0.34356074972251244), complex.root(3))
    }

    @Test
    fun pow_Fraction() {
        val complex = Complex128(3.0, 4.0)
        val fraction = Fraction(1, 2)
        assertEquals(Complex128(2.0, 1.0), complex.pow(fraction))
    }

    @Test
    fun pow_Double() {
        val complex = Complex128(3.0, 4.0)
        assertEquals(Complex128(2.0, 1.0), complex.pow(0.5))
    }

    @Test
    fun sin_Zero() {
        val complex = Complex128(0.0, 0.0)
        assertEquals(Complex128(0.0, 0.0), sin(complex))
    }

    @Test
    fun sin_PositiveImaginary() {
        val complex = Complex128(0.0, 1.0)
        assertEquals(Complex128(0.0, 1.1752011936438014), sin(complex))
    }

    @Test
    fun sin_NegativeImaginary() {
        val complex = Complex128(0.0, -1.0)
        assertEquals(Complex128(0.0, -1.1752011936438014), sin(complex))
    }

    @Test
    fun cos_Zero() {
        val complex = Complex128(0.0, 0.0)
        assertEquals(Complex128(1.0, 0.0), cos(complex))
    }

    @Test
    fun cos_PositiveImaginary() {
        val complex = Complex128(0.0, 1.0)
        assertEquals(Complex128(1.5430806348152437, 0.0), cos(complex))
    }

    @Test
    fun cos_NegativeImaginary() {
        val complex = Complex128(0.0, -1.0)
        assertEquals(Complex128(1.5430806348152437, 0.0), cos(complex))
    }

    @Test
    fun tan_Zero() {
        val complex = Complex128(0.0, 0.0)
        assertEquals(Complex128(0.0, 0.0), tan(complex))
    }

    @Test
    fun tan_PositiveImaginary() {
        val complex = Complex128(0.0, 1.0)
        assertEquals(Complex128(0.0, 0.7615941559557649), tan(complex))
    }

    @Test
    fun tan_NegativeImaginary() {
        val complex = Complex128(0.0, -1.0)
        assertEquals(Complex128(0.0, -0.7615941559557649), tan(complex))
    }


    @Test
    fun trigonometricEqualities() {
        val complexList = listOf(Complex128(0.3, -0.4), Complex128(0.3, 0.4), Complex128(.8, .5), Complex128(-.8, .5))
        for (z in complexList) {
            assertEquals(ONE, sin(z).pow(2) + cos(z).pow(2)) // sin^2(z) + cos^2(z) = 1
            assertEquals(ONE, tan(z) * cot(z))
        }
    }

    @Test
    fun exponentialEqualities() {
        val complexList = listOf(Complex128(0.3, -0.4), Complex128(0.3, 0.4), Complex128(.8, .5), Complex128(-.8, .5))
        for (z in complexList) {
            assertEquals(exp(ln(z)), z)
            assertEquals(sinh(z), (exp(z) - exp(-z)) / 2.0)
            assertEquals(ONE, cosh(z).pow(2) - sinh(z).pow(2)) // cosh^2(z) - sinh^2(z) = 1
        }
    }


}

fun main() {
}