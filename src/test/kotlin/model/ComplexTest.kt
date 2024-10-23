package model

import io.github.ezrnest.mathsymk.model.Complex
import io.github.ezrnest.mathsymk.model.Fraction
import io.github.ezrnest.mathsymk.model.Models
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplexTest {

    val ℚ = Fraction.model
    val ℂ = Complex.over(ℚ)

    val intP = Models.intModP(97)
    val complex1 = Complex.over(intP)

    @Test
    fun testAdd() {
        with(complex1) {
            val a = complexOf(1, 2)
            val b = complexOf(3, 4)
            val c = a + b
            assertEquals(complexOf(4, 6), c)
        }
    }

    @Test
    fun testSubtract() {
        with(complex1) {
            val a = complexOf(1, 2)
            val b = complexOf(3, 4)
            val c = a - b
            assertTrue(isEqual(complexOf(-2, -2), c))
        }
    }

    @Test
    fun additionWithZero() {
        with(complex1) {
            val a = complexOf(1, 2)
            val zero = complexOf(0, 0)
            val result = a + zero
            assertEquals(a, result)
        }
    }

    @Test
    fun subtractionWithZero() {
        with(complex1) {
            val a = complexOf(1, 2)
            val zero = complexOf(0, 0)
            val result = a - zero
            assertEquals(a, result)
        }
    }

    @Test
    fun multiplicationWithZero() {
        with(complex1) {
            val a = complexOf(1, 2)
            val zero = complexOf(0, 0)
            val result = a * zero
            assertEquals(zero, result)
        }
    }

    @Test
    fun divisionByOne() {
        with(complex1) {
            val a = complexOf(1, 2)
            val one = complexOf(1, 0)
            val result = a / one
            assertEquals(a, result)
        }
    }

    @Test
    fun divisionByZeroThrowsException() {
        with(complex1) {
            val a = complexOf(1, 2)
            val zero = complexOf(0, 0)
            assertThrows<ArithmeticException> {
                println(a / zero)
            }
        }
    }

    @Test
    fun conjugateOfComplexNumber() {
        with(complex1) {
            val a = complexOf(1, 2)
            val conjugate = a.conj
            assertTrue { isEqual(complexOf(1, -2), conjugate) }
        }
    }

    @Test
    fun modulusOfComplexNumber() {
        with(complex1) {
            val a = complexOf(3, 4)
            val modulus = a.modSq
            assertEquals(25, modulus)
        }
    }

//    @Test
//    fun argumentOfComplexNumber() {
//        with(complex1) {
//            val a = complexOf(1, 1)
//            val argument = a.arg
//            assertEquals(Math.PI / 4, argument)
//        }
//    }
}

//fun main() {
//    val l = listcomplexOf(0,0)
//    val a : Int = l[0]
//    val b : Int = l[1]
//    println(l[0] / l[1])
//}