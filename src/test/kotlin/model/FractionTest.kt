package model

import cn.mathsymk.model.Fraction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FractionTest {

    @Test
    fun testAddition() {
        val f1 = Fraction.of(1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 + f2
        assertEquals(Fraction.of(5, 6), result)
    }

    @Test
    fun testSubtraction() {
        val f1 = Fraction.of(1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 - f2
        assertEquals(Fraction.of(1, 6), result)
    }

    @Test
    fun testMultiplication() {
        val f1 = Fraction.of(1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 * f2
        assertEquals(Fraction.of(1, 6), result)
    }

    @Test
    fun testDivision() {
        val f1 = Fraction.of(1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 / f2
        assertEquals(Fraction.of(3, 2), result)
    }

    @Test
    fun testToString() {
        val f = Fraction.of(1, 2)
        assertEquals("1/2", f.toString())
    }


    @Test
    fun testCreate() {
        val a = Fraction.of(0, 1)
        assertEquals(Fraction.ZERO, a)
        val b = Fraction.of(3, -4)
        assertEquals(Fraction.of(-3, 4), b)
        assertEquals(b, Fraction.of(-6, 8))
    }

    @Test
    fun testAdd() {
        val a = Fraction.ONE
        assertEquals(Fraction.of(2), a.plus(a))
        assertEquals(Fraction.ZERO, a - a)

        val b = Fraction.of(3, -4)
        assertEquals(Fraction.of(1, 4), b + a)
        assertEquals(Fraction.of(-7, 4), b - a)

        val c = Fraction.of(1, 6)
        assertEquals(Fraction.of("-7/12"), b + c)
        assertEquals(Fraction.of("-11/12"), b - c)

        assertEquals(b + a, b + 1)
        assertEquals(b - a, b - 1)
    }

    @Test
    fun testMultiply() {
        val a = Fraction.of("-3/4")
        val b = Fraction.of("6/5")
        assertEquals(Fraction.of("-9/10"), a * b)
        assertEquals(Fraction.of("-5/8"), a / b)
        assertEquals(Fraction.of(-3), a * 4)
        assertEquals(Fraction.of("-1/4"), a / 3)
    }
}