package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.Fraction
import cn.mathsymk.model.NumberModels
import cn.mathsymk.model.PTerm
import cn.mathsymk.model.Polynomial
import cn.mathsymk.model.struct.times
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals


class PolynomialTest {
    private val fractions = Fraction.FractionAsQuotient
    private val ints = NumberModels.IntAsIntegers


    private fun ofF(vararg cs: Int): Polynomial<Fraction> {
        return Polynomial.fromList(ints, cs.asList()).mapTo(fractions, Fraction::of)
    }

    private fun ofI(vararg cs: Int): Polynomial<Int> {
        return Polynomial.fromList(ints, cs.asList())
    }

    private val x = Polynomial.x(ints)

    @Test
    fun testSum() {
        val f = Polynomial.of(ints, 1, 2, 1)
        val g = Polynomial.of(ints, 1, 1)
        val h = Polynomial.of(ints, 2, 3, 1)
        assertEquals(h, f + g, "(x^2+2x+1)+(x+1) = x^2+3x+2")
        assertEquals(-1, (f - f).degree)
        assertEquals(Polynomial.zero(ints), f - f)
        assertEquals(f, f + Polynomial.zero(ints))
        assertEquals(f, Polynomial.zero(ints) + f)

        assertEquals(f * 3, f + f + f)
        assertEquals(f * 3, Polynomial.sum(ints, f, f, f))
        assertEquals(f, Polynomial.sum(ints, f, -f, f))
        assertEquals(f + g, Polynomial.sum(ints, f, g))
    }


    @Test
    fun testMul() {
        val f = Polynomial.of(ints, 1, 2, 1)
        val g = Polynomial.of(ints, 1, 1)
        val h = Polynomial.of(ints, 1, 3, 3, 1)
        assertEquals(h, f * g, "(x^2+2x+1)(x+1) = x^3+3x^2+3x+1")
        val zero = Polynomial.zero(ints)

        assertEquals(zero, zero * f)
        assertEquals(zero, f * zero)
        assertEquals(zero, f * 0)
    }

    @Test
    fun additionOfPolynomials() {
        run {
            val p1 = ofI(1, 2, 3) // 1 + 2x + 3x^2
            val p2 = ofI(3, 2, 1) // 3 + 2x + 1x^2
            val result = p1 + p2
            assertEquals(ofI(4, 4, 4), result) // 4 + 4x + 4x^2
        }
        run {
            val p1 = ofI(1, 0, -1) // 1 - x^2
            val p2 = ofI(-1, 0, 1) // -1 + x^2
            val result = p1 + p2
            assertEquals(ofI(0), result) // 0
        }
    }

    @Test
    fun subtractionOfPolynomials() {
        run {
            val p1 = ofI(1, 2, 3) // 1 + 2x + 3x^2
            val p2 = ofI(3, 2, 1) // 3 + 2x + 1x^2
            val result = p1 - p2
            assertEquals(ofI(-2, 0, 2), result) // -2 + 2x^2
        }
        run {
            val p1 = ofI(1, 0, -1) // 1 - x^2
            val p2 = ofI(-1, 0, 1) // -1 + x^2
            val result = p1 - p2
            assertEquals(ofI(2, 0, -2), result) // 2 - 2x^2
        }
    }

    @Test
    fun multiplicationOfPolynomials() {
        run {
            val p1 = ofI(1, 2) // 1 + 2x
            val p2 = ofI(3, 4) // 3 + 4x
            val result = p1 * p2
            assertEquals(ofI(3, 10, 8), result) // 3 + 10x + 8x^2
        }
        run {
            val p1 = ofI(1, 0, -1) // 1 - x^2
            val p2 = ofI(1, 0, 1) // 1 + x^2
            val result = p1 * p2
            assertEquals(ofI(1, 0, 0, 0, -1), result) // 1 - x^4
        }
    }

    @Test
    fun divisionOfPolynomials() {
        run {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(1, 1) // 1 + x
            val result = p1.exactDivide(p2)
            assertEquals(ofF(1, 1), result) // 1 + x
        }
        run {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(1, 1) // 1 + x
            val result = p1.exactDivide(p2)
            assertEquals(ofF(1, -1), result) // 1 - x
        }
        run {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(0) // 0
            assertThrows(ArithmeticException::class.java) {
                p1.exactDivide(p2)
            }
        }
    }

