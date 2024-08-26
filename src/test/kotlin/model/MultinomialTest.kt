package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.ChPow
import cn.mathsymk.model.MTerm
import cn.mathsymk.model.Multinomial.Companion.of
import cn.mathsymk.model.NumberModels
import kotlin.test.*

class MultinomialTest {
    val model = NumberModels.intModP(97, cached = true)

    @Test
    fun additionOfMultinomials() {
        val m1 = of(model) { x + 2 * y }
        val m2 = of(model) { 3 * x + 4 * z }
        val result = m1 + m2
        val expected = of(model) { 4 * x + 4 * z + 2 * y }
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
    fun divideAndRemainderWithNonExactDivision() {
        val m1 = of(model, 6 to "x^2y", 4 to "xy^2", 3 to "x") // 6x^2y + 4xy^2 + 3x
        val m2 = of(model, 2 to "xy") // 2xy
        val (quotient, remainder) = m1.divideAndRemainder(m2)
        val expectedQuotient = of(model, 3 to "x", 2 to "y")
        val expectedRemainder = of(model, 3 to "x")
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
        val exception = assertFailsWith<ArithmeticException> {
            m1.divideAndRemainder(m2)
        }
        assertEquals("Division by zero", exception.message)
    }

}