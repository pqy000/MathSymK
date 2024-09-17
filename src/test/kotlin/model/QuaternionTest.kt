package model

import TestUtils.assertEquals
import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.model.Quaternion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuaternionTest {
    val intModP = NumberModels.intModP(97)
    val quaterion = Quaternion.from(intModP)

    @Test
    fun additionOfQuaternions() {
        with(quaterion) {
            val q1 = quat(1, 2, 3, 4)
            val q2 = quat(4, 3, 2, 1)
            val result = q1 + q2
            assertEquals(quat(5, 5, 5, 5), result)
        }
    }

    @Test
    fun multiplicationOfQuaternions() {
        with(quaterion) {
            val q1 = quat(1, 2, 3, 4)
            val q2 = quat(4, 3, 2, 1)
            val result = q1 * q2
            assertTrue { isEqual(quat(-12, 6, 24, 12), result) }
        }
    }

    @Test
    fun scalarMultiplication() {
        val q = quaterion.quat(1, 2, 3, 4)
        val result = quaterion.scalarMul(2, q)
        assertEquals(quaterion.quat(2, 4, 6, 8), result)
    }

    @Test
    fun scalarDivision() {
        val q = quaterion.quat(2, 4, 6, 8)
        val result = quaterion.scalarDiv(q, 2)
        assertEquals(quaterion.quat(1, 2, 3, 4), result)
    }

    @Test
    fun quaternionConjugate() {
        with(quaterion) {
            val q = quat(1, 2, 3, 4)
            val result = q.conj
            assertEquals(quat(1, -2, -3, -4), result)
        }
    }

    @Test
    fun quaternionTensor() {
        with(quaterion) {
            val q = quat(1, 2, 3, 4)
            val result = q.tensor
            assertEquals(30, result)
        }
    }

    @Test
    fun reciprocalOfQuaternion() {
        with(quaterion) {
            val q = quat(1, 2, 3, 4)
            val result = q.inv()
            assertEquals(quat(1, -2, -3, -4) / 30, result)
        }
    }

    @Test
    fun zeroQuaternion() {
        with(quaterion) {
            val q = zero
            assertTrue(isZero(q))
        }
    }

    @Test
    fun containsQuaternion() {
        val q = quaterion.quat(1, 2, 3, 4)
        assertTrue(quaterion.contains(q))
    }
}