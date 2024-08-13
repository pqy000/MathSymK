/**
 * 2018-03-01
 */
package discrete

import cn.mathsymk.model.struct.Composable
import cn.mathsymk.model.struct.Invertible
import cn.mathsymk.number_theory.NTFunctions
import util.ArraySup

/**
 * A permutation describes a transformation on a finite set of elements.
 * Different from the mathematical permutation,
 * the smallest index in this permutation should be **zero**.
 *
 *
 * See: [Permutation](https://en.wikipedia.org/wiki/Permutation)
 *
 * @author liyicheng
 * 2018-03-01 19:26
 * @see Permutations
 */
interface Permutation : Composable<Permutation>, Invertible<Permutation>, Comparable<Permutation> {
    /**
     * Returns the size of this permutation, which is equal to the
     * size of the finite set.
     *
     */
    val size : Int

    /**
     * Returns the index of the element of index `x` after this permutation.
     *
     * For example, if the permutation is (1,0,2), then `apply(1)` returns 0.
     */
    fun apply(x: Int): Int

    /**
     * Returns the index before this permutation of the element of index `y` after this permutation.
     * It is ensured that `inverse(apply(n))==n`.
     *
     *
     * For example, if the permutation is (1,0,2), then `inverse(1)` returns 0.
     *
     */
    fun inverse(y: Int): Int

    /**
     * Returns the inverse of this permutation.
     *
     */
    override fun inverse(): Permutation

    /**
     * Returns the index of this permutation. The index is
     * a number ranged in [0,size! - 1]. It represents the index of this
     * permutation in all the permutations of the identity size ordered by
     * the natural of their representative array. The identity permutation always
     * has the index of `0` and the total flip permutation always has the
     * index of `size! - 1`
     *
     * For example, the index of `(1,0,2)` is `2`, because all
     * 3-permutations are sorted as
     * `(0,1,2),(0,2,1),(1,0,2),(1,2,0),(2,0,1),(2,1,0)`.
     */
    fun index(): Long {
        var sum: Long = 0
        val arr = getArray()
        for (i in arr.indices) {
            sum += arr[i] * CombUtils.factorial(arr.size - i - 1)
            for (j in i + 1 until arr.size) {
                if (arr[j] > arr[i]) {
                    arr[j]--
                }
            }
        }
        return sum
    }

    /**
     * Reduces this permutation to several elementary permutations. The last element in the
     * list is the first permutation that should be applied. Therefore, assume the elements
     * in the list in order are `p1,p2,p3...pn`, then `p1·p2·...·pn == this`, where
     * `·` is the compose of permutations.
     *
     *
     * For example, if `this=(4,0,3,1,2)`, then the returned list can
     * be equal to `(0,4)(0,1)(2,4)(3,4)`.
     *
     * @return
     */
    fun decomposeTransposition(): List<Transposition>

    /**
     * Returns the count of reverse in the array representing this permutation.
     *
     * @return
     */
    fun reverseCount(): Int {
        return CombUtils.reverseCount(getArray())
    }

    val isEven: Boolean
        /**
         * Determines whether this permutation is an even permutation.
         *
         * @return
         */
        get() = reverseCount() % 2 == 0

    /**
     * Decompose this permutation to several non-intersecting rotation permutations. The order is not
     * strictly restricted because the rotation permutations are commutative. The list may omit
     * rotations of length 1.
     *
     *
     * For example, if `this=(2,0,4,3,1,7,6,5)`, then the returned list can
     * be equal to `(1,3,5,2)(6,8)`, and
     *
     * @return
     */
    fun decompose(): List<Cycle>

    /**
     * Returns the rank of this permutation.<P>
     * `this^rank=identity`
     *
     * @return
    </P> */
    fun rank(): Int {
        val list = decompose()
        var rank = 1
        for (p in list) {
            rank = NTFunctions.lcm(rank, p.rank())
        }
        return rank
    }

    /*
     * Returns a composed permutation that first applies the {@code before}
     * permutation to its input, and then applies this permutation to the result.
     *
     */
    override fun compose(before: Permutation): Permutation