    @Test
    fun divideAndRemainderOfPolynomials() {
        run {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(1, 1) // 1 + x
            val (quotient, remainder) = p1.divideAndRemainder(p2)
            assertEquals(ofF(1, 1), quotient) // 1 + x
            assertEquals(ofF(0), remainder) // 0
        }
        run {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(1, 1) // 1 + x
            val (quotient, remainder) = p1.divideAndRemainder(p2)
            assertEquals(ofF(1, -1), quotient) // 1 - x
            assertEquals(ofF(0), remainder) // 0
        }
        run {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(2) // 2
            val (quotient, remainder) = p1.divideAndRemainder(p2)
            assertEquals(ofF(1, 0, -1).mapTo(fractions) { it / Fraction.of(2) }, quotient) // 1/2 - 1/2x^2
            assertEquals(ofF(0), remainder) // 0
        }
        run {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(0) // 0
            assertThrows(ArithmeticException::class.java) {
                p1.divideAndRemainder(p2)
            }
        }

        run {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(2, 1) // 2 + x
            val (quotient, remainder) = p1.divideAndRemainder(p2)
            // 1 + 2x + x^2 = (2+x)x + 1
            assertEquals(ofF(0, 1), quotient)
            assertEquals(ofF(1), remainder)
        }
    }


    @Test
    fun powerOfPolynomial() {
        run {
            val p = ofI(1, 1) // 1 + x
            val result = p.pow(2)
            assertEquals(ofI(1, 2, 1), result) // 1 + 2x + x^2
        }
        run {
            val p = ofI(1, 1) // 1 + x
            val result = p.pow(0)
            assertEquals(ofI(1), result) // 1
        }
    }


    @Test
    fun testApply() {
        val f = Polynomial.of(ints, 1, 2, 1) // (x+1)^2
        assertEquals(4, f.apply(1))
        assertEquals(0, f.apply(-1))
        assertEquals(1, f.apply(0))

        val g = Polynomial.power(ints, 4, 1) // x^4
        assertEquals(16, g.apply(2))
        assertEquals(0, g.apply(0))
        assertEquals(1, g.apply(1))
    }

    @Test
    fun testMap() {
        val f1 = Polynomial.of(ints, 1, 2, 1) // (x+1)^2
        val f2 = f1.mapTo(fractions, Fraction::of)
        assertEquals(Fraction.of(4), f2.apply(Fraction.ONE))
        assertEquals(Fraction.of(f1.apply(1)), f2.apply(Fraction.ONE))

        val polyModel = NumberModels.asRing(Polynomial.zero(ints))
        val f3 = f1.mapTo(polyModel) { c -> Polynomial.constant(ints, c) }
        assertEquals(f1, f3.apply(Polynomial.x(ints)))
    }

    @Test
    fun testCommonRoots() {
        // Test case 1: Simple linear polynomials with no common root
        run {
            val poly1 = ofF(1, 1)  // 1 + x
            val poly2 = ofF(-1, 1) // -1 + x
            val roots1 = poly1.hasCommonRoot(poly2)
            assertEquals(false, roots1, "1 + x and -1 + x should not have a common root")
        }

        // Test case 2: Polynomials with a common root
        run {
            val poly1 = ofF(-1, 1)   // -1 + x
            val poly2 = ofF(1, -2, 1) // 1 - 2x + x^2
            val roots2 = poly1.hasCommonRoot(poly2)
            assertEquals(true, roots2, "-1 + x and 1 - 2x + x^2 should have a common root")
        }
    }

    @Test
    fun testPolynomialResultant() {
        // Test case 1: Simple linear polynomials with no common root
//        run {
//            val poly1 = of( 1, 1)  // 1 + x
//            val poly2 = of( -1, 1) // -1 + x
//            val res1 = poly1.resultant(poly2)
//            assertEquals(Fraction.of(2), res1, "Resultant of 1 + x and -1 + x should be 2")
//        }
//
//
//        // Test case
//        run {
//            val poly1 = of( -1, 1)   // -1 + x
//            val poly2 = of( 1, -2, 1) // 1 - 2x + x^2
//            val res = poly1.resultant(poly2)
//            assertEquals(Fraction.ZERO, res, "Resultant of -1 + x and 1 - 2x + x^2 should be 0")
//        }
    }

//        @Test
//    fun difference() {
//        val p = Polynomial.of(ints, 1, 2, -3, 4, 5)
//        assertEquals(
//            "p.difference() = p(n)-p(n-1)",
//            p.difference().compute(Fraction.ONE),
//            p.compute(field.getOne()).subtract(p.compute(field.getZero()))
//        )
//    }

