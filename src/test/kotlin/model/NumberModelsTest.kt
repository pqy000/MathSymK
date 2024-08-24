package model

import cn.mathsymk.model.NumberModels
import kotlin.test.Test
import kotlin.test.assertEquals

class NumberModelsTest {

    @Test
    fun testDoubleAsReals(){
        val real = NumberModels.DoubleAsReals()
        assert(real.contains(1.0))
        assertEquals(0.0, real.zero)
        assertEquals(2.0, real.add(1.0, 1.0))
        assertEquals(-1.0, real.negate(1.0))
        assertEquals(1.0-1.0, real.subtract(1.0, 1.0))
        assertEquals(2.0-1.0, real.subtract(2.0, 1.0))
    }




}