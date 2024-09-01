package model

import TestUtils.assertValueEquals
import cn.mathsymk.model.Fraction
import cn.mathsymk.model.NumberModels
import cn.mathsymk.model.Polynomial
import cn.mathsymk.model.RingFraction
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RingFractionTest {
    val intRing = NumberModels.IntAsIntegers
    val fInt = RingFraction.asField(intRing)

    val pInt = Polynomial.from(intRing)
    val quotientFieldRx = RingFraction.asField(pInt)


    @Test
    fun testEquals() {
        with(fInt) {
            run {
                val f1 = of(3, 4)
                val f2 = of(3, 4)
                assertEquals(f1, f2)
            }

            run {
                val f1 = of(2, 4)
                val f2 = of(1, 2)
                assertEquals(f1, f2)
            }
        }
    }

    @Test
    fun testMultiply() {
        with(fInt) {
            run {
                val f1 = of(3, 4)
                val f2 = of(1, 2)
                val result = f1 * f2
                assertEquals(of(3, 8), result)
            }
            run {
                val f1 = of(-3, -5)
                val f2 = of(2, 3)
                val result = f1 * f2
                assertTrue { isEqual(of(6, 15), result) }
                assertEquals(of(-2, -5), result)
            }
        }
    }

    @Test
    fun additionOfRingFractions() {
        with(fInt) {
            run {
                val f1 = of(1, 2)
                val f2 = of(1, 3)
                val result = f1 + f2
                assertEquals(of(5, 6), result)
            }
            run {
                val f1 = of(-1, 3)
                val f2 = of(1, 3)
                val result = f1 + f2
                assertEquals(of(0, 1), result)
            }
        }
    }

    @Test
    fun subtractionOfRingFractions() {
        with(fInt) {
            run {
                val f1 = of(1, 2)
                val f2 = of(1, 3)
                val result = f1 - f2
                assertEquals(of(1, 6), result)
            }
            run {
                val f1 = of(1, 3)
                val f2 = of(1, 3)
                val result = f1 - f2
                assertEquals(of(0, 1), result)
            }
        }
    }


    @Test
    fun divisionOfRingFractions() {
        with(fInt) {
            run {
                val f1 = of(1, 2)
                val f2 = of(1, 3)
                val result = f1 / f2
                assertEquals(of(3, 2), result)
            }
            run {
                val f1 = of(1, 3)
                val f2 = of(1, 3)
                val result = f1 / f2
                assertEquals(one, result)
            }
            run {
                val f1 = of(1, 3)
                val f2 = of(0, 1)
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
                val f = of(3, 4)
                val result = f.inv()
                assertEquals(of(4, 3), result)
            }
            run {
                val f = of(0, 1)
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
                val f = of(2, 3)
                val result = f.pow(2)
                assertEquals(of(4, 9), result)
            }
            run {
                val f = of(2, 3)
                val result = f.pow(-2)
                assertEquals(of(9, 4), result)
            }
            run {
                val f = of(2, 3)
                val result = f.pow(0)
                assertEquals(of(1, 1), result)
            }
        }
    }

    @Test
    fun additionOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f1 = of(pInt.of(0, 1), pInt.one) // x
                val f2 = of(pInt.of(1), pInt.one) // 1
                val result = f1 + f2 // x + 1
                assertTrue { isEqual(of(pInt.of(1, 1), pInt.one), result) }
            }
            run {
                val f1 = of(pInt.of(-1, 0), pInt.of(1))
                val f2 = of(pInt.of(1, 0), pInt.of(1))
                val result = f1 + f2
                assertEquals(zero, result)
            }
            run {
                // (1+x)/(1+2x)
                val f1 = of(pInt.of(1, 1), pInt.of(1, 2))
                // (2 + 3x)/(1 + 2x)
                val f2 = of(pInt.of(2, 3), pInt.of(1, 2))
                // (3 + 4x)/(1 + 2x)
                val result = f1 + f2
                assertTrue { isEqual(of(pInt.of(3, 4), pInt.of(1, 2)), result) }
            }
        }
    }

    @Test
    fun subtractionOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f1 = of(pInt.of(1, 1), pInt.of(1))
                val f2 = of(pInt.of(1), pInt.of(1))
                val result = f1 - f2
                assertEquals(of(pInt.of(0, 1), pInt.of(1)), result)
            }
            run {
                val f1 = of(pInt.of(1, 0), pInt.of(1))
                val f2 = of(pInt.of(1, 0), pInt.of(1))
                val result = f1 - f2
                assertEquals(of(pInt.of(0), pInt.of(1)), result)
            }
            run {
                // (1+x)/(1+2x)
                val f1 = of(pInt.of(1, 1), pInt.of(1, 2))
                // (2 + 3x)/(1 + 2x)
                val f2 = of(pInt.of(2, 3), pInt.of(1, 2))
                // (-1 + -2x)/(1 + 2x)
                val result = f1 - f2
                assertTrue { isEqual(of(pInt.of(-1, -2), pInt.of(1, 2)), result) }
            }
        }
    }

    @Test
    fun multiplicationOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f1 = of(pInt.of(1, 1), pInt.of(1))
                val f2 = of(pInt.of(1), pInt.of(1))
                val result = f1 * f2
                assertEquals(of(pInt.of(1, 1), pInt.of(1)), result)
            }
            run {
                val f1 = of(pInt.of(-1, 0), pInt.of(1))
                val f2 = of(pInt.of(1, 0), pInt.of(1))
                val result = f1 * f2
                assertEquals(of(pInt.of(-1, 0), pInt.of(1)), result)
            }
            run {
                val n1 = pInt.of(1, 1)
                val d1 = pInt.of(1, 2)
                val n2 = pInt.of(2, 3)
                val d2 = pInt.of(1, 2)
                // (1+x)/(1+2x)
                val f1 = of(n1, d1)
                // (2 + 3x)/(1 + 2x)
                val f2 = of(n2, d2)
                assertTrue { isEqual(of(n1 * n2, d1 * d2), f1 * f2) }
            }
        }
    }

    @Test
    fun divisionOfRingFractionsWithPolyRingInt() {
        with(quotientFieldRx) {
            run {
                val f1 = of(pInt.of(1, 1), pInt.of(1))
                val f2 = of(pInt.of(1), pInt.of(1))
                val result = f1 / f2
                assertEquals(of(pInt.of(1, 1), pInt.of(1)), result)
            }
            run {
                val f1 = of(pInt.of(1, 0), pInt.of(1))
                val f2 = of(pInt.of(0), pInt.of(1))
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
                val f = of(pInt.of(1, 1), pInt.of(1))
                val result = f.inv()
                assertEquals(of(pInt.of(1), pInt.of(1, 1)), result)
            }
            run {
                val f = of(pInt.of(0), pInt.of(1))
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
                val f = of(pInt.of(1, 1), pInt.of(1))
                val result = f.pow(2)
                assertEquals(of(pInt.of(1, 2, 1), pInt.of(1)), result)
            }
            run {
                val f = of(pInt.of(1, 1), pInt.of(1))
                val result = f.pow(-2)
                assertEquals(of(pInt.of(1), pInt.of(1, 2, 1)), result)
            }
            run {
                val f = of(pInt.of(1, 1), pInt.of(1))
                val result = f.pow(0)
                assertEquals(of(pInt.of(1), pInt.of(1)), result)
            }
        }
    }

}
