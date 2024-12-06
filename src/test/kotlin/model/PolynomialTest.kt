package model

import io.github.ezrnest.mathsymk.model.Fraction
import io.github.ezrnest.mathsymk.model.Models
import io.github.ezrnest.mathsymk.model.PTerm
import io.github.ezrnest.mathsymk.model.Polynomial
import io.github.ezrnest.mathsymk.structure.sumOf
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals


class PolynomialTest {
    private val fractions = Fraction.model
    private val ints = Models.ints()
    private val Zx = Polynomial.over(ints)
    val Qx = Polynomial.over(fractions)

    private fun ofF(vararg cs: Int): Polynomial<Fraction> {
        val coefs = cs.map { Fraction.of(it) }
        return Polynomial.fromList(fractions, coefs)
    }

    private fun ofI(vararg cs: Int): Polynomial<Int> {
        return Polynomial.fromList(ints, cs.asList())
    }


    @Test
    fun testCreate() {
        with(Zx) {
            assertEquals(poly(1, 2, 1), 1 + 2 * x + x2)
            assertEquals(poly(1, 2, 1), io.github.ezrnest.mathsymk.model.Polynomial.of(ints, 1, 2, 1))
        }
    }

    @Test
    fun testSum() {
        with(Zx) {
            val f = 1 + 2 * x + x2
            val g = 1 + x
            val h = 2 + 3 * x + x2
            assertEquals(h, f + g, "(x^2+2x+1)+(x+1) = x^2+3x+2")
            assertEquals(-1, (f - f).degree)
            assertEquals(zero, f - f)
            assertEquals(f, f + zero)
            assertEquals(f, zero + f)

            assertEquals(f * 3, f + f + f)
            assertEquals(f * 3, sumOf(f, f, f))
            assertEquals(f, sumOf(f, -f, f))
            assertEquals(f + g, sumOf(f, g))
        }
    }


    @Test
    fun testMul() {
        with(Zx) {
            val f = poly(1, 2, 1) // (x+1)^2
            val g = poly(1, 1) // x+1
            val h = poly(1, 3, 3, 1) // x^3+3x^2+3x+1
            assertEquals(h, f * g, "(x^2+2x+1)(x+1) = x^3+3x^2+3x+1")
            assertEquals(f, f * 1)
            assertEquals(zero, f * 0)
            assertEquals(zero, 0 * f)
            assertEquals(zero, zero * f)
        }
    }

    @Test
    fun additionOfPolynomials() {
        with(Zx) {
            val p1 = poly(1, 2, 3) // 1 + 2x + 3x^2
            val p2 = poly(3, 2, 1) // 3 + 2x + 1x^2
            val result = p1 + p2
            assertEquals(poly(4, 4, 4), result) // 4 + 4x + 4x^2
        }
        with(Zx) {
            val p1 = poly(1, 0, -1) // 1 - x^2
            val p2 = poly(-1, 0, 1) // -1 + x^2
            val result = p1 + p2
            assertEquals(poly(0), result) // 0
        }
    }

    @Test
    fun subtractionOfPolynomials() {
        with(Zx) {
            val p1 = ofI(1, 2, 3) // 1 + 2x + 3x^2
            val p2 = ofI(3, 2, 1) // 3 + 2x + 1x^2
            val result = p1 - p2
            assertEquals(ofI(-2, 0, 2), result) // -2 + 2x^2
        }
        with(Zx) {
            val p1 = ofI(1, 0, -1) // 1 - x^2
            val p2 = ofI(-1, 0, 1) // -1 + x^2
            val result = p1 - p2
            assertEquals(ofI(2, 0, -2), result) // 2 - 2x^2
        }
    }

    @Test
    fun multiplicationOfPolynomials() {
        with(Zx) {
            val p1 = ofI(1, 2) // 1 + 2x
            val p2 = ofI(3, 4) // 3 + 4x
            val result = p1 * p2
            assertEquals(ofI(3, 10, 8), result) // 3 + 10x + 8x^2
        }
        with(Zx) {
            val p1 = ofI(1, 0, -1) // 1 - x^2
            val p2 = ofI(1, 0, 1) // 1 + x^2
            val result = p1 * p2
            assertEquals(ofI(1, 0, 0, 0, -1), result) // 1 - x^4
        }
    }

