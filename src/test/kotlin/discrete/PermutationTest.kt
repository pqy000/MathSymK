package discrete

import util.ArraySup
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class PermutationTest {

    @Test
    fun testPermute1() {
        assertEquals(2, Permutations.valueOf(1, 2, 0).apply(1))
    }


    @Test
    fun testRotate() {
        val p = Permutations.rotate(5, 2)
        assertEquals(2, p.apply(0))
        assertEquals(3, p.apply(1))
        assertEquals(4, p.apply(2))
        assertEquals(0, p.apply(3))
        assertEquals(1, p.apply(4))


        val list = arrayListOf(1, 2, 3, 4, 5)
        val coll_rotated = list.toList().apply { Collections.rotate(this, 2) }
        val rotated = p.permute(list)
        assertEquals(coll_rotated, rotated)

        assertEquals(list.toList().apply { Collections.rotate(this, -2) }, Permutations.rotate(5, -2).permute(list))
    }

    @Test
    fun testDecompose() {
        val identity = Permutations.identity(5)
        assertEquals(emptyList(), identity.decompose())
        val p = Permutations.valueOf(1, 0, 3, 2, 4)
        assertEquals(listOf(Permutations.swap(5, 0, 1), Permutations.swap(5, 2, 3)), p.decompose())
        val p2 = Permutations.rotate(5, 2)
        assertEquals(listOf(p2), p2.decompose())
    }

    @Test
    fun testIndex() {
        val perms = Permutations.universe(5)
        assertEquals(CombUtils.permutation(5, 5).toInt(), perms.size)
        for (i in perms.indices) {
            val idx = perms[i].index()
            assertEquals(i.toLong(), idx)
            assertEquals(perms[i], Permutations.fromIndex(idx, 5))
            assertEquals(perms[i], Permutations.composeAll(perms[i].decompose(), 5))
            assertEquals(perms[i], Permutations.composeAll(perms[i].decomposeTransposition(), 5))
        }
    }

    @Test
    fun testPermute() {
        val p = Permutations.valueOf(1, 0, 3, 2, 4)
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(listOf(2, 1, 4, 3, 5), p.permute(list))

        assertEquals(p, Permutations.fromPermuted(*p.permute(ArraySup.indexArray(5))))
    }

    @Test
    fun testCycle() {
        val p = Permutations.cycle(5, 0, 1, 2)
        assertEquals(1, p.apply(0))
        assertEquals(2, p.apply(1))
        assertEquals(5, p.apply(2))
        assertEquals(3, p.apply(3))
        assertEquals(4, p.apply(4))
        val arr = intArrayOf(1, 2, 3, 4, 5, 6)
        val result = intArrayOf(6, 1, 2, 4, 5, 3)
        assertEquals(result.toList(), p.permute(arr.toList()))
    }


}


fun main() {
    val p = Permutations.valueOf(0, 2, 3, 4, 1)
    println(p.permute(ArraySup.indexArray(5)).contentToString())
//    println(p)
//    var t = p
//    println(p.decomposeTransposition())
//    for (i in p.decomposeTransposition()) {
//        t = t.andThen(i)
//        println(t)
//    }
//    println()
//    t = Permutations.identity(5)
//    for(i in p.decomposeTransposition().reversed()){
//        t = t.andThen(i)
//        println(i)
//        println(t.getArray().contentToString())
//    }
//    println()
//    println(Permutations.composeAll(p.decomposeTransposition().reversed(),5))
}