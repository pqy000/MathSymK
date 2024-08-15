package discrete

import cn.mathsymk.model.struct.FiniteGroup
import cn.mathsymk.number_theory.NTFunctions
import cn.mathsymk.structure.Group
import cn.mathsymk.util.IterUtils
import discrete.Permutations.isEqual
import discrete.Permutations.valueOf
import util.ArraySup


/**
 * @author liyicheng
 * 2018-03-01 19:49
 */
abstract class AbstractPermutation(override val size: Int) : Permutation {


    override fun compose(before: Permutation): Permutation {
        val newArr = IntArray(size) { i ->
            apply(before.apply(i))
        }
        return valueOf(*newArr)
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
        if (other !is AbstractPermutation) {
            return false
        }
        return isEqual(this, other)
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


    override fun compose(before: Permutation): Permutation {
        return ArrPermutation(permute(before.getArray()))
    }


    override fun andThen(after: Permutation): Permutation {
        return ArrPermutation(after.permute(getArray()))
    }
}

internal class TranspositionImpl(size: Int, i: Int, j: Int) : AbstractPermutation(size), Transposition {

    override val first: Int


    override val second: Int

    /**
     * Swap `i` and `j` if `i>j`
     *
     * @param size
     */
    init {
        this.first = minOf(i, j)
        this.second = maxOf(i, j)
    }


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
        var x = x
        x += shift
        if (x < 0) {
            x += size
        } else if (x >= size) {
            x -= size
        }
        return x
    }

    override fun inverse(): Permutation {
        return RotateAll(size, -shift)
    }