    /**
     * Returns a composed permutation that first applies this permutation to
     * its input, and then applies the `after` permutation to the result.
     */
    override fun andThen(after: Permutation): Permutation

    /**
     * Gets a copy of array representing this permutation. For each index n in the range,
     * `arr[n]==apply(n)`
     *
     */
    fun getArray(): IntArray {
        val length = size
        val arr = IntArray(length)
        for (i in 0 until length) {
            arr[i] = apply(i)
        }
        return arr
    }

    /**
     * Applies this permutation to an array. The `i`-th element in the resulting
     * array will be equal to the `apply(i)`-th element in the original array.
     */
    fun <T> apply(array: Array<T>): Array<T> {
        require(array.size >= size) { "array's length!=" + size }
        val copy = array.clone()
        for (i in array.indices) {
            array[i] = copy[apply(i)]
        }
        return array
    }

    fun <T> apply(list: List<T>): List<T> {
        require(list.size >= size) { "The list's length ${list.size} is not enough." }
        val newList = ArrayList<T>(list.size)
        for (i in list.indices) {
            newList.add(list[apply(i)])
        }
        return newList
    }

    /**
     * Applies this permutation to an integer array.
     * The `i`-th element in the resulting
     * array will be equal to the `apply(i)`-th element in the original array.
     */
    fun apply(array: IntArray): IntArray {
        require(array.size >= size) { "array's length!=" + size }
        val copy = array.clone()
        for (i in array.indices) {
            array[i] = copy[apply(i)]
        }
        return array
    }

    /**
     * Applies this permutation to an array.
     * The `i`-th element in the resulting
     * array will be equal to the `apply(i)`-th element in the original array.
     */
    fun apply(array: DoubleArray): DoubleArray {
        require(array.size >= size) { "array's length!=" + size }
        val copy = array.clone()
        for (i in array.indices) {
            array[i] = copy[apply(i)]
        }
        return array
    }

    /**
     * Applies this permutation to an array.
     * The `i`-th element in the resulting
     * array will be equal to the `apply(i)`-th element in the original array.
     */
    fun apply(array: BooleanArray): BooleanArray {
        require(array.size >= size) { "array's length!=" + size }
        val copy = array.clone()
        for (i in array.indices) {
            array[i] = copy[apply(i)]
        }
        return array
    }

    /**
     * Applies this permutation to an array.
     * The `i`-th element in the resulting
     * array will be equal to the `apply(i)`-th element in the original array.
     */
    fun apply(array: LongArray): LongArray {
        require(array.size >= size) { "array's length!=" + size }
        val copy = array.clone()
        for (i in array.indices) {
            array[i] = copy[apply(i)]
        }
        return array
    }

    val isIdentity: Boolean
        get() {
            for (i in 0 until size) {
                if (apply(i) != i) {
                    return false
                }
            }
            return true
        }

    override fun compareTo(o: Permutation): Int {
        val comp = size - o.size
        if (comp != 0) {
            return comp
        }
        return java.lang.Long.signum(index() - o.index())
    }
}

/**
 * A cycle permutation is a permutation that shifts some elements in this permutation by one.
 * <P>For example, a rotation permutation whose element array is (0,2,4,1) should
 * have a permutation array of (2,0,4,3,1,5,6,7), which means the permutation map 0 to 2,
 * 2 to 4,4 to 1 and 1 to 0.
 *
 * @author liyicheng
 * 2018-03-03 15:44
</P> */
interface Cycle : Permutation {
    /**
     * Gets an array that contains all the elements that
     * should be rotated.
     *
     * @return
     */
    val elements: IntArray

    /**
     * Determines whether the element should be rotated.
     *
     * @param x
     * @return
     */
    fun containsElement(x: Int): Boolean

