package model

import cn.mathsymk.model.NumberModels
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberModelsTest {

    @Test
    fun testDoubleAsReals() {
        val real = NumberModels.DoubleAsReals()
        assert(real.contains(1.0))
        assertEquals(0.0, real.zero)
        assertEquals(2.0, real.add(1.0, 1.0))
        assertEquals(-1.0, real.negate(1.0))
        assertEquals(1.0 - 1.0, real.subtract(1.0, 1.0))
        assertEquals(2.0 - 1.0, real.subtract(2.0, 1.0))
    }


    @Test
    fun testIntAsIntegers() {
        val int = NumberModels.IntAsIntegers
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
        run{
            val intMod97 = NumberModels.intModP(97)
            assertEquals(3, intMod97.add(1,2))
            assertEquals(96, intMod97.subtract(1,2))
            assertEquals(2, intMod97.multiply(1,2))
            assertEquals(49, intMod97.reciprocal(2))
            assertEquals(49, intMod97.divide(98, 2))
            assertThrows<ArithmeticException> { intMod97.divide(98, 0) }
        }
    }

}