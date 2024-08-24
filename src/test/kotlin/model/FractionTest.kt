package model

import cn.mathsymk.model.Fraction
import cn.mathsymk.model.toFrac
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FractionTest {

    @Test
    fun testCreate(){
        val a = Fraction.of(0, 1)
        assertEquals(Fraction.ZERO, a)
        val b = Fraction.of(3, -4)
        assertEquals(Fraction.of(-3, 4), b)
        assertEquals(b, Fraction.of(-6, 8))
    }

    @Test
    fun testLongIntToFrac(){
        val a = 3L.toFrac()
        val b = 2.toFrac()
        assertEquals(Fraction.of(3), a)
        assertEquals(Fraction.of(2), b)
    }

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


    @Test
    fun additionWithNegativeFractions() {
        val f1 = Fraction.of(-1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 + f2
        assertEquals(Fraction.of(-1, 6), result)
    }

    @Test
    fun subtractionWithNegativeFractions() {
        val f1 = Fraction.of(-1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 - f2
        assertEquals(Fraction.of(-5, 6), result)
    }

    @Test
    fun multiplicationWithNegativeFractions() {
        val f1 = Fraction.of(-1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 * f2
        assertEquals(Fraction.of(-1, 6), result)
    }

    @Test
    fun divisionWithNegativeFractions() {
        val f1 = Fraction.of(-1, 2)
        val f2 = Fraction.of(1, 3)
        val result = f1 / f2
        assertEquals(Fraction.of(-3, 2), result)
    }

    @Test
    fun additionWithZero() {
        val f1 = Fraction.of(0, 1)
        val f2 = Fraction.of(1, 3)
        val result = f1 + f2
        assertEquals(Fraction.of(1, 3), result)
    }

    @Test
    fun subtractionWithZero() {
        val f1 = Fraction.of(1, 2)
        val f2 = Fraction.of(0, 1)
        val result = f1 - f2
        assertEquals(Fraction.of(1, 2), result)
    }

    @Test
    fun multiplicationWithZero() {
        val f1 = Fraction.of(0, 1)
        val f2 = Fraction.of(1, 3)
        val result = f1 * f2
        assertEquals(Fraction.ZERO, result)
    }

    @Test
    fun divisionWithZeroNumerator() {
        val f1 = Fraction.of(0, 1)
        val f2 = Fraction.of(1, 3)
        val result = f1 / f2
        assertEquals(Fraction.ZERO, result)
    }

    @Test
    fun divisionByZeroThrowsException() {
        val f1 = Fraction.of(1, 2)
        val f2 = Fraction.of(0, 1)
        assertThrows(ArithmeticException::class.java) {
            f1 / f2
        }
    }

    @Test
    fun reciprocalOfZeroThrowsException() {
        val f = Fraction.of(0, 1)
        assertThrows(ArithmeticException::class.java) {
            f.reciprocal()
        }
    }

    @Test
    fun powWithZeroExponent() {
        val f = Fraction.of(3, 4)
        val result = f.pow(0)
        assertEquals(Fraction.ONE, result)
    }

    @Test
    fun powWithNegativeExponent() {
        val f = Fraction.of(3, 4)
        val result = f.pow(-2)
        assertEquals(Fraction.of(16, 9), result)
    }

    @Test
    fun squared(){
        val f = Fraction.of(3, 4)
        val result = f.squared()
        assertEquals(Fraction.of(9, 16), result)
    }

//    @Test
//    fun expWithZeroExponent() {
//        val f = Fraction.of(3, 4)
//        val exp = Fraction.of(0, 1)
//        val result = f.exp(exp)
//        assertEquals(Fraction.ONE, result)
//    }
//
//    @Test
//    fun expWithNegativeExponent() {
//        val f = Fraction.of(3, 4)
//        val exp = Fraction.of(-1, 1)
//        val result = f.exp(exp)
//        assertEquals(Fraction.of(4, 3), result)
//    }

    @Test
    fun floorOfPositiveFraction() {
        val f = Fraction.of(7, 3)
        val result = f.floor()
        assertEquals(2, result)
    }

    @Test
    fun ceilOfNegativeFraction() {
        val f = Fraction.of(-7, 3)
        val result = f.ceil()
        assertEquals(-2, result)
    }

    @Test
    fun isIntegerForIntegerFraction() {
        val f = Fraction.of(4, 1)
        assertTrue(f.isInteger)
    }

    @Test
    fun isIntegerForNonIntegerFraction() {
        val f = Fraction.of(4, 3)
        assertFalse(f.isInteger)
    }

    @Test
    fun isNegativeForNegativeFraction() {
        val f = Fraction.of(-4, 3)
        assertTrue(f.isNegative)
    }

    @Test
    fun isNegativeForPositiveFraction() {
        val f = Fraction.of(4, 3)
        assertFalse(f.isNegative)
    }

    @Test
    fun isPositiveForPositiveFraction() {
        val f = Fraction.of(4, 3)
        assertTrue(f.isPositive)
    }

    @Test
    fun isPositiveForNegativeFraction() {
        val f = Fraction.of(-4, 3)
        assertFalse(f.isPositive)
    }

    @Test
    fun signumForPositiveFraction() {
        val f = Fraction.of(4, 3)
        assertEquals(1, f.signum)
    }

    @Test
    fun signumForNegativeFraction() {
        val f = Fraction.of(-4, 3)
        assertEquals(-1, f.signum)
    }

    @Test
    fun signumForZeroFraction() {
        val f = Fraction.of(0, 1)
        assertEquals(0, f.signum)
    }

    @Test
    fun numeratorAbsForPositiveFraction() {
        val f = Fraction.of(4, 3)
        assertEquals(4, f.numeratorAbs)
    }

    @Test
    fun numeratorAbsForNegativeFraction() {
        val f = Fraction.of(-4, 3)
        assertEquals(4, f.numeratorAbs)
    }

    @Test
    fun toLongForIntegerFraction() {
        val f = Fraction.of(4, 1)
        assertEquals(4L, f.toLong())
    }

    @Test
    fun toLongThrowsExceptionForNonIntegerFraction() {
        val f = Fraction.of(4, 3)
        assertThrows(ArithmeticException::class.java) {
            f.toLong()
        }
    }

    @Test
    fun absForPositiveFraction() {
        val f = Fraction.of(4, 3)
        assertEquals(Fraction.of(4, 3), f.abs())
    }

    @Test
    fun absForNegativeFraction() {
        val f = Fraction.of(-4, 3)
        assertEquals(Fraction.of(4, 3), f.abs())
    }


    @Test
    fun divideAndRemainderScenarios() {
        run {
            val f1 = Fraction.of(7, 3)
            val f2 = Fraction.of(2, 3)
            val (quotient, remainder) = f1.divideAndRemainder(f2)
            assertEquals(3, quotient)
            assertEquals(Fraction.of(1, 3), remainder)
        }
        run {
            val f1 = Fraction.of(7, 3)
            val f2 = Fraction.of(1, 2)
            val (quotient, remainder) = f1.divideAndRemainder(f2)
            assertEquals(4, quotient)
            assertEquals(Fraction.of(1, 3), remainder)
        }
        run {
            val f1 = Fraction.of(0, 1)
            val f2 = Fraction.of(1, 3)
            val (quotient, remainder) = f1.divideAndRemainder(f2)
            assertEquals(0, quotient)
            assertEquals(Fraction.ZERO, remainder)
        }
        run {
            val f1 = Fraction.of(1, 2)
            val f2 = Fraction.of(0, 1)
            assertThrows(ArithmeticException::class.java) {
                f1.divideAndRemainder(f2)
            }
        }
    }

    @Test
    fun remainderScenarios() {
        run {
            val f1 = Fraction.of(7, 3)
            val f2 = Fraction.of(2, 3)
            val result = f1.remainder(f2)
            assertEquals(Fraction.of(1, 3), result)
        }
        run {
            val f1 = Fraction.of(7, 3)
            val f2 = Fraction.of(1, 2)
            val result = f1.remainder(f2)
            assertEquals(Fraction.of(1, 3), result)
        }
        run {
            val f1 = Fraction.of(0, 1)
            val f2 = Fraction.of(1, 3)
            val result = f1.remainder(f2)
            assertEquals(Fraction.ZERO, result)
        }
        run {
            val f1 = Fraction.of(1, 2)
            val f2 = Fraction.of(0, 1)
            assertThrows(ArithmeticException::class.java) {
                f1.remainder(f2)
            }
        }
    }

    @Test
    fun toStringWithBracketScenarios() {
        run {
            val f = Fraction.of(3, 4)
            assertEquals("(3/4)", f.toStringWithBracket())
        }
        run {
            val f = Fraction.of(1)
            assertEquals("1", f.toStringWithBracket())
        }
        run {
            val f = Fraction.of(-5, 2)
            assertEquals("(-5/2)", f.toStringWithBracket())
        }
        run {
            val f = Fraction.of(0)
            assertEquals("0", f.toStringWithBracket())
        }
    }

    @Test
    fun toLatexStringScenarios() {
        run {
            val f = Fraction.of(3, 4)
            assertEquals("\\frac{3}{4}", f.toLatexString())
        }
        run {
            val f = Fraction.of(1)
            assertEquals("1", f.toLatexString())
        }
        run {
            val f = Fraction.of(-5, 2)
            assertEquals("-\\frac{5}{2}", f.toLatexString())
        }
        run {
            val f = Fraction.of(0)
            assertEquals("0", f.toLatexString())
        }
    }
}