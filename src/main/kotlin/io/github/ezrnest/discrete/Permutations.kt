package io.github.ezrnest.discrete

import io.github.ezrnest.numberTheory.NTFunctions
import io.github.ezrnest.util.ArraySup


/**
 * @author liyicheng
 * 2018-03-01 19:49
 */
abstract class AbstractPermutation(override val size: Int) : Permutation {


    override fun compose(before: Permutation): Permutation {
        val newArr = IntArray(size) { i ->
            apply(before.apply(i))
        }
        return Permutation.valueOf(*newArr)
    }


    override fun andThen(after: Permutation): Permutation {
        return after.compose(this)
    }


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
            list.add(TranspositionImpl(size, i, j))
        }
        return list.reversed()
    }


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

    override fun equals(other: Any?): Boolean {
        if (other !is Permutation) {
            return false
        }
        return Permutation.isEqual(this, other)
    }

    override fun hashCode(): Int {
        return getArray().contentHashCode()
    }

    /**
     * Returns the string representation of this permutation, which is the array representation of the permutation.
     */
    override fun toString(): String {
        return getArray().joinToString(prefix = "(", postfix = ")")
    }


}

internal class ArrPermutation(protected val parr: IntArray) : AbstractPermutation(parr.size) {

    override fun apply(x: Int): Int {
        return parr[x]
    }

    override fun applyAll(arr: IntArray, inPlace: Boolean): IntArray {
        val result = if (inPlace) arr else IntArray(size)
        for (i in 0 until size) {
            result[i] = parr[arr[i]]
        }
        return result
    }

    override fun invert(y: Int): Int {
        if (inverseTemp != null) {
            return inverseTemp!!.apply(y)
        }
        for (i in 0 until size) {
            if (parr[i] == y) {
                return i
            }
        }
        return y
    }

    private var inverseTemp: ArrPermutation? = null


    override fun inverse(): Permutation {
        if (inverseTemp == null) {
            val narr = IntArray(size)
            for (i in 0 until size) {
                narr[parr[i]] = i
            }
            inverseTemp = ArrPermutation(narr)
        }

        return inverseTemp!!
    }


    override fun getArray(): IntArray {
        return parr.clone()
    }

    override fun toString(): String {
        return parr.joinToString(prefix = "(", postfix = ")")
    }

    override fun hashCode(): Int {
        return parr.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if(other is ArrPermutation){
            return parr.contentEquals(other.parr)
        }
        return super.equals(other)
    }

}

internal class TranspositionImpl(size: Int, i: Int, j: Int) : AbstractPermutation(size), Transposition {

    override val first: Int = minOf(i, j)


    override val second: Int = maxOf(i, j)

    override fun toString(): String {
        return "Swap($first,$second)"
    }

    override fun decomposeTransposition(): List<Transposition> {
        return super<Transposition>.decomposeTransposition()
    }

    override fun decompose(): List<Cycle> {
        return super<Transposition>.decompose()
    }
}

internal class IdentityPerm(size: Int) : AbstractPermutation(size), Cycle {


    override fun apply(x: Int): Int {
        return x
    }


    override fun invert(y: Int): Int {
        return y
    }


    override fun inverse(): Cycle {
        return this
    }


    override fun containsInCycle(x: Int): Boolean {
        return false
    }

    override val elements: IntArray
        get() = intArrayOf()


    override fun getArray(): IntArray {
        return ArraySup.indexArray(size)
    }


    override fun index(): Long {
        return 0
    }


    override fun rank(): Int {
        return 1
    }


    override fun reverseCount(): Int {
        return 0
    }


    override fun andThen(after: Permutation): Permutation {
        return after
    }


    override fun compose(before: Permutation): Permutation {
        return before
    }

    override fun decomposeTransposition(): List<Transposition> {
        return emptyList()
    }

    override fun decompose(): List<Cycle> {
        return emptyList()
    }
}

internal class RotateAll(size: Int, private val shift: Int) : AbstractPermutation(size) {
    override fun apply(x: Int): Int {
        var res = x
        res += shift
        if (res < 0) {
            res += size
        } else if (res >= size) {
            res -= size
        }
        return res
    }

    override fun inverse(): Permutation {
        return RotateAll(size, -shift)
    }


    override fun invert(y: Int): Int {
        var res = y
        res -= shift
        if (res < 0) {
            res += size
        } else if (res >= size) {
            res -= size
        }
        return res
    }
}

internal class CycleImpl(size: Int, override val elements: IntArray) : AbstractPermutation(size), Cycle {

    private val elementsSorted: IntArray
    private val elementsIndex: IntArray

    init {
//        val entries = Array(elements.size) { i -> Pair(elements[i], elements[(i + 1) % elements.size]) }
//        entries.sortBy { it.first }
//        entriesIn = IntArray(elements.size) { entries[it].first }
//        entriesDest = IntArray(elements.size) { entries[it].second }
        val t = ArraySup.sortWithIndex(elements)
        elementsSorted = t.first
        elementsIndex = t.second
    }

    private fun getInCycle(idx: Int): Int {
        val i = NTFunctions.mod(idx, elements.size)
        return elements[i]
    }


    override fun apply(x: Int): Int {
        // binary search
        val index = elementsSorted.binarySearch(x)
        return if (index >= 0) {
            getInCycle(elementsIndex[index] + 1)
        } else {
            x
        }
    }

    override fun invert(y: Int): Int {
        val index = elementsSorted.binarySearch(y)
        return if (index >= 0) {
            getInCycle(elementsIndex[index] - 1)
        } else {
            y
        }
    }


    override fun inverse(): CycleImpl {
        return CycleImpl(size, ArraySup.flip(elements, 0, elements.size))
    }


    override fun containsInCycle(x: Int): Boolean {
        return elementsSorted.binarySearch(x) >= 0
    }


    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Cycle(")
        elements.joinTo(sb, ",")
        sb.append(")")
        return sb.toString()
    }

    override fun decompose(): List<Cycle> {
        return super<Cycle>.decompose()
    }
}

///**
// * @author liyicheng 2018-03-02 20:26
// */
//object Permutations {
////    fun sizeCheck(size: Int) {
////        require(size > 0) { "Invalid size=$size" }
////    }
//
//}
