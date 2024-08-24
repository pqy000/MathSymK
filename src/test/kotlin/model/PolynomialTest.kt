package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.Fraction
import cn.mathsymk.model.NumberModels
import cn.mathsymk.model.Polynomial
import cn.mathsymk.structure.Field
import cn.mathsymk.structure.Integers
import kotlin.test.Test
import kotlin.test.assertEquals


class PolynomialTest {
    private val fractions = Fraction.FractionAsQuotient
    private val ints = NumberModels.IntAsIntegers


    fun of(vararg coefficients: Int): Polynomial<Fraction> {
        return Polynomial.of(ints, *coefficients.toTypedArray()).mapTo(fractions, Fraction::of)
    }

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
    fun testSum2() {

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
            val poly1 = of(1, 1)  // 1 + x
            val poly2 = of(-1, 1) // -1 + x
            val roots1 = poly1.hasCommonRoot(poly2)
            assertEquals(false, roots1, "1 + x and -1 + x should not have a common root")
        }

        // Test case 2: Polynomials with a common root
        run {
            val poly1 = of(-1, 1)   // -1 + x
            val poly2 = of(1, -2, 1) // 1 - 2x + x^2
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

    //    @Test
//    fun difference() {
//        val p = Polynomial.of(ints, 1, 2, -3, 4, 5)
////        assertEquals(
////            "p.difference() = p(n)-p(n-1)",
////            p.difference().compute(Fraction.ONE),
////            p.compute(field.getOne()).subtract(p.compute(field.getZero()))
////        )
//    }
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
            val f = Polynomial.of(ints, 1, 2, 1).mapTo(fractions, Fraction::of) // 1 + 2x + x^2
            val g = Polynomial.of(ints, -2, -1, 1).mapTo(fractions, Fraction::of) // -2 - x + x^2
            val h = Polynomial.of(ints, 1, 1).mapTo(fractions, Fraction::of)
            assertValueEquals(h, f.gcd(g).toMonic())
        }
        run {
            val f = Polynomial.of(ints, 1, 2).mapTo(fractions, Fraction::of) // 1 + 2x
            val g = Polynomial.of(ints, -1, -2).mapTo(fractions, Fraction::of) // -1 - 2x
            assertValueEquals(f.toMonic(), f.gcd(g).toMonic())
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