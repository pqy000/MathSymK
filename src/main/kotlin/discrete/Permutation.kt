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
 *
 * A permutation is a bijective function `σ: S -> S`, from a finite set, `S = {0,1, ... size-1}`, to itself.
 * This bijective function is described by the methods [apply(Int)] and [inverse].
 * Moreover, [getArray] returns an array that represents this function.
 *
 *
 *
 * A permutation can naturally [permute] an array (a list) of elements by moving `i`-th element to the `apply(i)`-th position.
 * Namely, if we have an array `arr`, and  `newArr = p.permute(arr)` where `p` is a permutation,
 * then `newArr[p.apply(i)] = arr[i]`.
 * Mathematically, we have `(σf)(i) = f(σ⁻¹(i))` or `g(σ(i)) = f(i)` where `g = σf`.
 * This convention is consistent with composition: `(σ₁·σ₂)(f) = σ₁(σ₂(f))`,
 * which is proven by `(σ₁·σ₂)(f)(i) = f( (σ₁·σ₂)⁻¹(i) ) = f(σ₂⁻¹(σ₁⁻¹(i))) = σ₂(f(σ₁⁻¹(i))) = σ₂(σ₁(f))(i)`.
 *
 *
 * Alternatively, there is a cycle representation of a permutation. A cycle is a permutation that shifts some elements in this permutation by one.
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
    val size: Int

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
     * a number ranged in `[0,size! - 1]`. It represents the index of this
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
     * @see Transposition
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
     *
     * @see Cycle
     */
    fun decompose(): List<Cycle>

    /**
     * Returns the rank of this permutation.
     * `this^rank=identity`
     *
     * @return
     */
    fun rank(): Int {
        val list = decompose()
        var rank = 1
        for (p in list) {
            rank = NTFunctions.lcm(rank, p.rank())
        }
        return rank
    }

    override fun compose(before: Permutation): Permutation

    /**
     * Returns a composed permutation that first applies this permutation to
     * its input, and then applies the `after` permutation to the result.
     */
    override fun andThen(after: Permutation): Permutation

    /**
     * Gets a copy of array representing this permutation mapping:
     * Let `arr = getArray()`, then `arr[i] = apply(i)`.
     *
     */
    fun getArray(): IntArray {
        return IntArray(size) { apply(it) }
    }

    /**
     * Permutes the array by this permutation in place.
     * Let `arr2 = permute(arr)`, then `arr2[apply(i)] = arr[i]`.
     *
     * @param array the array to permute, which will be modified.
     */
    fun <T> permute(array: Array<T>): Array<T> {
        require(array.size >= size) { "The array's length ${array.size} is not enough." }
        val origin = array.clone()
        for (i in array.indices) {
            array[apply(i)] = origin[i]
        }
        return array
    }

    /**
     * Permutes the array by this permutation in place.
     * Let `arr2 = permute(arr)`, then `arr2[apply(i)] = arr[i]`.
     *
     * @param array the array to permute, which will be modified.
     * @see Permutation.permute
     */
    fun permute(array: IntArray): IntArray {
        require(array.size >= size) { "The array's length ${array.size} is not enough." }
        val origin = array.clone()
        for (i in array.indices) {
            array[apply(i)] = origin[i]
        }
        return array
    }

    /**
     * Permutes the list by this permutation and returns a new list.
     */
    fun <T> permute(list: List<T>): List<T> {
        require(list.size >= size) { "The list's length ${list.size} is not enough." }
        val newList = list.toMutableList()
        for (i in list.indices) {
            newList[apply(i)] = list[i]
        }
        return newList
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

    override fun compareTo(other: Permutation): Int {
        val comp = size - other.size
        if (comp != 0) {
            return comp
        }
        return java.lang.Long.signum(index() - other.index())
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
     * Gets an array that contains all the elements in the Cycle's order.
     * The permutation satisfies that `apply(elements[i])=elements[(i+1)%length]`.
     *
     * @return
     */
    val elements: IntArray

    val cycleLength: Int
        get() = elements.size

    /**
     * Determines whether the element should be rotated.
     *
     * @param x
     * @return
     */
    fun containsInCycle(x: Int): Boolean


    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#rank()
     */
    override fun rank(): Int {
        return cycleLength
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#reduceRotate()
     */
    override fun decompose(): List<Cycle> {
        return listOf(this)
    }


    override fun apply(x: Int): Int {
        if (!containsInCycle(x)) {
            return x
        }
        if (cycleLength == 1) {
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
    override fun permute(array: IntArray): IntArray {
        if (cycleLength == 1) {
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

//    /*
//     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(boolean[])
//     */
//    override fun permute(array: BooleanArray): BooleanArray {
//        if (length() == 1) {
//            return array
//        }
//        val earr = elements
//        val t = array[earr[0]]
//        for (i in 0 until earr.size - 1) {
//            array[earr[i]] = array[earr[i + 1]]
//        }
//        array[earr[earr.size - 1]] = t
//        return array
//    }
//
//    /*
//     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(double[])
//     */
//    override fun permute(array: DoubleArray): DoubleArray {
//        if (length() == 1) {
//            return array
//        }
//        val earr = elements
//        val t = array[earr[0]]
//        for (i in 0 until earr.size - 1) {
//            array[earr[i]] = array[earr[i + 1]]
//        }
//        array[earr[earr.size - 1]] = t
//        return array
//    }
//
//    /*
//     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(long[])
//     */
//    override fun permute(array: LongArray): LongArray {
//        if (length() == 1) {
//            return array
//        }
//        val earr = elements
//        val t = array[earr[0]]
//        for (i in 0 until earr.size - 1) {
//            array[earr[i]] = array[earr[i + 1]]
//        }
//        array[earr[earr.size - 1]] = t
//        return array
//    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(java.lang.Object[])
     */
    override fun <T> permute(array: Array<T>): Array<T> {
        if (cycleLength == 1) {
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
 * A transposition permutation is a permutation that only swap two elements.
 * By convenience, it is not strictly required that the two elements are not equal.
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

    override val cycleLength: Int
        get() = if (first == second) 1 else 2
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
    override fun containsInCycle(x: Int): Boolean {
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


//    /*
//     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(boolean[])
//     */
//    override fun permute(array: BooleanArray): BooleanArray {
//        ArraySup.swap(array, first, second)
//        return array
//    }
//
//    /*
//     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(double[])
//     */
//    override fun permute(array: DoubleArray): DoubleArray {
//        ArraySup.swap(array, first, second)
//        return array
//    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(int[])
     */
    override fun permute(array: IntArray): IntArray {
        ArraySup.swap(array, first, second)
        return array
    }

//    /*
//     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(long[])
//     */
//    override fun permute(array: LongArray): LongArray {
//        ArraySup.swap(array, first, second)
//        return array
//    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#apply(java.lang.Object[])
     */
    override fun <T> permute(array: Array<T>): Array<T> {
        ArraySup.swap(array, first, second)
        return array
    }

    /*
     * @see cn.ancono.math.numberTheory.combination.Permutation#getArray()
     */
    override fun getArray(): IntArray {
        val arr: IntArray = ArraySup.indexArray(size)
        return permute(arr)
    }
}