package model

import io.github.ezrnest.model.Models
import io.github.ezrnest.model.Polynomial
import io.github.ezrnest.model.RFraction
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RFractionTest {
    val ints = Models.ints()
    val qInt = RFraction.over(ints)

    val polyInt = Polynomial.over(ints)
    val qPoly = RFraction.over(polyInt)


    @Test
    fun testEquals() {
        with(qInt) {
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
        with(qInt) {
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
        with(qInt) {
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
        with(qInt) {
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
        with(qInt) {
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
        with(qInt) {
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
        with(qInt) {
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
        with(qPoly) {
            with(polyInt) {
                val f1 = frac(x, 1.p)
                val f2 = frac(1.p, 1.p)
                val result = f1 + f2
                assertEquals(frac(1.p + x, 1.p), result)
            }
            run {
                val f1 = frac(polyInt.poly(-1, 0), polyInt.poly(1))
                val f2 = frac(polyInt.poly(1, 0), polyInt.poly(1))
                val result = f1 + f2
                assertEquals(zero, result)
            }
            run {
                // (1+x)/(1+2x)
                val f1 = frac(polyInt.poly(1, 1), polyInt.poly(1, 2))
                // (2 + 3x)/(1 + 2x)
                val f2 = frac(polyInt.poly(2, 3), polyInt.poly(1, 2))
                // (3 + 4x)/(1 + 2x)
                val result = f1 + f2
                assertTrue { isEqual(frac(polyInt.poly(3, 4), polyInt.poly(1, 2)), result) }
            }
        }
        with(polyInt) {
            with(qPoly) {
                val f1 = (x + 1) / (1 + 2 * x)
                val f2 = (2 + 3 * x) / (1 + 2 * x)
                val result = f1 + f2
                assertTrue { isEqual(frac(3 + 4 * x, 1 + 2 * x), result) }
            }
        }
    }

    @Test
    fun subtractionOfRingFractionsWithPolyRingInt() {
        with(qPoly) {
            run {
                val f1 = polyInt.poly(1, 1) / polyInt.poly(1)
                val f2 = polyInt.poly(1) / polyInt.poly(1)
                val result = f1 - f2
                assertEquals(frac(polyInt.poly(0, 1), polyInt.poly(1)), result)
            }
            run {
                val f1 = frac(polyInt.poly(1, 0), polyInt.poly(1))
                val f2 = frac(polyInt.poly(1, 0), polyInt.poly(1))
                val result = f1 - f2
                assertEquals(frac(polyInt.poly(0), polyInt.poly(1)), result)
            }
            run {
                // (1+x)/(1+2x)
                val f1 = polyInt.poly(1, 1) / polyInt.poly(1, 2)
                // (2 + 3x)/(1 + 2x)
                val f2 = polyInt.poly(2, 3) / polyInt.poly(1, 2)
                // (-1 + -2x)/(1 + 2x)
                val result = f1 - f2
                assertTrue { isEqual(frac(polyInt.poly(-1, -2), polyInt.poly(1, 2)), result) }
            }
        }
    }

    @Test
    fun multiplicationOfRingFractionsWithPolyRingInt() {
        with(polyInt) {
            with(qPoly) {
                val n1 = x + 1
                val d1 = 2.x + 1
                val n2 = 3.x + 2
                val d2 = 2.x + 1
                val f1 = frac(n1, d1)
                val f2 = frac(n2, d2)
                assertTrue { isEqual(frac(n1 * n2, d1 * d2), f1 * f2) }

            }
        }
        with(qPoly) {
            run {
                val f1 = frac(polyInt.poly(1, 1), polyInt.poly(1))
                val f2 = frac(polyInt.poly(1), polyInt.poly(1))
                val result = f1 * f2
                assertEquals(frac(polyInt.poly(1, 1), polyInt.poly(1)), result)
            }
            run {
                val f1 = frac(polyInt.poly(-1, 0), polyInt.poly(1))
                val f2 = frac(polyInt.poly(1, 0), polyInt.poly(1))
                val result = f1 * f2
                assertEquals(frac(polyInt.poly(-1, 0), polyInt.poly(1)), result)
            }
        }
    }

    @Test
    fun divisionOfRingFractionsWithPolyRingInt() {
        with(qPoly) {
            run {
                val f1 = frac(polyInt.poly(1, 1), polyInt.poly(1))
                val f2 = frac(polyInt.poly(1), polyInt.poly(1))
                val result = f1 / f2
                assertEquals(frac(polyInt.poly(1, 1), polyInt.poly(1)), result)
            }
            run {
                val f1 = frac(polyInt.poly(1, 0), polyInt.poly(1))
                val f2 = frac(polyInt.poly(0), polyInt.poly(1))
                assertThrows(ArithmeticException::class.java) {
                    f1 / f2
                }
            }
        }
    }

    @Test
    fun inversionOfRingFractionWithPolyRingInt() {
        with(polyInt) {
            with(qPoly) {
                val f = (x + 1).f
                val result = f.inv()
                assertEquals(frac(1.p, 1.p + x), result)
            }
            with(qPoly) {
                val f = frac(0.p,1.p)
                assertThrows(ArithmeticException::class.java) {
                    f.inv()
                }
            }
        }
    }

    @Test
    fun powerOfRingFractionWithPolyRingInt() {
        with(polyInt) {
            with(qPoly) {
                val f = (x + 1).f
                val result = f.pow(2)
                assertEquals(frac(1.p + 2 * x + x2, 1.p), result)
            }
            with(qPoly) {
                val f = (x + 1).f
                val result = f.pow(-2)
                assertEquals(frac(1.p, 1.p + 2 * x + x2), result)
            }

            with(qPoly) {
                val f = (x + 1).f
                val result = f.pow(0)
                assertEquals(one, result)
            }

        }
    }

}


