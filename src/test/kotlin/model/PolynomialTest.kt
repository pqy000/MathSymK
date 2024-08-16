package model

import cn.mathsymk.model.Fraction
import cn.mathsymk.model.NumberModels
import cn.mathsymk.structure.Field
import kotlin.test.Test
import kotlin.test.assertEquals


class PolynomialTest {
    private val field: Field<Fraction> = Fraction.FractionAsQuotient
    private val ints = NumberModels.IntAsIntegers


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
    fun testApply(){
        val f = Polynomial.of(ints, 1, 2, 1) // (x+1)^2
        assertEquals(4, f.apply(1))
        assertEquals(0, f.apply(-1))
        assertEquals(1, f.apply(0))

        val g = Polynomial.fromPower(ints, 4, 1) // x^4
        assertEquals(16, g.apply(2))
        assertEquals(0, g.apply(0))
        assertEquals(1, g.apply(1))
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
//    @Test
//    fun testGcd() {
//        val calInt: Unit = Calculators.integer()
//        val cal: Unit = Fraction.getCalculator()
//        val f: Unit = Polynomial.of(calInt, 1, 2, 1).mapTo(cal, Fraction::of) // 1 + 2x + x^2
//        val g: Unit = Polynomial.of(calInt, -2, -1, 1).mapTo(cal, Fraction::of) // -2 - x + x^2
//        val h: Unit = Polynomial.of(calInt, 1, 1).mapTo(cal, Fraction::of)
//        assertTrue("", h.valueEquals(f.gcd(g)))
//    }
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