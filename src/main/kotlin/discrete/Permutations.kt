package discrete

import cn.mathsymk.model.struct.FiniteGroup
import cn.mathsymk.structure.Group
import cn.mathsymk.util.IterUtils
import util.ArraySup

/**
 * @author liyicheng 2018-03-02 20:26
 */
object Permutations {
    fun sizeCheck(size: Int) {
        require(size > 0) { "Invalid size=$size" }
    }

    fun rangeAndDuplicateCheck(array: IntArray, ubound: Int) {
        val marks = BooleanArray(ubound)
        for (j in array) {
            require(!(j < 0 || j >= ubound)) { "Invalid index=$j" }
            require(!marks[j]) { "Duplicated index=$j" }
            marks[j] = true
        }
    }

    /**
     * Gets a permutation of the specific array as the method [getArray()] in Permutation.
     */
    @JvmStatic
    fun valueOf(vararg array: Int): Permutation {
        sizeCheck(array.size)
        rangeAndDuplicateCheck(array, array.size)
        return ArrPermutation(array)
    }

    /**
     * Returns the permutation of swapping the i-th element and the j-th element.
     *
     * @param size the size(length) of the permutation
     */
    fun swap(size: Int, i: Int, j: Int): Transposition {
        sizeCheck(size)
        require(!(i < 0 || j < 0 || i >= size || j >= size)) { "Invalid index i=$i,j=$j" }
        return Swap(size, i, j)
    }

    fun identity(size: Int): Permutation {
        require(size > 0) { "Invalid size=$size" }
        return Identity(size)
    }

