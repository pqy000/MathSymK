/**
 * 2018-03-01
 */
package discrete

import discrete.Permutations.CycleImpl
import discrete.Permutations.Swap
import discrete.Permutations.isEqual
import discrete.Permutations.valueOf
import util.ArraySup


/**
 * @author liyicheng
 * 2018-03-01 19:49
 */
abstract class AbstractPermutation(override val size: Int) : Permutation {

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#compose(cn.ancono.math.numberTheory.combination.Permutation)
     */
    override fun compose(before: Permutation): Permutation {
        return valueOf(*permute(before.getArray()))
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#andThen(cn.ancono.math.numberTheory.combination.Permutation)
     */
    override fun andThen(after: Permutation): Permutation {
        return after.compose(this)
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#reduce()
     */
    override fun decomposeTransposition(): List<Transposition> {
        val list: MutableList<Transposition> = ArrayList(size)
        val arr = getArray()
        for (i in 0 until size) {
            if (arr[i] == i) {
                continue
            }
            var j = i + 1
            while (j < size) {
                if (arr[j] == i) {
                    break
                }
                j++
            }
            //arr[j] = i
            //swap i,j
            arr[j] = arr[i]
            arr[i] = i
            list.add(Swap(size, i, j))
        }
        return list
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#rotateReduce()
     */
    override fun decompose(): List<Cycle> {
        val arr = getArray()
        val length = arr.size
        val list: MutableList<Cycle> = ArrayList(size)
        val mark = BooleanArray(length)
        val temp = IntArray(length)
        for (i in 0 until length) {
            if (mark[i]) {
                continue
            }
            var t = i
            var count = 0
            do {
                temp[count++] = t
                mark[t] = true
                t = arr[t]
            } while (!mark[t])
            if (count == 1) {
                continue
            }
            val elements = IntArray(count)
            System.arraycopy(temp, 0, elements, 0, count)
            list.add(CycleImpl(length, elements))
        }
        return list
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(other: Any?): Boolean {
        if (other !is AbstractPermutation) {
            return false
        }
        return isEqual(this, other)
    }

    /**
     * Returns the string representation of this permutation, which is the array representation of the permutation.
     */
    override fun toString(): String {
        val idArr = ArraySup.indexArray(size)
        val arr = permute(idArr)
        return arr.joinToString(prefix = "(", postfix = ")")
    }
}
