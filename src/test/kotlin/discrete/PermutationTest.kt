package discrete

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
        val p = Permutations.rotateAll(5, 2)
        assertEquals(2, p.apply(0))
        assertEquals(3, p.apply(1))
        assertEquals(4, p.apply(2))
        assertEquals(0, p.apply(3))
        assertEquals(1, p.apply(4))


        val list = arrayListOf(1, 2, 3, 4, 5)
        val coll_rotated = list.toList().apply { Collections.rotate(this, 2) }
        val rotated = p.permute(list)
        assertEquals(coll_rotated, rotated)

        assertEquals(list.toList().apply { Collections.rotate(this, -2) }, Permutations.rotateAll(5, -2).permute(list))
    }

    @Test
    fun testDecompose(){
        val identity = Permutations.identity(5)
        assertEquals(emptyList(), identity.decompose())
        val p = Permutations.valueOf(1,0,3,2,4)
        assertEquals(listOf(Permutations.swap(5, 0, 1), Permutations.swap(5, 2, 3)), p.decompose())
        val p2 = Permutations.rotateAll(5, 2)
        assertEquals(listOf(p2),p2.decompose())
    }


}

fun main() {
    val p = Permutations.valueOf(1,0,3,2,4)
    println(p.decompose())
    println(Permutations.rotateAll(5, 2).decompose())
}