    @Test
    fun divisionOfPolynomials() {
        val Fx = Polynomial.over(fractions)
        with(Fx) {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(1, 1) // 1 + x
            val result = p1 / p2
            assertEquals(ofF(1, 1), result) // 1 + x
        }
        with(Fx) {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(1, 1) // 1 + x
            val result = p1.div(p2)
            assertEquals(ofF(1, -1), result) // 1 - x
        }
        with(Fx) {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(0) // 0
            assertThrows(ArithmeticException::class.java) {
                p1.div(p2)
            }
        }
    }

    @Test
    fun divAndRemOfPolynomials() {

        with(Qx) {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(1, 1) // 1 + x
            val (quotient, remainder) = p1.divAndRem(p2)
            assertEquals(ofF(1, 1), quotient) // 1 + x
            assertEquals(ofF(0), remainder) // 0
        }
        with(Qx) {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(1, 1) // 1 + x
            val (quotient, remainder) = p1.divAndRem(p2)
            assertEquals(ofF(1, -1), quotient) // 1 - x
            assertEquals(ofF(0), remainder) // 0
        }
        with(Qx) {
            val p1 = ofF(1, 0, -1) // 1 - x^2
            val p2 = ofF(2) // 2
            val (quotient, remainder) = p1.divAndRem(p2)
            assertEquals(ofF(1, 0, -1) / Fraction(2), quotient) // 1/2 - 1/2x^2
            assertEquals(ofF(0), remainder) // 0
        }
        with(Qx) {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(0) // 0
            assertThrows(ArithmeticException::class.java) {
                p1.divAndRem(p2)
            }
        }

        with(Qx) {
            val p1 = ofF(1, 2, 1) // 1 + 2x + x^2
            val p2 = ofF(2, 1) // 2 + x
            val (quotient, remainder) = p1.divAndRem(p2)
            // 1 + 2x + x^2 = (2+x)x + 1
            assertEquals(ofF(0, 1), quotient)
            assertEquals(ofF(1), remainder)
        }
    }


    @Test
    fun powerOfPolynomial() {
        with(Zx) {
            val p = ofI(1, 1) // 1 + x
            val result = p.pow(2)
            assertEquals(ofI(1, 2, 1), result) // 1 + 2x + x^2
        }
        with(Zx) {
            val p = ofI(1, 1) // 1 + x
            val result = p.pow(0)
            assertEquals(ofI(1), result) // 1
        }
    }


    @Test
    fun testApply() {
        val f = Polynomial.of(ints, 1, 2, 1) // (x+1)^2
        with(Zx) {
            assertEquals(4, f.apply(1))
            assertEquals(0, f.apply(-1))
            assertEquals(1, f.apply(0))
        }
        with(Zx) {
            val g = 1 xpow 4 // x^4
            assertEquals(16, g.apply(2))
            assertEquals(0, g.apply(0))
            assertEquals(1, g.apply(1))
        }
    }

//    @Test
//    fun testMap() {
//        val f1 = Polynomial.of(ints, 1, 2, 1) // (x+1)^2
//        val f2 = f1.mapTo(fractions, Fraction::of)
//        assertEquals(Fraction.of(4), f2.apply(Fraction.ONE))
//        assertEquals(Fraction.of(f1.apply(1)), f2.apply(Fraction.ONE))
//
//        val polyModel = NumberModels.asRing(Polynomial.zero(ints))
//        val f3 = f1.mapTo(polyModel) { c -> Polynomial.constant(ints, c) }
//        assertEquals(f1, f3.apply(Polynomial.x(ints)))
//    }