    override fun invert(y: Int): Int {
        var y = y
        y -= shift
        if (y < 0) {
            y += size
        } else if (y >= size) {
            y -= size
        }
        return y
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

/**
 * @author liyicheng 2018-03-02 20:26
 */
object Permutations {
//    fun sizeCheck(size: Int) {
//        require(size > 0) { "Invalid size=$size" }
//    }

    fun checkDistinct(array: IntArray, ubound: Int) {
        val marks = BooleanArray(ubound)
        for (j in array) {
            require(!(j < 0 || j >= ubound)) { "Invalid index=$j" }
            require(!marks[j]) { "Duplicated index=$j" }
            marks[j] = true
        }
    }

    /**
     * Gets a permutation of the specific array as the method [Permutation.getArray] in Permutation.
     */
    @JvmStatic
    fun valueOf(vararg array: Int): Permutation {
        require(array.isNotEmpty()) { "Empty array" }
        checkDistinct(array, array.size)
        return ArrPermutation(array)
    }

    /**
     * Returns a permutation `p` such that `p.permute(0 .. size-1) = permuted`.
     * It is required that `permuted` is a permutation of `0 .. size-1`.
     */
    fun fromPermuted(vararg permuted: Int): Permutation {
        require(permuted.isNotEmpty()) { "Empty array" }
        checkDistinct(permuted, permuted.size)
        val arr = IntArray(permuted.size)
        for (i in permuted.indices) {
            arr[permuted[i]] = i
        }
        return ArrPermutation(arr)
    }

//    fun fromPermuted(origin: )

    /**
     * Returns the permutation of swapping the `i`-th element and the `j`-th element.
     *
     * @param size the size of the permutation
     */
    fun swap(size: Int, i: Int, j: Int): Transposition {
        require(size > 0) { "Invalid size=$size" }
        require(!(i < 0 || j < 0 || i >= size || j >= size)) { "Invalid index i=$i,j=$j" }
        return TranspositionImpl(size, i, j)
    }

    fun identity(size: Int): Permutation {
        require(size >= 0) { "Invalid size=$size" }
        return IdentityPerm(size)
    }

    /**
     * Returns a new permutation that reverse the order of the array, which can be
     * written as `(n-1,n-2,...2,1,0)`
     *
     */
    fun flipAll(n: Int): Permutation {
        require(n > 0)
        return object : AbstractPermutation(n) {
            override fun inverse(): Permutation {
                return this
            }

            override fun invert(y: Int): Int {
                return size - y - 1
            }

            override fun apply(x: Int): Int {
                return size - x - 1
            }

        }
    }

    /**
     * Returns a new permutation that shifts all the elements to the right by [shift] circularly:
     * `i -> (i+shift)%n`.
     *
     *
     * For example, `rotateAll(5,2).permute([0,1,2,3,4]) = [3,4,0,1,2]`.
     *
     *
     * @param n the size of the permutation
     * @param shift the shift number, can be negative
     */
    fun rotate(n: Int, shift: Int): Permutation {
        require(n > 0) { "Invalid size=$n" }
        var s = shift
        s %= n
        if (s == 0) {
            return IdentityPerm(n)
        }
        return RotateAll(n, s)
    }

    /**
     * A left-version of [.rotateAll]
     */
    fun rotateLeft(n: Int, shift: Int): Permutation {
        return rotate(n, -shift)
    }


    /**
     * Returns a new permutation that cycles each one in the given [elements] to the next one.
     * For example, `cycle(1,2,0) = (0,1,2)`.
     *
     * @see Cycle
     */
    fun cycle(vararg elements: Int): Cycle {
        val size = elements.max() + 1
        return cycleSized(size, *elements)
    }

    /**
     * Returns a new permutation that cycles each one in the given [elements] to the next one.
     *
     * @param size the size of the resulting permutation
     */
    fun cycleSized(size: Int, vararg elements: Int): Cycle {
        checkDistinct(elements, size)
        if (elements.isEmpty()) {
            return IdentityPerm(0)
        }
        return CycleImpl(size, elements)
    }

    /**
     * Returns the flip permutation. For example,
     * `flipRange(5,2,5) = (0,1,4,3,2)`
     *
     * @param size
     * @param i    inclusive
     * @param j    exclusive
     * @return
     */
    fun flipRange(size: Int, i: Int, j: Int): Permutation {
        require(size > 0) { "Invalid size=$size" }
        require(!(i < 0 || j < 0 || i >= size || j >= size)) { "Invalid index i=$i,j=$j" }
        val arr = ArraySup.indexArray(size)
        ArraySup.flip(arr, i, j)
        return ArrPermutation(arr)
    }

    /**
     * Parse the Permutation from the given non-negative index and the given size.
     *
     * @param index the index of the permutation in [size]-sized permutations
     * @return
     */
    fun fromIndex(index: Long, size: Int): Permutation {
        var index = index
        require(index >= 0) { "Negative index=$index" }
        require(index < CombUtils.factorial(size)) { "Invalid index=$index for size=$size" }
        val arr = IntArray(size)
        for (i in 0 until size) {
            val f = CombUtils.factorial(size - i - 1)
            var t = 0
            while (index >= f) {
                index -= f
                t++
            }
            arr[i] = t
        }
        for (i in size - 2 downTo 0) {
            val t = arr[i]
            for (j in i + 1 until size) {
                if (arr[j] >= t) {
                    arr[j]++
                }
            }
        }
        return ArrPermutation(arr)
    }

    /**
     * Determines whether the two permutation is equal.
     *
     * @param p1 a permutation
     * @param p2 another permutation
     * @return {@true} if they are equal
     */
    @JvmStatic
    fun isEqual(p1: Permutation, p2: Permutation): Boolean {
        if (p1.size != p2.size) {
            return false
        }
        val size = p1.size
        for (i in 0 until size) {
            if (p1.apply(i) != p2.apply(i)) {
                return false
            }
        }
        return true
    }


    /**
     * Returns a list of permutations that contains all the `n`-size permutations.
     * The permutations are ordered by the [Permutation.index] of the permutation.
     *
     * This method only supports `n` smaller than 13.
     *
     * @param n
     * @return
     */
    fun universe(n: Int): List<Permutation> {
        require(!(n <= 0 || n > 12)) { "Invalid n=$n" }
        val list = ArrayList<Permutation>(CombUtils.factorial(n).toInt())
        IterUtils.perm(n, n, true).forEach { parr: IntArray -> list.add(ArrPermutation(parr)) }
        return list
    }

    fun universeIterable(n: Int): Iterable<Permutation> {
        require(n > 0) { "Invalid n=$n" }
        return IterUtils.perm(n, n, false).map { parr: IntArray -> ArrPermutation(parr) }
    }


    fun even(n: Int): FiniteGroup<Permutation> {
        require(!(n <= 0 || n > 12)) { "Invalid n=$n" }
        val list = arrayOfNulls<Permutation>(CombUtils.factorial(n).toInt() / 2)
        var i = 0
        IterUtils.permRev(n, false).forEach { (arr, revCount) ->
            if (revCount % 2 == 0) {
                list[i++] = ArrPermutation(arr.clone())
            }
        }

        TODO()
//        return MathSets.asSet(getCalculator(n), list)
    }

    /**
     * Composes all the permutations in the list: `list[0] · list[1] · ... · list[n-1]`.
     *
     * @param list a list of permutations, not empty
     * @return the result as a permutation
     */
    fun composeAll(list: List<Permutation>): Permutation {
        require(list.isNotEmpty()) { "Empty list" }
        val size = list[0].size
        return composeAll(list, size)
    }

    /**
     * Composes all the permutations in the list: `list[0] · list[1] · ... · list[n-1]`.
     * If the list is empty, returns the identity permutation.
     *
     * @param list a list of permutations
     * @param size the size of the permutations
     * @return the result as a permutation
     */
    fun composeAll(list: List<Permutation>, size: Int): Permutation {
        if (list.isEmpty()) {
            return identity(size)
        }
        val arr = ArraySup.indexArray(size)
        for (p in list.reversed()) {
            p.applyAll(arr, inPlace = true)
        }
        return valueOf(*arr)
    }


    /**
     *
     */
    fun getCalculator(size: Int): Group<Permutation> {
        return PermutationCalculator(size)
    }

    @JvmRecord
    private data class PermutationCalculator(
        val size: Int,
        override val identity: Permutation = identity(size),
        override val numberClass: Class<Permutation> = Permutation::class.java
    ) : Group<Permutation> {
        override fun isEqual(x: Permutation, y: Permutation): Boolean {
            return Permutations.isEqual(x, y)
        }

        override fun inverse(element: Permutation): Permutation {
            return element.inverse()
        }

        override fun contains(element: Permutation): Boolean {
            return element.size == size
        }

        override val isCommutative: Boolean
            get() = false

        override fun apply(x: Permutation, y: Permutation): Permutation {
            return x.compose(y)
        }


        init {
            require(size > 0) { "size < 0" }
        }
    }
}
