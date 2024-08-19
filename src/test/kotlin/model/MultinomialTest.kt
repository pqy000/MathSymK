package model

import cn.mathsymk.model.NumberModels
import model.Multinomial.Companion.of
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultinomialTest {
    val model = NumberModels.IntAsIntegers

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
        assertTrue(m1.isZero())
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
        assertTrue(result.isZero())
    }

    @Test
    fun unaryMinusMultinomial() {
        val m1 = of(model, 1 to "x", -2 to "y")
        val result = -m1
        val expected = of(model, -1 to "x", 2 to "y")
        assertEquals(expected, result)
    }

    @Test
    fun parseMultinomial() {
        val term = MTerm.parse(3, "x^2y")
        val expected = MTerm(arrayOf(ChPow("x", 2), ChPow("y", 1)), 3)
        assertEquals(expected, term)
    }
}