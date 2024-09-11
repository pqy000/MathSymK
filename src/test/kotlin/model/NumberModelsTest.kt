package model

import cn.mathsymk.model.IntModN
import cn.mathsymk.model.NumberModels
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberModelsTest {

    @Test
    fun testDoubleAsReals() {
        val real = NumberModels.doubleAsReals()
        assert(real.contains(1.0))
        assertEquals(0.0, real.zero)
        assertEquals(2.0, real.add(1.0, 1.0))
        assertEquals(-1.0, real.negate(1.0))
        assertEquals(1.0 - 1.0, real.subtract(1.0, 1.0))
        assertEquals(2.0 - 1.0, real.subtract(2.0, 1.0))
    }

    @Test
    fun testIntAsIntegers() {
        val int = NumberModels.intAsIntegers()
        assert(int.contains(1))
        assertEquals(0, int.zero)
        assertEquals(2, int.add(1, 1))
        assertEquals(-1, int.negate(1))
        assertEquals(1 - 1, int.subtract(1, 1))
        assertEquals(2 - 1, int.subtract(2, 1))

        // exactDivide
        assertEquals(0, int.mod(3, 1))
        assertEquals(3, int.divideToInteger(3, 1))
        assertEquals(1, int.exactDivide(2, 2))
        assertEquals(-3, int.exactDivide(-3, 1))
    }

    @Test
    fun testIntModP(){
        with(NumberModels.intModP(97)){
            assertEquals(3, add(1,2))
            assertEquals(96, subtract(1,2))
            assertEquals(2, multiply(1,2))
            assertEquals(49, reciprocal(2))
            assertEquals(49, divide(98, 2))
            assertThrows<ArithmeticException> { divide(98, 0) }
        }
    }

    @Test
    fun testBigInteger(){
        with(NumberModels.bigIntegerAsIntegers()) {
            assertEquals(0.toBigInteger(), zero)
            assertEquals(2.toBigInteger(), add(1.toBigInteger(), 1.toBigInteger()))
            assertEquals((-1).toBigInteger(), negate(1.toBigInteger()))
            assertEquals(1.toBigInteger() - 1.toBigInteger(), subtract(1.toBigInteger(), 1.toBigInteger()))
            assertEquals(2.toBigInteger() - 1.toBigInteger(), subtract(2.toBigInteger(), 1.toBigInteger()))
        }
    }

    @Test
    fun testBigFrac(){
        val bigFrac = NumberModels.fractionBig()
        with(bigFrac) {
            val f1 = bfrac(1,2)
            val f2 = bfrac(1,3)
            assertEquals(bfrac(5,6), f1 + f2)
        }
    }

    @Test
    fun testIntModN(){
        with(NumberModels.intModN(97)){
            assertEquals(3, add(1,2))
            assertEquals(96, subtract(1,2))
            assertEquals(2, multiply(1,2))
        }
        with(NumberModels.intModN(Int.MAX_VALUE)){
            assertEquals(3, add(1,2))
            assertEquals(Int.MAX_VALUE - 1, subtract(1,2))
            assertEquals(2, multiply(1,2))
            assertEquals(9,add(Int.MAX_VALUE-1,10))
        }
        with(NumberModels.intModN( Int.MAX_VALUE / 2 - 1)){
            val n = Int.MAX_VALUE / 2L - 1
            assertEquals(3, add(1,2))
            assertEquals(46341L * 46341 % n, multiply(46341, 46341).toLong())
            assertEquals(46340L * 46340 % n, multiply(46340, 46340).toLong())
        }
    }
}