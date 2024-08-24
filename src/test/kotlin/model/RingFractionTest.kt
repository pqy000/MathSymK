package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.Fraction
import cn.mathsymk.model.NumberModels
import cn.mathsymk.model.Polynomial
import cn.mathsymk.model.RingFraction
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RingFractionTest {
    val intRing = NumberModels.IntAsIntegers

    val pInt = Polynomial.asRing(intRing)

    val polyRingFrac = Polynomial.asRing(Fraction.asQuotient)


    @Test
    fun testEquals() {
        run {
            val f1 = RingFraction.of(3, 4, intRing)
            val f2 = RingFraction.of(3, 4, intRing)
            assertEquals(f1, f2)
        }
        run {
            val f1 = RingFraction.of(3, 4, intRing)
            val f2 = RingFraction.of(1, 2, intRing)
            assertNotEquals(f1, f2)
        }

        run {
            val f1 = RingFraction.of(2, 4, intRing)
            val f2 = RingFraction.of(1, 2, intRing)
            assertEquals(f1, f2)
        }
    }

    @Test
    fun testMultiply() {
        run {
            val f1 = RingFraction.of(3, 4, intRing)
            val f2 = RingFraction.of(1, 2, intRing)
            val result = f1 * f2
            assertEquals(RingFraction.of(3, 8, intRing), result)
        }
        run {
            val f1 = RingFraction.of(-3, -5, intRing)
            val f2 = RingFraction.of(2, 3, intRing)
            val result = f1 * f2
            assertValueEquals(RingFraction.of(6, 15, intRing), result)
            assertEquals(RingFraction.of(-2, -5, intRing), result)
        }
    }

    @Test
    fun additionOfRingFractions() {
        run {
            val f1 = RingFraction.of(1, 3, intRing)
            val f2 = RingFraction.of(1, 6, intRing)
            val result = f1 + f2
            assertEquals(RingFraction.of(1, 2, intRing), result)
        }
        run {
            val f1 = RingFraction.of(-1, 3, intRing)
            val f2 = RingFraction.of(1, 3, intRing)
            val result = f1 + f2
            assertEquals(RingFraction.of(0, 1, intRing), result)
        }
    }

    @Test
    fun subtractionOfRingFractions() {
        run {
            val f1 = RingFraction.of(1, 2, intRing)
            val f2 = RingFraction.of(1, 3, intRing)
            val result = f1 - f2
            assertEquals(RingFraction.of(1, 6, intRing), result)
        }
        run {
            val f1 = RingFraction.of(1, 3, intRing)
            val f2 = RingFraction.of(1, 3, intRing)
            val result = f1 - f2
            assertEquals(RingFraction.of(0, 1, intRing), result)
        }
    }

    @Test
    fun divisionOfRingFractions() {
        run {
            val f1 = RingFraction.of(1, 2, intRing)
            val f2 = RingFraction.of(1, 3, intRing)
            val result = f1 / f2
            assertEquals(RingFraction.of(3, 2, intRing), result)
        }
        run {
            val f1 = RingFraction.of(1, 3, intRing)
            val f2 = RingFraction.of(1, 3, intRing)
            val result = f1 / f2
            assertEquals(RingFraction.one(intRing), result)
        }
        run {
            val f1 = RingFraction.of(1, 3, intRing)
            val f2 = RingFraction.of(0, 1, intRing)
            assertThrows(ArithmeticException::class.java) {
                f1 / f2
            }
        }
    }

    @Test
    fun inversionOfRingFraction() {
        run {
            val f = RingFraction.of(3, 4, intRing)
            val result = f.inv()
            assertEquals(RingFraction.of(4, 3, intRing), result)
        }
        run {
            val f = RingFraction.of(0, 1, intRing)
            assertThrows(ArithmeticException::class.java) {
                f.inv()
            }
        }
    }

    @Test
    fun powerOfRingFraction() {
        run {
            val f = RingFraction.of(2, 3, intRing)
            val result = f.pow(2)
            assertEquals(RingFraction.of(4, 9, intRing), result)
        }
        run {
            val f = RingFraction.of(2, 3, intRing)
            val result = f.pow(-2)
            assertEquals(RingFraction.of(9, 4, intRing), result)
        }
        run {
            val f = RingFraction.of(2, 3, intRing)
            val result = f.pow(0)
            assertEquals(RingFraction.of(1, 1, intRing), result)
        }
    }

    @Test
    fun additionOfRingFractionsWithPolyRingInt() {
        run {
            val f1 = RingFraction.of(pInt.of(0, 1), pInt) // x
            val f2 = RingFraction.of(pInt.of(1), pInt) // 1
            val result = f1 + f2 // x + 1
            assertValueEquals(RingFraction.of(pInt.of(1, 1), pInt), result)
        }
        run {
            val f1 = RingFraction.of(pInt.of(-1, 0), pInt.of(1), pInt)
            val f2 = RingFraction.of(pInt.of(1, 0), pInt.of(1), pInt)
            val result = f1 + f2
            assertEquals(RingFraction.zero(pInt), result)
        }
        run {
            // (1+x)/(1+2x)
            val f1 = RingFraction.of(pInt.of(1, 1), pInt.of(1, 2), pInt)
            // (2 + 3x)/(1 + 2x)
            val f2 = RingFraction.of(pInt.of(2, 3), pInt.of(1, 2), pInt)
            // (3 + 4x)/(1 + 2x)
            val result = f1 + f2
            assertValueEquals(RingFraction.of(pInt.of(3, 4), pInt.of(1, 2), pInt), result)
        }
    }

    @Test
    fun subtractionOfRingFractionsWithPolyRingInt() {
        run {
            val f1 = RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt)
            val f2 = RingFraction.of(pInt.of(1), pInt.of(1), pInt)
            val result = f1 - f2
            assertEquals(RingFraction.of(pInt.of(0, 1), pInt.of(1), pInt), result)
        }
        run {
            val f1 = RingFraction.of(pInt.of(1, 0), pInt.of(1), pInt)
            val f2 = RingFraction.of(pInt.of(1, 0), pInt.of(1), pInt)
            val result = f1 - f2
            assertEquals(RingFraction.of(pInt.of(0), pInt.of(1), pInt), result)
        }
        run {
            // (1+x)/(1+2x)
            val f1 = RingFraction.of(pInt.of(1, 1), pInt.of(1, 2), pInt)
            // (2 + 3x)/(1 + 2x)
            val f2 = RingFraction.of(pInt.of(2, 3), pInt.of(1, 2), pInt)
            // (-1 + -2x)/(1 + 2x)
            val result = f1 - f2
            assertValueEquals(RingFraction.of(pInt.of(-1, -2), pInt.of(1, 2), pInt), result)
        }
    }

    @Test
    fun multiplicationOfRingFractionsWithPolyRingInt() {
        run {
            val f1 = RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt)
            val f2 = RingFraction.of(pInt.of(1), pInt.of(1), pInt)
            val result = f1 * f2
            assertEquals(RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt), result)
        }
        run {
            val f1 = RingFraction.of(pInt.of(-1, 0), pInt.of(1), pInt)
            val f2 = RingFraction.of(pInt.of(1, 0), pInt.of(1), pInt)
            val result = f1 * f2
            assertEquals(RingFraction.of(pInt.of(-1, 0), pInt.of(1), pInt), result)
        }

        run {
            val n1 = pInt.of(1, 1)
            val d1 = pInt.of(1, 2)
            val n2 = pInt.of(2, 3)
            val d2 = pInt.of(1, 2)
            // (1+x)/(1+2x)
            val f1 = RingFraction.of(n1, d1, pInt)
            // (2 + 3x)/(1 + 2x)
            val f2 = RingFraction.of(n2, d2, pInt)
            assertValueEquals(RingFraction.of(n1 * n2, d1 * d2, pInt), f1 * f2)
        }
    }

    @Test
    fun divisionOfRingFractionsWithPolyRingInt() {
        run {
            val f1 = RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt)
            val f2 = RingFraction.of(pInt.of(1), pInt.of(1), pInt)
            val result = f1 / f2
            assertEquals(RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt), result)
        }
        run {
            val f1 = RingFraction.of(pInt.of(1, 0), pInt.of(1), pInt)
            val f2 = RingFraction.of(pInt.of(0), pInt.of(1), pInt)
            assertThrows(ArithmeticException::class.java) {
                f1 / f2
            }
        }
    }

    @Test
    fun inversionOfRingFractionWithPolyRingInt() {
        run {
            val f = RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt)
            val result = f.inv()
            assertEquals(RingFraction.of(pInt.of(1), pInt.of(1, 1), pInt), result)
        }
        run {
            val f = RingFraction.of(pInt.of(0), pInt.of(1), pInt)
            assertThrows(ArithmeticException::class.java) {
                f.inv()
            }
        }
    }

    @Test
    fun powerOfRingFractionWithPolyRingInt() {
        run {
            val f = RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt)
            val result = f.pow(2)
            assertEquals(RingFraction.of(pInt.of(1, 2, 1), pInt.of(1), pInt), result)
        }
        run {
            val f = RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt)
            val result = f.pow(-2)
            assertEquals(RingFraction.of(pInt.of(1), pInt.of(1, 2, 1), pInt), result)
        }
        run {
            val f = RingFraction.of(pInt.of(1, 1), pInt.of(1), pInt)
            val result = f.pow(0)
            assertEquals(RingFraction.of(pInt.of(1), pInt.of(1), pInt), result)
        }
    }

}