    /**
     * Gets the number of the elements to rotate, the result should not be
     * bigger than `size()`
     *
     * @return
     */
    fun length(): Int

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#rank()
     */
    override fun rank(): Int {
        return length()
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#reduceRotate()
     */
    override fun decompose(): List<Cycle> {
        return listOf(this)
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int)
     */
    override fun apply(x: Int): Int {
        if (!containsElement(x)) {
            return x
        }
        if (length() == 1) {
            return x
        }
        val earr = elements
        var index: Int = ArraySup.firstIndexOf(x, earr)
        index--
        if (index < 0) {
            index += earr.size
        }
        return earr[index]
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int[])
     */
    override fun apply(array: IntArray): IntArray {
        if (length() == 1) {
            return array
        }
        val earr = elements
        val t = array[earr[0]]
        for (i in 0 until earr.size - 1) {
            array[earr[i]] = array[earr[i + 1]]
        }
        array[earr[earr.size - 1]] = t
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(boolean[])
     */
    override fun apply(array: BooleanArray): BooleanArray {
        if (length() == 1) {
            return array
        }
        val earr = elements
        val t = array[earr[0]]
        for (i in 0 until earr.size - 1) {
            array[earr[i]] = array[earr[i + 1]]
        }
        array[earr[earr.size - 1]] = t
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(double[])
     */
    override fun apply(array: DoubleArray): DoubleArray {
        if (length() == 1) {
            return array
        }
        val earr = elements
        val t = array[earr[0]]
        for (i in 0 until earr.size - 1) {
            array[earr[i]] = array[earr[i + 1]]
        }
        array[earr[earr.size - 1]] = t
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(long[])
     */
    override fun apply(array: LongArray): LongArray {
        if (length() == 1) {
            return array
        }
        val earr = elements
        val t = array[earr[0]]
        for (i in 0 until earr.size - 1) {
            array[earr[i]] = array[earr[i + 1]]
        }
        array[earr[earr.size - 1]] = t
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(java.lang.Object[])
     */
    override fun <T> apply(array: Array<T>): Array<T> {
        if (length() == 1) {
            return array
        }
        val earr = elements
        val t = array[earr[0]]
        for (i in 0 until earr.size - 1) {
            array[earr[i]] = array[earr[i + 1]]
        }
        array[earr[earr.size - 1]] = t
        return array
    }
}

/**
 * An transposition permutation is a permutation that only swap two elements.
 * By convenience, it is not strictly required that the two elements aren't the identity.
 *
 * @author liyicheng
 * 2018-03-02 20:47
 */
interface Transposition : Cycle {
    /**
     * Gets the index of the first element of the swapping, which has
     * a smaller index.
     *
     * @return
     */
    val first: Int

    /**
     * Gets the index of the second element of the swapping, which has
     * a bigger index.
     *
     * @return
     */
    val second: Int

    override val elements: IntArray
        /*
                  */
        get() {
            val a = first
            val b = second
            if (a == b) {
                return intArrayOf(a)
            }
            return intArrayOf(a, b)
        }

    /*
     */
    override fun length(): Int {
        return if (first == second) 1 else 2
    }

    /*
     */
    override fun containsElement(x: Int): Boolean {
        return x == first || x == second
    }


    /*
     */
    override fun apply(x: Int): Int {
        val f = first
        val s = second
        if (x == f) {
            return s
        }
        if (x == s) {
            return f
        }
        return x
    }

    /*
     */
    override fun inverse(y: Int): Int {
        //symmetry
        return apply(y)
    }

    /*
     */
    override fun inverse(): Transposition {
        return this
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#reduce()
     */
    override fun decomposeTransposition(): List<Transposition> {
        return listOf(this)
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#reduceRotate()
     */
    override fun decompose(): List<Cycle> {
        return listOf<Cycle>(this)
    }


    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(boolean[])
     */
    override fun apply(array: BooleanArray): BooleanArray {
        ArraySup.swap(array, first, second)
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(double[])
     */
    override fun apply(array: DoubleArray): DoubleArray {
        ArraySup.swap(array, first, second)
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int[])
     */
    override fun apply(array: IntArray): IntArray {
        ArraySup.swap(array, first, second)
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(long[])
     */
    override fun apply(array: LongArray): LongArray {
        ArraySup.swap(array, first, second)
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(java.lang.Object[])
     */
    override fun <T> apply(array: Array<T>): Array<T> {
        ArraySup.swap(array, first, second)
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#getArray()
     */
    override fun getArray(): IntArray {
        val arr: IntArray = ArraySup.indexArray(size)
        return apply(arr)
    }
}