    @Test
    fun testCommonRoots() {
        // Test case 1: Simple linear polynomials with no common root
        with(Qx) {
            val poly1 = ofF(1, 1)  // 1 + x
            val poly2 = ofF(-1, 1) // -1 + x
            val roots1 = hasCommonRoot(poly1, poly2)
            assertEquals(false, roots1, "1 + x and -1 + x should not have a common root")
        }

        // Test case 2: Polynomials with a common root
        with(Qx) {
            val poly1 = ofF(-1, 1)   // -1 + x
            val poly2 = ofF(1, -2, 1) // 1 - 2x + x^2
            val roots2 = hasCommonRoot(poly1, poly2)
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
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.derivative()
            assertEquals(ofI(0), result) // 0
        }
        with(Zx) {
            val p = ofI(3, 2) // 3 + 2x
            val result = p.derivative()
            assertEquals(ofI(2), result) // 2
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.derivative()
            assertEquals(ofI(-3, 4), result) // -3 + 4x
        }
        with(Zx) {
            val p = ofI(0) // 0
            val result = p.derivative()
            assertEquals(ofI(0), result) // 0
        }
        with(Zx) {
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
        with(Qx) {
            val f = ofF(1, 2, 1) // 1 + 2x + x^2
            val g = ofF(-2, -1, 1) // -2 - x + x^2
            val h = ofF(1, 1)
            assertEquals(h, gcd(f, g).toMonic())
        }
        with(Qx) {
            val f = ofF(1, 2) // 1 + 2x
            val g = ofF(-1, -2) // -1 - 2x
            assertEquals(f.toMonic(), gcd(f, g).toMonic())
        }

        with(Zx) {
            val f = ofI(1, 2) // 1 + 2x
            val g = ofI(-1, -2) // -1 - 2x
            assertEquals(f.toPrimitive(), gcd(f, g).toPrimitive())
        }
        with(Zx) {
            val p1 = x + 1
            val p2 = 2 * x + 1
            val p3 = x + 2
            val f1 = p1 * p2
            val f2 = p1 * p3
            assertEquals(p1, gcd(f1, f2))
        }
    }

    @Test
    fun integralOfVariousPolynomials() {
        with(Qx) {
            val p = ofF(5) // 5
            val result = p.integral()
            assertEquals(ofF(0, 5), result) // 5x
        }
        with(Qx) {
            val p = ofF(3, 2) // 3 + 2x
            val result = p.integral()
            assertEquals(ofF(0, 3, 1), result) // 3x + x^2
        }
        with(Qx) {
            val p = ofF(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.integral()
            val expected =
                io.github.ezrnest.mathsymk.model.Polynomial.of(fractions, io.github.ezrnest.mathsymk.model.Fraction.ZERO, io.github.ezrnest.mathsymk.model.Fraction.of(1), io.github.ezrnest.mathsymk.model.Fraction.of(-3, 2), io.github.ezrnest.mathsymk.model.Fraction.of(2, 3))
            assertEquals(expected, result) // x - 1.5x^2 + 2/3x^3
        }
        with(Qx) {
            val p = ofF(0) // 0
            val result = p.integral()
            assertEquals(ofF(0), result) // 0
        }
        with(Qx) {
            val p = ofF(1, 0, 0, 4) // 1 + 4x^3
            val result = p.integral()
            assertEquals(ofF(0, 1, 0, 0, 1), result) // x + x^4
        }
    }

    @Test
    fun contentOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.cont()
            assertEquals(5, result)
        }
        with(Zx) {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.cont()
            assertEquals(3, result)
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.cont()
            assertEquals(1, result)
        }
        with(Zx) {
            val p = ofI(0) // 0
            val result = p.cont()
            assertEquals(0, result)
        }
        with(Zx) {
            val p = ofI(4, 8, 12) // 4 + 8x + 12x^2
            val result = p.cont()
            assertEquals(4, result)
        }
    }