    /**
     * Returns a new permutation that reverse the order of the array, which can be
     * written as `(n-1,n-2,...2,1,0)`
     *
     */
    fun flipAll(n: Int): Permutation {
        sizeCheck(n)
        return object : AbstractPermutation(n) {
            override fun inverse(): Permutation {
                return this
            }

            override fun inverse(y: Int): Int {
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
    fun rotateAll(n: Int, shift: Int): Permutation {
        var shift = shift
        sizeCheck(n)
        shift %= n
        if (shift == 0) {
            return Identity(n)
        }
        return RotateAll(n, shift)
    }

    /**
     * A left-version of [.rotateAll]
     */
    fun rotateAllLeft(n: Int, shift: Int): Permutation {
        return rotateAll(n, n - shift)
    }


    /**
     * Returns a new permutation that cycles each one in the given [elements] to the next one.
     * For example, `cycle(5,1,2,0) = (0,1,2)`.
     *
     * @see Cycle
     */
    fun cycle(vararg elements: Int): Cycle {
        rangeAndDuplicateCheck(elements, Int.MAX_VALUE)
        if (elements.isEmpty()) {
            return Identity(0)
        }
        return CycleImpl(elements.max(), elements)
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
        sizeCheck(size)
        require(!(i < 0 || j < 0 || i >= size || j >= size)) { "Invalid index i=$i,j=$j" }
        val arr = ArraySup.indexArray(size)
        ArraySup.flip(arr, i, j)
        return ArrPermutation(arr)
    }

    /**
     * Parse the Permutation from the given non-negative index and the given size.
     *
     * @param index
     * @return
     */
    fun fromIndex(index: Long, size: Int): Permutation {
        var index = index
        require(index >= 0) { "Negative index=$index" }
        sizeCheck(size)
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

    fun getCalculator(size: Int): Group<Permutation> {
        return PermutationCalculator(size)
    }


    /**
     * Returns a set of permutations that contains all the n-size permutations.
     * This method only supports n smaller than 13.
     *
     * @param n
     * @return
     */
    fun universe(n: Int): List<Permutation> {
        require(!(n <= 0 || n > 12)) { "Invalid n=$n" }
        val list = ArrayList<Permutation>(CombUtils.factorial(n).toInt())
        TODO()
//        for (arr in Enumer.permutation(n, n)) {
//            list.add(ArrPermutation(arr))
//        }
//        return list
    }

    fun universeIterable(n: Int): Iterable<Permutation> {
        require(n > 0) { "Invalid n=$n" }
        val seq: Sequence<Permutation> = IterUtils.perm(n, n, false).map { parr: IntArray -> ArrPermutation(parr) }
        return seq.asIterable()
    }


    fun even(n: Int): FiniteGroup<Permutation> {
        require(!(n <= 0 || n > 12)) { "Invalid n=$n" }
        val list = arrayOfNulls<Permutation>(CombUtils.factorial(n).toInt() / 2)
        var i = 0
        for (arr in Enumer.permutation(n, n)) {
            val p: Permutation = ArrPermutation(arr)
            if (p.isEven) {
                list[i++] = p
            }
        }
        TODO()
//        return MathSets.asSet(getCalculator(n), list)
    }

    /**
     * Composes all the permutations in the list, applying the last element in the list first.
     *
     * @param list a list of permutations, not empty
     * @return the result as a permutation
     */
    fun composeAll(list: List<Permutation>): Permutation {
        require(!list.isEmpty())
        val lit = list.listIterator(list.size)
        var p = lit.previous()
        while (lit.hasPrevious()) {
            p = p.andThen(lit.previous())
        }
        return p
    }

    /**
     * Composes all the permutations in the list, applying the last element in the list first.
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
        return composeAll(list)
    } //    public static void main(String[] args) {
    //        var p1 = valueOf(new int[]{1,2,0});
    //        var p2 = valueOf(new int[]{0,1,2});
    //        print(p1.compose(p2));
    //        print(p2.compose(p1));
    //        print(p1.apply(p2.getArray()));
    //    }

    internal class ArrPermutation(protected val parr: IntArray) : AbstractPermutation(parr.size) {
        /*
               * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int)
               */
        override fun apply(x: Int): Int {
            return parr[x]
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#inverse(int)
         */
        override fun inverse(y: Int): Int {
            for (i in 0 until size) {
                if (parr[i] == y) {
                    return i
                }
            }
            throw AssertionError()
        }

        private var inverseTemp: ArrPermutation? = null

        /*
        * @see cn.ancono.math.numberTheory.combination.Permutation#inverse()
        */
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

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#getArray()
         */
        override fun getArray(): IntArray {
            return parr.clone()
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(java.lang.Object[])
         */
        override fun <T> permute(array: Array<T>): Array<T> {
            require(array.size >= size) { "array's length!=$size" }
            val copy = array.clone()
            for (i in 0 until size) {
                array[i] = copy[parr[i]]
            }
            return array
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int[])
         */
        override fun permute(array: IntArray): IntArray {
            require(array.size >= size) { "array's length!=$size" }
            val copy = array.clone()
            for (i in 0 until size) {
                array[i] = copy[parr[i]]
            }
            return array
        }

        /*
         * @see
         * cn.ancono.math.numberTheory.combination.AbstractPermutation#compose(cn.timelives.
         * java.math.combination.Permutation)
         */
        override fun compose(before: Permutation): Permutation {
            return ArrPermutation(permute(before.getArray()))
        }

        /*
         * @see
         * cn.ancono.math.numberTheory.combination.AbstractPermutation#andThen(cn.timelives.
         * java.math.combination.Permutation)
         */
        override fun andThen(after: Permutation): Permutation {
            return ArrPermutation(after.permute(getArray()))
        }
    }

    internal class Swap(size: Int, i: Int, j: Int) : AbstractPermutation(size), Transposition {
        /*
         * @see
         * cn.ancono.math.numberTheory.combination.Permutation.ElementaryPermutation#getFirst
         * ()
         */
        override val first: Int

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation.ElementaryPermutation#
         * getSecond()
         */
        override val second: Int

        /**
         * Swap `i` and `j` if `i>j`
         *
         * @param size
         */
        init {
            var i = i
            var j = j
            if (i > j) {
                val t = i
                i = j
                j = t
            }
            this.first = i
            this.second = j
        }


        /*
        * @see cn.ancono.math.numberTheory.combination.AbstractPermutation#toString()
        */
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

    internal class Identity(size: Int) : AbstractPermutation(size),Cycle {


        /*
        * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int)
        */
        override fun apply(x: Int): Int {
            return x
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#inverse(int)
         */
        override fun inverse(y: Int): Int {
            return y
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#inverse()
         */
        override fun inverse(): Cycle {
            return this
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation.ElementaryPermutation#containsElement(int)
         */
        override fun containsInCycle(x: Int): Boolean {
            return false
        }

        override val elements: IntArray
            /*
                     * @see cn.ancono.math.numberTheory.combination.Permutation.ElementaryPermutation#getElements()
                     */
            get() = intArrayOf()


        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int[])
         */
        override fun permute(array: IntArray): IntArray {
            return array
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(java.lang.Object[])
         */
        override fun <T> permute(array: Array<T>): Array<T> {
            return array
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#getArray()
         */
        override fun getArray(): IntArray {
            return ArraySup.indexArray(size)
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#index()
         */
        override fun index(): Long {
            return 0
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#rank()
         */
        override fun rank(): Int {
            return 1
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#inverseCount()
         */
        override fun reverseCount(): Int {
            return 0
        }


        /*
         * @see
         * cn.ancono.math.numberTheory.combination.AbstractPermutation#andThen(cn.timelives.
         * java.math.combination.Permutation)
         */
        override fun andThen(after: Permutation): Permutation {
            return after
        }

        /*
         * @see
         * cn.ancono.math.numberTheory.combination.AbstractPermutation#compose(cn.timelives.
         * java.math.combination.Permutation)
         */
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
        /*
               * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int)
               */
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

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#inverse()
         */
        override fun inverse(): Permutation {
            return RotateAll(size, -shift)
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#inverse(int)
         */
        override fun inverse(y: Int): Int {
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

        //            require(elements.isNotEmpty()){"Empty cycle"}
//            require(elements.size == elements.distinct().size){"Duplicate elements in cycle"}
        private val entriesIn : IntArray
        private val entriesDest : IntArray
        init{
            val entries = Array(elements.size){ i -> Pair(elements[i], elements[(i+1)%elements.size])}
            entries.sortBy { it.first }
            entriesIn = IntArray(elements.size){ entries[it].first }
            entriesDest = IntArray(elements.size){ entries[it].second }

        }


        override fun apply(y: Int): Int {
            // binary search
            val index = entriesIn.binarySearch(y)
            if (index >= 0) {
                return entriesDest[index]
            }else{
                return y
            }
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#inverse()
         */
        override fun inverse(): CycleImpl {
            return CycleImpl(size, ArraySup.flip(elements, 0, elements.size))
        }

//        /*
//         * @see cn.ancono.math.numberTheory.combination.Permutation.RotationPermutation#getElements()
//         */
//        override fun getElements(): IntArray {
//            return elements.clone()
//        }


        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation.RotationPermutation#containsElement(int)
         */
        override fun containsInCycle(x: Int): Boolean {
            for (element in elements) {
                if (element == x) {
                    return true
                }
            }
            return false
        }


        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int)
         */
        override fun inverse(x: Int): Int {
            if (!containsInCycle(x)) {
                return x
            }
            if (elements.size == 1) {
                return x
            }
            val earr = this.elements
            var index = ArraySup.firstIndexOf(x, earr)
            index--
            if (index < 0) {
                index += earr.size
            }
            return earr[index]
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int[])
         */
        @Suppress("DuplicatedCode") // Duplicated code is necessary for different types
        override fun permute(array: IntArray): IntArray {
            if (elements.size == 1) {
                return array
            }
            val earr = this.elements
            val t = array[earr[0]]
            for (i in 0 until earr.size - 1) {
                array[earr[i]] = array[earr[i + 1]]
            }
            array[earr[earr.size - 1]] = t
            return array
        }

//        /*
//         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(boolean[])
//         */
//        override fun permute(array: BooleanArray): BooleanArray {
//            if (elements.size == 1) {
//                return array
//            }
//            val earr = this.elements
//            val t = array[earr[0]]
//            for (i in 0 until earr.size - 1) {
//                array[earr[i]] = array[earr[i + 1]]
//            }
//            array[earr[earr.size - 1]] = t
//            return array
//        }
//
//        /*
//         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(double[])
//         */
//        override fun permute(array: DoubleArray): DoubleArray {
//            if (elements.size == 1) {
//                return array
//            }
//            val earr = this.elements
//            val t = array[earr[0]]
//            for (i in 0 until earr.size - 1) {
//                array[earr[i]] = array[earr[i + 1]]
//            }
//            array[earr[earr.size - 1]] = t
//            return array
//        }
//
//        /*
//         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(long[])
//         */
//        override fun permute(array: LongArray): LongArray {
//            if (elements.size == 1) {
//                return array
//            }
//            val earr = this.elements
//            val t = array[earr[0]]
//            for (i in 0 until earr.size - 1) {
//                array[earr[i]] = array[earr[i + 1]]
//            }
//            array[earr[earr.size - 1]] = t
//            return array
//        }

        /*
         * @see cn.ancono.math.numberTheory.combination.Permutation#apply(java.lang.Object[])
         */
        override fun <T> permute(array: Array<T>): Array<T> {
            if (elements.size == 1) {
                return array
            }
            val earr = this.elements
            val t = array[earr[0]]
            for (i in 0 until earr.size - 1) {
                array[earr[i]] = array[earr[i + 1]]
            }
            array[earr[earr.size - 1]] = t
            return array
        }

        /*
         * @see cn.ancono.math.numberTheory.combination.AbstractPermutation#toString()
         */
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

    @JvmRecord
    private data class PermutationCalculator(
        val size: Int,
        override val identity: Permutation = identity(size),
        override val numberClass: Class<Permutation> = Permutation::class.java
    ) : Group<Permutation> {
        override fun isEqual(x: Permutation, y: Permutation): Boolean {
            return Permutations.isEqual(x, y)
        }

        override fun inverse(x: Permutation): Permutation {
            return x.inverse()
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
