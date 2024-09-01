package model

import cn.mathsymk.model.NumberModels
import cn.mathsymk.model.Polynomial
import cn.mathsymk.model.RFraction
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RFractionTest {
    val intRing = NumberModels.IntAsIntegers
    val fInt = RFraction.asField(intRing)

    val pInt = Polynomial.from(intRing)
    val quotientFieldRx = RFraction.asField(pInt)


    @Test
    fun testEquals() {
        with(fInt) {
            run {
                val f1 = frac(3, 4)
                val f2 = frac(3, 4)
                assertEquals(f1, f2)
            }

            run {
                val f1 = frac(2, 4)
                val f2 = frac(1, 2)
                assertEquals(f1, f2)
            }
        }
    }

    @Test
    fun testMultiply() {
        with(fInt) {
            run {
                val f1 = frac(3, 4)
                val f2 = frac(1, 2)
                val result = f1 * f2
                assertEquals(frac(3, 8), result)
            }
            run {
                val f1 = frac(-3, -5)
                val f2 = frac(2, 3)
                val result = f1 * f2
                assertTrue { isEqual(frac(6, 15), result) }
                assertEquals(frac(-2, -5), result)
            }
        }
    }

    @Test
    fun additionOfRingFractions() {
        with(fInt) {
            run {
                val f1 = frac(1, 2)
                val f2 = frac(1, 3)
                val result = f1 + f2
                assertEquals(frac(5, 6), result)
            }
            run {
                val f1 = frac(-1, 3)
                val f2 = frac(1, 3)
                val result = f1 + f2
                assertEquals(frac(0, 1), result)
            }
        }
    }

    @Test
    fun subtractionOfRingFractions() {
        with(fInt) {
            run {
                val f1 = frac(1, 2)
                val f2 = frac(1, 3)
                val result = f1 - f2
                assertEquals(frac(1, 6), result)
            }
            run {
                val f1 = frac(1, 3)
                val f2 = frac(1, 3)
                val result = f1 - f2
                assertEquals(frac(0, 1), result)
            }
        }
    }


    @Test
    fun divisionOfRingFractions() {
        with(fInt) {
            run {
                val f1 = frac(1, 2)
                val f2 = frac(1, 3)
                val result = f1 / f2
                assertEquals(frac(3, 2), result)
            }
            run {
                val f1 = frac(1, 3)
                val f2 = frac(1, 3)
                val result = f1 / f2
                assertEquals(one, result)
            }
            run {
                val f1 = frac(1, 3)
                val f2 = frac(0, 1)
                assertThrows(ArithmeticException::class.java) {
                    f1 / f2
                }
            }
        }
    }

    @Test
    fun inversionOfRingFraction() {
        with(fInt) {
            run {
                val f = frac(3, 4)
                val result = f.inv()
                assertEquals(frac(4, 3), result)
            }
            run {
                val f = frac(0, 1)
                assertThrows(ArithmeticException::class.java) {
                    f.inv()
                }
            }
        }
    }

    @Test
    fun powerOfRingFraction() {
        with(fInt) {
            run {
                val f = frac(2, 3)
                val result = f.pow(2)
                assertEquals(frac(4, 9), result)
            }
            run {
                val f = frac(2, 3)
                val result = f.pow(-2)
                assertEquals(frac(9, 4), result)
            }
            run {
                val f = frac(2, 3)
                val result = f.pow(0)
                assertEquals(frac(1, 1), result)
            }
        }
    }

    @Test
    fun additionOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            with(pInt) {
                val f1 = frac(x, 1.p)
                val f2 = frac(1.p, 1.p)
                val result = f1 + f2
                assertEquals(frac(1.p + x, 1.p), result)
            }
            run {
                val f1 = frac(pInt.poly(-1, 0), pInt.poly(1))
                val f2 = frac(pInt.poly(1, 0), pInt.poly(1))
                val result = f1 + f2
                assertEquals(zero, result)
            }
            run {
                // (1+x)/(1+2x)
                val f1 = frac(pInt.poly(1, 1), pInt.poly(1, 2))
                // (2 + 3x)/(1 + 2x)
                val f2 = frac(pInt.poly(2, 3), pInt.poly(1, 2))
                // (3 + 4x)/(1 + 2x)
                val result = f1 + f2
                assertTrue { isEqual(frac(pInt.poly(3, 4), pInt.poly(1, 2)), result) }
            }
        }
        with(pInt) {
            with(quotientFieldRx) {
                val f1 = (x + 1) / (1 + 2 * x)
                val f2 = (2 + 3 * x) / (1 + 2 * x)
                val result = f1 + f2
                assertTrue { isEqual(frac(3 + 4 * x, 1 + 2 * x), result) }
            }
        }
    }

    @Test
    fun subtractionOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f1 = pInt.poly(1, 1) / pInt.poly(1)
                val f2 = pInt.poly(1) / pInt.poly(1)
                val result = f1 - f2
                assertEquals(frac(pInt.poly(0, 1), pInt.poly(1)), result)
            }
            run {
                val f1 = frac(pInt.poly(1, 0), pInt.poly(1))
                val f2 = frac(pInt.poly(1, 0), pInt.poly(1))
                val result = f1 - f2
                assertEquals(frac(pInt.poly(0), pInt.poly(1)), result)
            }
            run {
                // (1+x)/(1+2x)
                val f1 = pInt.poly(1, 1) / pInt.poly(1, 2)
                // (2 + 3x)/(1 + 2x)
                val f2 = pInt.poly(2, 3) / pInt.poly(1, 2)
                // (-1 + -2x)/(1 + 2x)
                val result = f1 - f2
                assertTrue { isEqual(frac(pInt.poly(-1, -2), pInt.poly(1, 2)), result) }
            }
        }
    }

    @Test
    fun multiplicationOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f1 = frac(pInt.poly(1, 1), pInt.poly(1))
                val f2 = frac(pInt.poly(1), pInt.poly(1))
                val result = f1 * f2
                assertEquals(frac(pInt.poly(1, 1), pInt.poly(1)), result)
            }
            run {
                val f1 = frac(pInt.poly(-1, 0), pInt.poly(1))
                val f2 = frac(pInt.poly(1, 0), pInt.poly(1))
                val result = f1 * f2
                assertEquals(frac(pInt.poly(-1, 0), pInt.poly(1)), result)
            }
            run {
                val n1 = pInt.poly(1, 1)
                val d1 = pInt.poly(1, 2)
                val n2 = pInt.poly(2, 3)
                val d2 = pInt.poly(1, 2)
                // (1+x)/(1+2x)
                val f1 = frac(n1, d1)
                // (2 + 3x)/(1 + 2x)
                val f2 = frac(n2, d2)
                assertTrue { isEqual(frac(n1 * n2, d1 * d2), f1 * f2) }
            }
        }
    }

    @Test
    fun divisionOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f1 = frac(pInt.poly(1, 1), pInt.poly(1))
                val f2 = frac(pInt.poly(1), pInt.poly(1))
                val result = f1 / f2
                assertEquals(frac(pInt.poly(1, 1), pInt.poly(1)), result)
            }
            run {
                val f1 = frac(pInt.poly(1, 0), pInt.poly(1))
                val f2 = frac(pInt.poly(0), pInt.poly(1))
                assertThrows(ArithmeticException::class.java) {
                    f1 / f2
                }
            }
        }
    }

    @Test
    fun inversionOfRingFractionWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f = frac(pInt.poly(1, 1), pInt.poly(1))
                val result = f.inv()
                assertEquals(frac(pInt.poly(1), pInt.poly(1, 1)), result)
            }
            run {
                val f = frac(pInt.poly(0), pInt.poly(1))
                assertThrows(ArithmeticException::class.java) {
                    f.inv()
                }
            }
        }
    }

    @Test
    fun powerOfRingFractionWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f = frac(pInt.poly(1, 1), pInt.poly(1))
                val result = f.pow(2)
                assertEquals(frac(pInt.poly(1, 2, 1), pInt.poly(1)), result)
            }
            run {
                val f = frac(pInt.poly(1, 1), pInt.poly(1))
                val result = f.pow(-2)
                assertEquals(frac(pInt.poly(1), pInt.poly(1, 2, 1)), result)
            }
            run {
                val f = frac(pInt.poly(1, 1), pInt.poly(1))
                val result = f.pow(0)
                assertEquals(frac(pInt.poly(1), pInt.poly(1)), result)
            }
        }
    }

}


fun main() {
    val ints = NumberModels.IntAsIntegers
    val polyRing = Polynomial.from(ints)
    val qField = RFraction.asField(polyRing)
    with(polyRing) { // polynomial ring over integers, Z[x]
        with(qField) { // quotient field of polynomials over integers, Z(x)
            val f1 = (1 + x) / (1 + 2.x)
            val f2 = (2 + 3.x) / (1 + 2.x)
            println(f1 + f2) // (3 + 4x)/(1 + 2x)
            println(f1 / f2) // (1 + x)/(2 + 3x)

            val f3 = x / (1 + 2.x)
            println(f1 + f3) // 1

            val f4 = (1 + x) / (1 + 3.x)
            println(f1 + f4) // (5*x^2 + 7*x + 2)/(6*x^2 + 5*x + 1)
        }
    }
}