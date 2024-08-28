package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.ChPow
import cn.mathsymk.model.MTerm
import cn.mathsymk.model.Multinomial
import cn.mathsymk.model.Multinomial.Companion.of
import cn.mathsymk.model.Multinomial.Companion.with
import cn.mathsymk.model.NumberModels
import kotlin.test.*

class MultinomialTest {
    val model = NumberModels.intModP(97, cached = true)

    @Test
    fun additionOfMultinomials() {
        val m1 = with(model) { x + 2 * y }
        val m2 = with(model) { 3 * x + 4 * z }
        val result = m1 + m2
        val expected = with(model) { 4 * x + 4 * z + 2 * y }
        assertEquals(expected, result)

    }

    @Test
    fun multiplicationOfMultinomials() {
        val m1 = of(model, 1 to "x", 2 to "y")
        val m2 = of(model, 3 to "x", 4 to "z")
        val result = m1 * m2
        val expected = of(model, 3 to "x^2", 4 to "xz", 6 to "xy", 8 to "yz")
        assertEquals(expected, result)
    }

    @Test
    fun zeroMultinomial() {
        val m1 = of(model, 0 to "x", 0 to "y")
        assertTrue(m1.isZero)
    }

    @Test
    fun additionWithZero() {
        val m1 = of(model, 1 to "x", 2 to "y")
        val zero = of(model)
        val result = m1 + zero
        assertEquals(m1, result)
    }

    @Test
    fun multiplicationWithZero() {
        val m1 = of(model, 1 to "x", 2 to "y")
        val zero = of(model)
        val result = m1 * zero
        assertTrue(result.isZero)
    }

    @Test
    fun unaryMinusMultinomial() {
        val m1 = of(model, 1 to "x", -2 to "y")
        val result = -m1
        val expected = of(model, -1 to "x", 2 to "y")
//        assertEquals(expected, result)
        assertValueEquals(expected, result)
    }

    @Test
    fun parseMultinomial() {
        val term = MTerm.parse(3, "x^2y")
        val expected = MTerm(arrayOf(ChPow("x", 2), ChPow("y", 1)), 3)
        assertEquals(expected, term)
    }

    @Test
    fun testTermOrder() {
        // chsStrictSmallerThan
        val t1 = MTerm.parse(3, "x^2y")
        val t2 = MTerm.parse(3, "x^2z")
        val t3 = MTerm.parse(3, "x^3")
        val t4 = MTerm.parse(3, "x^2")
        assertTrue(t2.chsContains(t4))
        assertTrue(t3.chsContains(t4))
        assertTrue(t1.chsContains(t4))
        val t5 = MTerm.parse(3, "x^2yz")
        assertTrue(t5.chsContains(t1))
        assertTrue(t5.chsContains(t2))
        assertFalse(t2.chsContains(t1))

    }

    @Test
    fun divideAndRemainderWithExactDivision() {
        val m1 = of(model, 6 to "x^2y", 4 to "xy^2")
        val m2 = of(model, 2 to "xy")
        val (quotient, remainder) = m1.divideAndRemainder(m2)
        val expectedQuotient = of(model, 3 to "x", 2 to "y")
        val expectedRemainder = of(model)
        assertEquals(expectedQuotient, quotient)
        assertEquals(expectedRemainder, remainder)
    }

    @Test
    fun divideAndRemainderWithZeroDividend() {
        val m1 = of(model)
        val m2 = of(model, 2 to "xy")
        val (quotient, remainder) = m1.divideAndRemainder(m2)
        val expectedQuotient = of(model)
        val expectedRemainder = of(model)
        assertEquals(expectedQuotient, quotient)
        assertEquals(expectedRemainder, remainder)
    }

    @Test
    fun divideAndRemainderWithZeroDivisor() {
        val m1 = of(model, 6 to "x^2y", 4 to "xy^2")
        val m2 = of(model)
        assertFailsWith<ArithmeticException> {
            m1.divideAndRemainder(m2)
        }
    }

    @Test
    fun divideAndRemainderComprehensive() {
        with(model) {
            val m1 = 3 * "x^2y" + 4 * "xy^3" + 3 * "x"  // 3x^2y + 4xy^3 + 3x
            val p = 2 * "xy" + 1 * "y"  // 2xy + y
            val m3 = 3 * "x" + 4 * "y"
            val product = m1 * p //  3xy + 6x^2y + 3x^2y^2 + 4xy^4 + 6x^3y^2 + 8x^2y^4
            val f = product + m3 //
            val (q, r) = f.divideAndRemainder(p)
            assertValueEquals(f, q * p + r)
            assertTrue(f.leadTermCompare(q * p) >= 0)
        }

        with(model) {
            val f = "x^2y".m + "xy^2".m + "y^2".m
            val p1 = "xy".m - 1.m
            val p2 = "y^2".m - 1.m
            val (qs, r) = f.divideAndRemainder(listOf(p1, p2))
            assertValueEquals(f, qs[0] * p1 + qs[1] * p2 + r)
            assertTrue(f.leadTermCompare(qs[0] * p1) >= 0)
            assertTrue(f.leadTermCompare(qs[1] * p1) >= 0)
        }
    }


    @Test
    fun testOrder() {
        val tc0 = Multinomial.DEFAULT_TERM_COMPARATOR
        with(model, tc0){
            // x < y < z, so the power of x is compared first
            val f = "xy".m + "x^2".m + "y^2".m + "z".m
            assertEquals("x^2".m.leadTerm, f.leadTerm)
        }
        val tc1 = Multinomial.getTermComparatorLexGraded(Comparator.reverseOrder())
        with(model, tc1) {
            // x > y > z, the power of z is compared first
            val f = "xy".m + "x^2".m + "y^2".m + "z".m
            assertEquals(z.leadTerm, f.leadTerm)
        }
        run{
            val f1 = with(model, tc0){ "xy".m + "x^2".m + "y^2".m + "z".m }
            val f2 = with(model, tc1){ "xy".m + "x^2".m + "y^2".m + "z".m }
            assertNotEquals(f1,f2) // different order
            assertValueEquals(f1,f2) // different order, but valueEquals == true
        }
    }

}

fun main() {
    val model = NumberModels.intModP(97, cached = true)
    val ints = NumberModels.intAsIntegers()
    run {
        val m1 =
            with(model) { 3 * "x^2y" + 4 * "xy^3" + 3 * "x" } // 3x^2y + 4xy^3 + 3x
        val m2 = with(model) { 2 * "xy" + 1 * "y" } // 2xy + y
        val m3 = with(model) { 3 * "x" + 4 * "y" }
        val product = m1 * m2 //  3xy + 6x^2y + 3x^2y^2 + 4xy^4 + 6x^3y^2 + 8x^2y^4
        val m4 = product + m3 //
        val (q, r) = m4.divideAndRemainder(m2)
        assertEquals(m1, q)
        assertEquals(with(model) { 3 * "x" + 4 * "y" }, r)
    }
    run {
        val m1 =
            with(ints) { 1 * "x^2y" + 1 * "xy^2" + 1 * "y^2" } // x^2 + xy^2 + y^2
        val m2 = with(ints) { 1 * "xy" - 1 * "" } // x + y
        val m3 = with(ints) { 1 * "y^2" - 1 * "" } // y^2 - 1
        println(m1)
        println(m2)
        println(m3)
        println()
        val (qs, r) = m1.divideAndRemainder(listOf(m2, m3))
        println(qs)
        println(r)
    }
}