    @Test
    fun primitivePartOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.toPrimitive()
            assertEquals(ofI(1), result) // 1
        }
        with(Zx) {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.toPrimitive()
            assertEquals(ofI(1, 2, 3), result) // 1 + 2x + 3x^2
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.toPrimitive()
            assertEquals(ofI(1, -3, 2), result) // 1 - 3x + 2x^2
        }
        with(Zx) {
            val p = ofI(0) // 0
            val result = p.toPrimitive()
            assertEquals(ofI(0), result) // 0
        }
        with(Zx) {
            val p = ofI(4, 8, 12) // 4 + 8x + 12x^2
            val result = p.toPrimitive()
            assertEquals(ofI(1, 2, 3), result) // 1 + 2x + 3x^2
        }
    }

    @Test
    fun constantOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.constantCoef
            assertEquals(5, result)
        }
        with(Zx) {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.constantCoef
            assertEquals(3, result)
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.constantCoef
            assertEquals(1, result)
        }
    }

    @Test
    fun leadTermOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.leadTerm
            assertEquals(PTerm(0, 5), result)
        }
        with(Zx) {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.leadTerm
            assertEquals(PTerm(2, 9), result)
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.leadTerm
            assertEquals(PTerm(2, 2), result)
        }
        with(Zx) {
            val p = ofI(0) // 0
            assertThrows(NoSuchElementException::class.java) {
                p.leadTerm
            }
        }
        with(Zx) {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p.leadTerm
            assertEquals(PTerm(3, 4), result)
        }
    }


    @Test
    fun degreeOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.degree
            assertEquals(0, result)
        }
        with(Zx) {
            val p = ofI(3, 2) // 3 + 2x
            val result = p.degree
            assertEquals(1, result)
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.degree
            assertEquals(2, result)
        }
        with(Zx) {
            val p = ofI(0) // 0
            val result = p.degree
            assertEquals(-1, result)
        }
        with(Zx) {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p.degree
            assertEquals(3, result)
        }
    }

    @Test
    fun getCoefficientOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p[0]
            assertEquals(5, result)
        }
        with(Zx) {
            val p = ofI(3, 2) // 3 + 2x
            val result = p[1]
            assertEquals(2, result)
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p[2]
            assertEquals(2, result)
        }
        with(Zx) {
            val p = ofI(0) // 0
            val result = p[0]
            assertEquals(0, result)
        }
        with(Zx) {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p[3]
            assertEquals(4, result)
        }
    }

    @Test
    fun coefficientListOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.coefficientList()
            assertEquals(listOf(5), result)
        }
        with(Zx) {
            val p = ofI(3, 2) // 3 + 2x
            val result = p.coefficientList()
            assertEquals(listOf(3, 2), result)
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.coefficientList()
            assertEquals(listOf(1, -3, 2), result)
        }
        with(Zx) {
            val p = ofI(0) // 0
            val result = p.coefficientList()
            assertEquals(emptyList(), result)
        }
        with(Zx) {
            val p = ofI(1, 0, 0, 4) // 1 + 4x^3
            val result = p.coefficientList()
            assertEquals(listOf(1, 0, 0, 4), result)
        }
    }

    @Test
    fun constantCoefficientOfVariousPolynomials() {
        with(Zx) {
            val p = ofI(5) // 5
            val result = p.constantCoef
            assertEquals(5, result)
        }
        with(Zx) {
            val p = ofI(3, 6, 9) // 3 + 6x + 9x^2
            val result = p.constantCoef
            assertEquals(3, result)
        }
        with(Zx) {
            val p = ofI(1, -3, 2) // 1 - 3x + 2x^2
            val result = p.constantCoef
            assertEquals(1, result)
        }
        with(Zx) {
            val p = ofI(0) // 0
            val result = p.constantCoef
            assertEquals(0, result)
        }
        with(Zx) {
            val p = ofI(4, 8, 12) // 4 + 8x + 12x^2
            val result = p.constantCoef
            assertEquals(4, result)
        }
    }

    @Test
    fun squareFreeFact(){
        with(Qx) {
            val a = ofF(1,1)
            val p = a pow 2
            val result = p.squareFreeFactorize()
            assertEquals(listOf(a to 2), result)
        }
        with(Polynomial.over(Models.intModP(7))){
            val f = x + x.pow(8) // x + x^8 = x (1 + x^7) = x (1 + x)^7
            val result = f.squareFreeFactorize()
            assertEquals(f, result.fold(one){acc, pair -> acc * pair.first.pow(pair.second)})
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