    @Test
    fun derivativeOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.derivative()
            assertEquals(ofI(0), result) // 0
        }
        run {
            val p = ofI(3, 2) // 3 + 2x
            val result = p.derivative()
            assertEquals(ofI(2), result) // 2
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.derivative()
            assertEquals(ofI(-3, 4), result) // -3 + 4x
        }
        run {
            val p = ofI(0) // 0
            val result = p.derivative()
            assertEquals(ofI(0), result) // 0
        }
        run {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p.derivative()
            assertEquals(ofI(0, 0, 12), result) // 12x^2
        }
    }


    //
//    @Test
//    fun sumOfN() {
//        val p: Unit = Polynomial.of(Calculators.integer(), 1, 2, -3, 4, 5).mapTo(field, Fraction::of)
//        assertTrue("p.sumOfN().difference() = p", p.sumOfN().difference().valueEquals(p))
//        assertEquals("p.difference().sumOfN() = p + C", 0, p.difference().sumOfN().subtract(p).getLeadingPower())
//    }
//
    @Test
    fun testGcd() {
        run {
            val f = ofF(1, 2, 1) // 1 + 2x + x^2
            val g = ofF(-2, -1, 1) // -2 - x + x^2
            val h = ofF(1, 1)
            assertValueEquals(h, f.gcd(g).toMonic())
        }
        run {
            val f = ofF(1, 2) // 1 + 2x
            val g = ofF(-1, -2) // -1 - 2x
            assertValueEquals(f.toMonic(), f.gcd(g).toMonic())
        }

        run {
            val f = ofI(1, 2) // 1 + 2x
            val g = ofI(-1, -2) // -1 - 2x
            assertValueEquals(f.toPrimitive(), f.gcd(g).toPrimitive())
        }

        run {
            val p1 = x + 1
            val p2 = 2 * x + 1
            val p3 = x + 2
            val f1 = p1 * p2
            val f2 = p1 * p3
            assertValueEquals(p1, f1.gcd(f2))
        }
    }

    @Test
    fun integralOfVariousPolynomials() {
        run {
            val p = ofF(5) // 5
            val result = p.integral()
            assertEquals(ofF(0, 5), result) // 5x
        }
        run {
            val p = ofF(3, 2) // 3 + 2x
            val result = p.integral()
            assertEquals(ofF(0, 3, 1), result) // 3x + x^2
        }
        run {
            val p = ofF(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.integral()
            val expected =
                Polynomial.of(fractions, Fraction.ZERO, Fraction.of(1), Fraction.of(-3, 2), Fraction.of(2, 3))
            assertEquals(expected, result) // x - 1.5x^2 + 2/3x^3
        }
        run {
            val p = ofF(0) // 0
            val result = p.integral()
            assertEquals(ofF(0), result) // 0
        }
        run {
            val p = ofF(1, 0, 0, 4) // 1 + 4x^3
            val result = p.integral()
            assertEquals(ofF(0, 1, 0, 0, 1), result) // x + x^4
        }
    }

    @Test
    fun contentOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.cont()
            assertEquals(5, result)
        }
        run {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.cont()
            assertEquals(3, result)
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.cont()
            assertEquals(1, result)
        }
        run {
            val p = ofI(0) // 0
            val result = p.cont()
            assertEquals(0, result)
        }
        run {
            val p = ofI(4, 8, 12) // 4 + 8x + 12x^2
            val result = p.cont()
            assertEquals(4, result)
        }
    }

    @Test
    fun primitivePartOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.toPrimitive()
            assertEquals(ofI(1), result) // 1
        }
        run {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.toPrimitive()
            assertEquals(ofI(1, 2, 3), result) // 1 + 2x + 3x^2
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.toPrimitive()
            assertEquals(ofI(1, -3, 2), result) // 1 - 3x + 2x^2
        }
        run {
            val p = ofI(0) // 0
            val result = p.toPrimitive()
            assertEquals(ofI(0), result) // 0
        }
        run {
            val p = ofI(4, 8, 12) // 4 + 8x + 12x^2
            val result = p.toPrimitive()
            assertEquals(ofI(1, 2, 3), result) // 1 + 2x + 3x^2
        }
    }

    @Test
    fun constantOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.constantCoef
            assertEquals(5, result)
        }
        run {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.constantCoef
            assertEquals(3, result)
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.constantCoef
            assertEquals(1, result)
        }
    }

    @Test
    fun leadTermOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.leadTerm
            assertEquals(PTerm(0, 5), result)
        }
        run {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.leadTerm
            assertEquals(PTerm(2, 9), result)
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.leadTerm
            assertEquals(PTerm(2, 2), result)
        }
        run {
            val p = ofI(0) // 0
            assertThrows(NoSuchElementException::class.java) {
                p.leadTerm
            }
        }
        run {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p.leadTerm
            assertEquals(PTerm(3, 4), result)
        }
    }


    @Test
    fun degreeOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.degree
            assertEquals(0, result)
        }
        run {
            val p = ofI(3, 2) // 3 + 2x
            val result = p.degree
            assertEquals(1, result)
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.degree
            assertEquals(2, result)
        }
        run {
            val p = ofI(0) // 0
            val result = p.degree
            assertEquals(-1, result)
        }
        run {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p.degree
            assertEquals(3, result)
        }
    }

    @Test
    fun getCoefficientOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p[0]
            assertEquals(5, result)
        }
        run {
            val p = ofI(3, 2) // 3 + 2x
            val result = p[1]
            assertEquals(2, result)
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p[2]
            assertEquals(2, result)
        }
        run {
            val p = ofI(0) // 0
            val result = p[0]
            assertEquals(0, result)
        }
        run {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p[3]
            assertEquals(4, result)
        }
    }

    @Test
    fun coefficientListOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.coefficientList()
            assertEquals(listOf(5), result)
        }
        run {
            val p = ofI(3, 2) // 3 + 2x
            val result = p.coefficientList()
            assertEquals(listOf(3, 2), result)
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.coefficientList()
            assertEquals(listOf(1, -3, 2), result)
        }
        run {
            val p = ofI(0) // 0
            val result = p.coefficientList()
            assertEquals(emptyList(), result)
        }
        run {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p.coefficientList()
            assertEquals(listOf(1, 0, 0, 4), result)
        }
    }

    @Test
    fun constantCoefficientOfVariousPolynomials() {
        run {
            val p = ofI(5) // 5
            val result = p.constantCoef
            assertEquals(5, result)
        }
        run {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.constantCoef
            assertEquals(3, result)
        }
        run {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.constantCoef
            assertEquals(1, result)
        }
        run {
            val p = ofI(0) // 0
            val result = p.constantCoef
            assertEquals(0, result)
        }
        run {
            val p = ofI(4, 8, 12) // 4 + 8x + 12x^2
            val result = p.constantCoef
            assertEquals(4, result)
        }
    }
//
//    @Test
//    fun reverse() {
//        var c1 = Arrays.asList(1, 2, -3, 4, 5)
//        var c2 = c1.reversed()
//        var p = Polynomial.of(Calculators.integer(), c1)
//        var q = Polynomial.of(Calculators.integer(), c2)
//
//        assertValueEquals(q, p.reversed())
//        assertValueEquals(p, q.reversed())
//
//        c1 = mutableListOf(0, 0, 1, 2, 3)
//        c2 = mutableListOf(3, 2, 1)
//        p = Polynomial.of(Calculators.integer(), c1)
//        q = Polynomial.of(Calculators.integer(), c2)
//        assertValueEquals(q, p.reversed())
//    }
}

fun main() {
    val ints = NumberModels.IntAsIntegers
    val x = Polynomial.x(ints)
    val p1 = x + 1
    val p2 = 2 * x + 1
    val p3 = x + 2
    val f1 = p1 * p2
    val f2 = p1 * p3
    println(f1.gcd(f2)) // x + 1

}