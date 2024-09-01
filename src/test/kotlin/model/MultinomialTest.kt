package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.*
import kotlin.test.*

class MultinomialTest {
    val intModP = NumberModels.intModP(97, cached = true)
    val mult = Multinomial.from(intModP)

    @Test
    fun additionOfMultinomials() {
        with(mult) {
            val m1 = x + 2 * y
            val m2 = 3 * x + 4 * z
            val result = m1 + m2
            val expected = 4 * x + 4 * z + 2 * y
            assertEquals(expected, result)
        }
    }

    @Test
    fun multiplicationOfMultinomials() {
        with(mult) {
            val m1 = of(1 to "x", 2 to "y")
            val m2 = of(3 to "x", 4 to "z")
            val result = m1 * m2
            val expected = of(3 to "x^2", 4 to "xz", 6 to "xy", 8 to "yz")
            assertEquals(expected, result)
        }
    }

    @Test
    fun zeroMultinomial() {
        with(mult) {
            val m1 = of(0 to "x", 0 to "y")
            assertTrue(m1.isZero)
        }
    }

    @Test
    fun additionWithZero() {
        with(mult) {
            val m1 = of(1 to "x", 2 to "y")
            val zero = of()
            val result = m1 + zero
            assertEquals(m1, result)
        }
    }


    @Test
    fun multiplicationWithZero() {
        with(mult) {
            val m1 = of(1 to "x", 2 to "y")
            val zero = of()
            val result = m1 * zero
            assertTrue(result.isZero)
        }
    }

    @Test
    fun unaryMinusMultinomial() {
        with(mult) {
            val m1 = of(1 to "x", 2 to "y")
            val result = -m1
            val expected = of(96 to "x", 95 to "y")
            assertEquals(expected, result)
        }

    }

    @Test
    fun parseMultinomial() {
        val term = TermChs.parseChar("x^2y")
        val expected = TermChs(arrayOf(ChPow("x", 2), ChPow("y", 1)))
        assertEquals(expected, term)
    }

    @Test
    fun testTermOrder() {
        // chsStrictSmallerThan
        val chComp = Comparator.naturalOrder<String>()
        val t1 = TermChs.parseChar( "x^2y")
        val t2 = TermChs.parseChar( "x^2z")
        val t3 = TermChs.parseChar( "x^3")
        val t4 = TermChs.parseChar( "x^2")
        assertTrue(t2.contains(t4,chComp))
        assertTrue(t3.contains(t4,chComp))
        assertTrue(t1.contains(t4,chComp))
        val t5 = TermChs.parseChar( "x^2yz")
        assertTrue(t5.contains(t1,chComp))
        assertTrue(t5.contains(t2,chComp))
        assertFalse(t2.contains(t1,chComp))

    }

    @Test
    fun divideAndRemainderWithExactDivision() {
        with(mult) {
            val m1 = of(6 to "x^2y", 4 to "xy^2")
            val m2 = of(2 to "xy")
            val (quotient, remainder) = m1.divideAndRemainder(m2)
            val expectedQuotient = of(3 to "x", 2 to "y")
            val expectedRemainder = zero
            assertEquals(expectedQuotient, quotient)
            assertEquals(expectedRemainder, remainder)
        }
    }

    @Test
    fun divideAndRemainderWithZeroDividend() {
        with(mult) {
            val m1 = of()
            val m2 = of(2 to "xy")
            val (quotient, remainder) = m1.divideAndRemainder(m2)
            val expectedQuotient = of()
            val expectedRemainder = of()
            assertEquals(expectedQuotient, quotient)
            assertEquals(expectedRemainder, remainder)
        }
    }

    @Test
    fun divideAndRemainderWithZeroDivisor() {
        with(mult) {
            val m1 = of(6 to "x^2y", 4 to "xy^2")
            val m2 = of()
            assertFailsWith<ArithmeticException> {
                m1.divideAndRemainder(m2)
            }
        }
    }

    @Test
    fun divideAndRemainderComprehensive() {
        with(mult) {
            val m1 = 3 * "x^2y" + 4 * "xy^3" + 3 * "x"  // 3x^2y + 4xy^3 + 3x
            val p = 2 * "xy" + 1 * "y"  // 2xy + y
            val m3 = 3 * "x" + 4 * "y"
            val product = m1 * p //  3xy + 6x^2y + 3x^2y^2 + 4xy^4 + 6x^3y^2 + 8x^2y^4
            val f = product + m3 //
            val (q, r) = f.divideAndRemainder(p)
            assertValueEquals(f, q * p + r)
            assertTrue(f.leadTermCompare(q * p) >= 0)
        }

        with(mult) {
            val f = "x^2y".m + "xy^2".m + "y^2".m
            val p1 = "xy".m - constant(1)
            val p2 = "y^2".m - constant(1)
            val (qs, r) = f.divideAndRemainder(listOf(p1, p2))
            println("$qs, $r")
            assertValueEquals(f, qs[0] * p1 + qs[1] * p2 + r)
            assertTrue(f.leadTermCompare(qs[0] * p1) >= 0)
            assertTrue(f.leadTermCompare(qs[1] * p1) >= 0)
        }
    }


    @Test
    fun testOrder() {
        with(mult){
            // x < y < z, so the power of x is compared first
            val f = "xy".m + "x^2".m + "y^2".m + "z".m
            assertEquals("x^2".m.leadTerm, f.leadTerm)
        }
        val mult2 = Multinomial.from(intModP, Multinomial.getMonomialOrderLex(Comparator.reverseOrder()))
        with(mult2) {
            // x > y > z, the power of z is compared first
            val f = "xy".m + "x^2".m + "y^2".m + "z".m
            assertEquals(z.leadTerm, f.leadTerm)
        }
        run {
            val f1 = with(mult) { "xy".m + "x^2".m + "y^2".m + "z".m }
            val f2 = with(mult2) { "xy".m + "x^2".m + "y^2".m + "z".m }
            assertNotEquals(f1, f2) // different order
            assertValueEquals(f1, f2) // different order, but valueEquals == true
        }
    }

}
