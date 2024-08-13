/**
 * 2018-03-01
 */
package discrete

import discrete.Permutations.Rotate
import discrete.Permutations.Swap
import discrete.Permutations.isEqual
import discrete.Permutations.valueOf


/**
 * @author liyicheng
 * 2018-03-01 19:49
 */
abstract class AbstractPermutation(override val size: Int) : Permutation {

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#compose(cn.ancono.math.numberTheory.combination.Permutation)
     */
    override fun compose(before: Permutation): Permutation {
        return valueOf(*apply(before.getArray()))
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
        val list: MutableList<Cycle> = ArrayList(
            size
        )
        val mark = BooleanArray(length)
        for (i in 0 until length) {
            if (mark[i]) {
                continue
            }
            var t = i
            val temp = BooleanArray(length)
            var n = 0
            while (!temp[t]) {
                temp[t] = true
                t = arr[t]
                n++
            }
            if (n == 1) {
                mark[i] = true
                continue
            }
            val elements = IntArray(n)
            for (j in 0 until n) {
                elements[j] = t
                mark[t] = true
                t = arr[t]
            }
            list.add(Rotate(length, elements))
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

    /*
     * @see java.lang.Object#toString()
     */
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('(')
        for (i in 0 until size) {
            sb.append(apply(i)).append(',')
        }
        sb.deleteCharAt(sb.length - 1)
        sb.append(')')
        return sb.toString()
    }
}
