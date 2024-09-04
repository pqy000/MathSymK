package model

import cn.mathsymk.model.Complex
import cn.mathsymk.model.Fraction
import cn.mathsymk.model.NumberModels
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplexTest {

    val ℚ = Fraction.asQuotient
    val ℂ = Complex.asField(ℚ)

    val intP = NumberModels.intModP(97)
    val complex1 = Complex.asField(intP)

    @Test
    fun testAdd() {
        with(complex1) {
            val a = of(1, 2)
            val b = of(3, 4)
            val c = a + b
            assertEquals(of(4, 6), c)
        }
    }

    @Test
    fun testSubtract() {
        with(complex1) {
            val a = of(1, 2)
            val b = of(3, 4)
            val c = a - b
            assertTrue(isEqual(of(-2, -2), c))
        }
    }

    @Test
    fun additionWithZero() {
        with(complex1) {
            val a = of(1, 2)
            val zero = of(0, 0)
            val result = a + zero
            assertEquals(a, result)
        }
    }

    @Test
    fun subtractionWithZero() {
        with(complex1) {
            val a = of(1, 2)
            val zero = of(0, 0)
            val result = a - zero
            assertEquals(a, result)
        }
    }

    @Test
    fun multiplicationWithZero() {
        with(complex1) {
            val a = of(1, 2)
            val zero = of(0, 0)
            val result = a * zero
            assertEquals(zero, result)
        }
    }

    @Test
    fun divisionByOne() {
        with(complex1) {
            val a = of(1, 2)
            val one = of(1, 0)
            val result = a / one
            assertEquals(a, result)
        }
    }

    @Test
    fun divisionByZeroThrowsException() {
        with(complex1) {
            val a = of(1, 2)
            val zero = of(0, 0)
            assertThrows<ArithmeticException> {
                println(a / zero)
            }
        }
    }

    @Test
    fun conjugateOfComplexNumber() {
        with(complex1) {
            val a = of(1, 2)
            val conjugate = a.conjugate
            assertTrue { isEqual(of(1, -2), conjugate) }
        }
    }

    @Test
    fun modulusOfComplexNumber() {
        with(complex1) {
            val a = of(3, 4)
            val modulus = a.modSquared
            assertEquals(25, modulus)
        }
    }

//    @Test
//    fun argumentOfComplexNumber() {
//        with(complex1) {
//            val a = of(1, 1)
//            val argument = a.arg
//            assertEquals(Math.PI / 4, argument)
//        }
//    }
}

fun main() {
    val l = listOf(0,0)
    val a : Int = l[0]
    val b : Int = l[1]
    println(l[0] / l[1])
}