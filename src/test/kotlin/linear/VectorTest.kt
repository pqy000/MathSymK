package linear

import cn.mathsymk.linear.Vector
import cn.mathsymk.model.NumberModels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VectorTest {
    val ints = NumberModels.intAsIntegers()
    val reals = NumberModels.doubleAsReals(1E-7)

    @Test
    fun applyAll_appliesFunctionToAllElements() {
        val vector = Vector.of(listOf(1, 2, 3), ints)
        val result = vector.applyAll { it * 2 }
        assertEquals(listOf(2, 4, 6), result.toList())
    }

    @Test
    fun valueEquals_returnsTrueForEqualVectors() {
        val vector1 = Vector.of(listOf(1, 2, 3), ints)
        val vector2 = Vector.of(listOf(1, 2, 3), ints)
        assertTrue(vector1.valueEquals(vector2))
    }

    @Test
    fun plus_addsTwoVectors() {
        val vector1 = Vector.of(listOf(1, 2, 3), ints)
        val vector2 = Vector.of(listOf(4, 5, 6), ints)
        val result = vector1.plus(vector2)
        assertEquals(listOf(5, 7, 9), result.toList())
    }

    @Test
    fun times_multipliesVectorByScalar() {
        val vector = Vector.of(listOf(1, 2, 3), ints)
        val result = vector.times(2)
        assertEquals(listOf(2, 4, 6), result.toList())
    }

    @Test
    fun inner_calculatesInnerProduct() {
        val vector1 = Vector.of(listOf(1, 2, 3), ints)
        val vector2 = Vector.of(listOf(4, 5, 6), ints)
        val result = vector1 inner vector2
        assertEquals(32, result)
    }

    @Test
    fun norm_calculatesNorm() {
        val vector = Vector.of(listOf(3, 4), ints)
        val result = vector.normSq()
        assertEquals(25, result)
    }

    @Test
    fun unitize_returnsUnitVector() {
        val vector = Vector.of(listOf(3.0, 4.0), reals)
        val result = vector.unitize()
        assertEquals(listOf(0.6, 0.8), result.toList())
    }
}