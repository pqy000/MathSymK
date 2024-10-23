package linear

import TestUtils.assertEquals
import io.github.ezrnest.mathsymk.linear.T
import io.github.ezrnest.mathsymk.linear.Vector
import io.github.ezrnest.mathsymk.linear.Vector.Companion.vec
import io.github.ezrnest.mathsymk.model.Models
import kotlin.test.Test
import kotlin.test.assertEquals

class VectorTest {
    val ints = Models.ints()
    val reals = Models.doubles(1E-7)
    val R3 = Vector.over(reals, 3)
    val Z3 = Vector.over(ints, 3)

    @Test
    fun applyAll_appliesFunctionToAllElements() {
        val vector = Vector.of(listOf(1, 2, 3))
        val result = vector.map { it * 2 }
        assertEquals(listOf(2, 4, 6), result.toList())
    }

    @Test
    fun valueEquals_returnsTrueForEqualVectors() {
        val vector1 = Vector.of(listOf(1, 2, 3))
        val vector2 = Vector.of(listOf(1, 2, 3))
        with(Z3) {
            assertEquals(vector1, vector2)
        }

    }

    @Test
    fun plus_addsTwoVectors() {
        with(Z3) {
            val v1 = Vector(1, 2, 3)
            val v2 = Vector(4, 5, 6)
            val result = v1 + v2
            assertEquals(Vector(5, 7, 9), result)
        }
    }

    @Test
    fun times_multipliesVectorByScalar() {
        with(Z3) {
            val vector = Vector(1, 2, 3)
            val result = vector * 2
            assertEquals(Vector(2, 4, 6), result)
        }
    }

    @Test
    fun inner_calculatesInnerProduct() {
        with(Z3) {
            val vector1 = Vector(1, 2, 3)
            val vector2 = Vector(4, 5, 6)
            val result = vector1 dot vector2
            assertEquals(32, result)
        }
    }

    @Test
    fun norm_calculatesNorm() {
        with(Z3) {
            val vector = Vector(3, 4)
            val result = vector.normSq()
            assertEquals(25, result)
        }
    }

    @Test
    fun unitize_returnsUnitVector() {
        with(R3) {
            val v = vec(3.0, 4.0, 5.0)
            val result = v.unitize()
            assertEquals(vec(0.4242640687119285, 0.565685424949238, 0.7071067811865475), result)
        }

        with(R3) {
            val vector = vec(3.0, 4.0, 0.0)
            val result = vector.unitize()
            assertEquals(vec(0.6, 0.8, 0.0), result)
        }
    }

    @Test
    fun div_dividesVectorByScalar() {
        with(R3) {
            val v = vec(1.0, 2.0, 3.0)
            val result = v / 2.0
            assertEquals(vec(0.5, 1.0, 1.5), result)
        }
    }

    @Test
    fun odot_calculatesHadamardProduct() {
        with(Z3) {
            val vector1 = Vector(1, 2, 3)
            val vector2 = Vector(4, 5, 6)
            val result = vector1 odot vector2
            assertEquals(Vector(4, 10, 18), result)
        }
    }

    @Test
    fun testVectorSpace() {
        with(R3) {
            val v1 = vec(1.0, 2.0, 3.0)
            val v2 = vec(4.0, 5.0, 6.0)
            assertEquals(vec(5.0, 7.0, 9.0), v1 + v2)
        }
    }

    @Test
    fun testRowVector() {
        with(R3) {
            val v = vec(1.0, 2.0, 3.0)
            assertEquals(inner(v, v), v.T * v)
        }
    }
}