package linear

import TestUtils.assertEquals
import cn.mathsymk.linear.Vector
import cn.mathsymk.model.NumberModels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VectorTest {
    val ints = NumberModels.intAsIntegers()
    val reals = NumberModels.doubleAsReals(1E-7)
    val vectors3 = Vector.space(reals, 3)

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
        with(vectors3) {
            val v = vec(3.0, 4.0, 5.0)
            val result = v.unitize()
            assertEquals(vec(0.4242640687119285, 0.565685424949238, 0.7071067811865475), result)
        }

        with(vectors3) {
            val vector = vec(3.0, 4.0)
            val result = vector.unitize()
            assertEquals(vec(0.6, 0.8), result)
        }
    }

    @Test
    fun div_dividesVectorByScalar() {
        with(vectors3) {
            val v = vec(1.0, 2.0, 3.0)
            val result = v / 2.0
            assertEquals(vec(0.5, 1.0, 1.5), result)
        }
    }

    @Test
    fun odot_calculatesHadamardProduct() {
        val vector1 = Vector.of(listOf(1, 2, 3), ints)
        val vector2 = Vector.of(listOf(4, 5, 6), ints)
        val result = vector1 odot vector2
        assertEquals(listOf(4, 10, 18), result.toList())
    }

    @Test
    fun testVectorSpace() {
        with(vectors3) {
            val v1 = vec(1.0, 2.0, 3.0)
            val v2 = vec(4.0, 5.0, 6.0)
            assertEquals(vec(5.0, 7.0, 9.0), v1 + v2)
        }
    }
}