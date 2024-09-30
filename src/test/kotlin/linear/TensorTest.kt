package linear

import TestUtils.assertEquals
import io.github.ezrnest.linear.Tensor
import io.github.ezrnest.linear.Tensor.Companion.invoke
import io.github.ezrnest.model.*
import io.github.ezrnest.linear.TensorImpl
import io.github.ezrnest.linear.all
import io.github.ezrnest.linear.get
import io.github.ezrnest.model.NumberModels.fractions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TensorTest {
    val mc = NumberModels.integers()
    val tenZ = Tensor.over(mc)

    @Test
    fun testAdd() {
        with(tenZ) {
            val u = Tensor(2, 8) { it.sum() }
            assertTrue(isZero(u - u))
        }
        with(tenZ) {
            val u = Tensor(2, 8) { it.sum() }
            val v = Tensor(2, 8) { it.sum() }
            val w = u + v
            assertTrue(isZero(w - u - v))
        }

    }

    @Test
    fun testView() {
        with(tenZ) {
            val v = zeros(2, 8)
            assertEquals(2, v.slice(null, 0 downTo -1 step 2).size)
        }

        with(tenZ) {
            val v = zeros(2, 8)
            assertEquals(4, v.slice(0, 0 until 8 step 2).size)
        }

    }

    @Test
    fun testReshape() {
        val u = Tensor.of(2, 3) { it.sum() }
        assertContentEquals(intArrayOf(2, 3), u.shape)
        assertContentEquals(intArrayOf(3, 2), u.reshape(3, 2).shape)
        assertThrows<IllegalArgumentException> { u.reshape(3, 3) }
        assertTrue { u.elementSequence().zip(u.reshape(3, 2).elementSequence()).all { it.first == it.second } }

        val w = Tensor(1) { 1 }
        assertTrue { w.reshape()[intArrayOf()] == 1 }
        assertContentEquals(intArrayOf(), w.squeeze().shape)
        assertContentEquals(intArrayOf(1),w.squeeze().ravel().shape)
    }

    @Test
    fun testPermute() {
        val u = Tensor(2, 3, 4) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        val v = u.permute(1, 2, 0)
        assertArrayEquals(intArrayOf(3, 4, 2), v.shape)
        assertEquals(u[0, 1, 2], v[1, 2, 0])
    }

    @Test
    fun testSet() {
        val u = Tensor(2, 8) { it.sum() }
        u[1, 1] = 3
        assertEquals(3, u[1, 1])
    }

    @Test
    fun testWedge() {
        with(tenZ) {
            val u = Tensor(2, 3) { it.sum() }
            val w = Tensor(3, 2) { it[0] }
            val r = u.wedge(w)
            assertArrayEquals(intArrayOf(2, 3, 3, 2), r.shape)
            assertEquals(0, r[0, 0, 0, 0])
        }
    }

    @Test
    fun testSum() {
        with(tenZ) {
            val t = Tensor.of(intArrayOf(2, 3), 0, 1, 2, 2, 3, 4)
            val re = Tensor.of(intArrayOf(3), 2, 4, 6)

            assertEquals(re, t.sum(0))
        }
    }

    @Test
    fun testEinsum() {

        val shape = intArrayOf(3, 3)
        val shape2 = intArrayOf(3, 3)
//    val v = Tensor.zeros(mc, *shape)
//    val w = Tensor.ones(mc, *shape)
        val u = Tensor(*shape) { it.sum() }
        val w = Tensor(*shape2) { it[0] }
        var r = TensorImpl.einsum(
            listOf(u, w),
            resShape = intArrayOf(3, 3),
            mulShape = intArrayOf(1),
            tToResList = listOf(listOf(0 to 0, 1 to 1), listOf(0 to 0, 1 to 1)),
            tToMulList = listOf(listOf(), listOf()),
            mc
        )//element-wise multiplication
        with(tenZ) {
            assertEquals(r, u * w)
        }

        r = TensorImpl.einsum(
            listOf(u),
            intArrayOf(3),
            intArrayOf(1),
            listOf(listOf(0 to 0, 1 to 0)),
            listOf(listOf()),
            mc
        )//diagonal elements
        with(tenZ) {
            assertEquals(r, Tensor(3) { it[0] * 2 })
        }


        r = TensorImpl.einsum(
            listOf(u),
            intArrayOf(1),
            intArrayOf(3),
            listOf(listOf()),
            listOf(listOf(0 to 0, 1 to 0)),
            mc
        ) // trace
        assertEquals(6, r[0])

        r = TensorImpl.einsum(
            listOf(u, w),
            intArrayOf(3, 3, 3, 3),
            intArrayOf(1),
            listOf(listOf(0 to 0, 1 to 1), listOf(0 to 2, 1 to 3)),
            listOf(listOf(), listOf()),
            mc
        ) // wedge(outer product)
        with(tenZ) {
            assertEquals(r, u.wedge(w))
        }
    }

    @Test
    fun testEinsum2() {

        val u = Tensor(3, 3) { it[0] + 2 * it[1] }
        val w = Tensor(3, 3) { it[0] }

        with(tenZ) {
            assertEquals(9, einsum("ii", u)[0]) // trace
            assertEquals(27, einsum("ij->", u)[0]) // sum
            assertEquals(3, einsum("ii->i", u)[1]) // diagonal
            assertEquals(4, einsum("ij->ji", u)[2, 0]) // transpose

            assertEquals(12, einsum("ij,ij->ij", u, w)[2, 2]) // element-wise multiplication

            assertEquals(13, einsum("ij,jk->ik", u, w)[1, 1]) // matrix multiplication
        }

    }

    @Test
    fun testEinsum3() {
        with(tenZ) {
            val u = Tensor.of(3, 3) { it[0] + 2 * it[1] }
            assertEquals(einsum("ij->i", u), TensorImpl.sumInOneAxis(u, 1, mc))
            assertEquals(einsum("ij->j", u), TensorImpl.sumInOneAxis(u, 0, mc))
        }
    }

    @Test
    fun testEinsum4() {
        val u = Tensor(2, 3, 4) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        with(tenZ) {
            assertEquals(u.sum(-1), einsum("ijk->ij", u))
            assertEquals(u.sum(0, 1), einsum("ijk->k", u))
        }
    }

    @Test
    fun testEinsum5() {

        val u = Tensor(2, 2, 3) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        val w = Tensor(2, 3, 4) { it[0] + 1 }

        with(tenZ) {
            val r1 = u.matmul(w, r = 2)
            val r2 = einsum("ijk,jkl->il", u, w)
            assertEquals(r1, r2)
        }
    }

    @Test
    fun testEinsum6() {
        val t1 = Tensor(2, 3) { it.sum() }
        val t2 = Tensor(3, 4) { it.sum() }
        val t3 = Tensor(3, 4, 5) { it.sum() }

        with(tenZ) {
            val r = TensorImpl.einsum(
                listOf(t1, t2, t3),
                resShape = intArrayOf(2, 5), mulShape = intArrayOf(3, 4),
                tToResList = listOf(listOf(0 to 0), listOf(), listOf(2 to 1)),
                tToMulList = listOf(listOf(1 to 0), listOf(0 to 0, 1 to 1), listOf(0 to 0, 1 to 1)),
                mc
            )
            assertContentEquals(intArrayOf(2, 5), r.shape)
        }
    }

    @Test
    fun testConcat() {
        val u = Tensor.of(3, 2) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        val w = Tensor.of(3, 3) { it[0] }

        with(tenZ) {
            val v = Tensor.concatM(u, w, axis = 1)
            assertArrayEquals(intArrayOf(3, 5), v.shape)
            val v1 = v.slice(0, null)
            v1.setAll(1)
            assertTrue(u.slice(0).all { it == 1 })
            assertTrue(w.slice(0).all { it == 1 })
        }
    }

    @Test
    fun testStack() {
        val shape = intArrayOf(3, 3)
        val shape2 = intArrayOf(3, 3)
        val u = Tensor.of(*shape) { idx -> idx.withIndex().sumOf { (1 + it.index) * it.value } }
        val w = Tensor.of(*shape2) { it[0] }

        with(tenZ) {
            val v = Tensor.stackM(u, w, axis = 1)
            assertEquals(v.slice(null, 0, null), u)
            assertEquals(v.slice(null, 1, null), w)
        }
    }

    @Test
    fun testCreate() {
        val t = Tensor.of<Int>(
            listOf(
                listOf(1, 2, 3),
                listOf(3, 4, 5)
            )
        )

        with(tenZ) {
            assertArrayEquals(intArrayOf(2, 3), t.shape)
            assertEquals(18, t.sumAll())
        }
    }

    @Test
    fun testDiag() {
        val a = Tensor.of<Int>((0..3).toList()).reshape(2, 2)

        with(tenZ) {
            assertEquals(Tensor.of(listOf(0, 3)), a.diagonal())
            assertEquals(Tensor.fill(c = 1, 1), a.diagonal(1))

            val b = Tensor.of<Int>((0..7).toList()).reshape(2, 2, 2)
            assertEquals(
                Tensor.of(intArrayOf(2, 2), 0, 6, 1, 7),
                b.diagonal(0, 0, 1)
            )
        }
    }

    @Test
    fun testTrace() {
        val a = Tensor.of(intArrayOf(2, 2), 0..3)

        with(tenZ) {
            val tr = a.trace()
            assertEquals(Tensor.scalar(3), tr)

            val b = Tensor.of<Int>((0..7).toList()).reshape(2, 2, 2)
            assertEquals(
                Tensor.of(listOf(5, 9)),
                b.trace(0, 0)
            )
            assertEquals(
                b.diagonal(0, 0, -1).sum(-1),
                b.trace(0, 0, -1)
            )
        }
    }

    @Test
    fun testFunctionalities() {
        val ℤ = NumberModels.integers()
        val ℚ = fractions()
        val tZ = Tensor.over(ℤ)
        val tQ = Tensor.over(ℚ)
        with(tZ) {
            val t1 = ones(2, 3)
            val t2 = zerosLike(t1)
            assertEquals(t2, t1 * t2)

            t2 += t1

        }
    }

//    @Test
//    fun testToMatrix() {
//        val mc = NumberModels.DoubleAsReals
//        val a = Tensor(intArrayOf(3, 3), mc) {
//            Random.nextDouble()
//        }
//        val m = a.toMatrix()
//        val a1 = Tensor.fromMatrix(m)
//        assertEquals(a, a1)
//        assert(mc.isEqual(a.sumAll(), m.sum()))
//        assert(mc.isEqual(a.trace().sumAll(), m.trace()))
//        assertEquals(a.transpose().toMatrix(), m.transpose())
//
//        val b = Tensor(a.shape) {
//            Random.nextDouble()
//        }
//        val c = a matmul b
//        val m1 = m * b.toMatrix()
//        assertEquals(c.toMatrix(), m1)
//
//    }


}