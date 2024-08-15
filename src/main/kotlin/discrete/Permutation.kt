/**
 * 2018-03-01
 */
package discrete

import cn.mathsymk.model.struct.Composable
import cn.mathsymk.model.struct.FiniteGroup
import cn.mathsymk.number_theory.NTFunctions
import cn.mathsymk.structure.Group
import cn.mathsymk.util.IterUtils
import function.BijectiveOperator
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
interface Permutation : BijectiveOperator<Int>, Composable<Permutation>, Comparable<Permutation> {
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
    override fun apply(x: Int): Int

    /**
     * Applies this permutation to all the elements in the array: `arr.map { x -> apply(x) }`.
     *
     * @param arr the array to apply this permutation.
     * @param inPlace whether to apply this permutation in place.
     */
    fun applyAll(arr: IntArray, inPlace: Boolean = false): IntArray {
        val result = if (inPlace) arr else IntArray(size)
        for (i in 0 until size) {
            result[i] = apply(arr[i])
        }
        return result
    }

    /**
     * Returns the index before this permutation of the element of index `y` after this permutation.
     * It is ensured that `invert(apply(n))==n`.
     *
     *
     * For example, if the permutation is (1,0,2), then `invert(1)` returns 0.
     *
     */
    override fun invert(y: Int): Int

    /**
     * Returns the inverse of this permutation.
     *
     */
    override fun inverse(): Permutation

    /**
     * Returns the index of this permutation. The index is
     * a number ranged in `[0,size! - 1]`. It represents the index of this
     * permutation in all the Permutation.of the identity size ordered by
     * the natural of their representative array. The identity permutation always
     * has the index of `0` and the total flip permutation always has the
     * index of `size! - 1`
     *
     * For example, the index of `(1,0,2)` is `2`, because all
     * 3-Permutation.are sorted as
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
     * Reduces this permutation to several elementary Permutation. The last element in the
     * list is the first permutation that should be applied. Therefore, assume the elements
     * in the list in order are `p1,p2,p3...pn`, then `p1·p2·...·pn == this`, where
     * `·` is the compose of Permutation.
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
     * Returns the count of reverse in the array representing this permutation,
     * which is the number of pairs `(i,j)` such that `i<j` and `arr[i]>arr[j]`.
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
     * Decompose this permutation to several non-intersecting rotation Permutation. The order is not
     * strictly restricted because the rotation Permutation.are commutative. The list may omit
     * rotations of length 1. Therefore, an empty list means this permutation is an identity.
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
    @Suppress("DuplicatedCode") // for non-generic types
    fun <T> permute(array: Array<T>, inPlace: Boolean = false): Array<T> {
        require(array.size >= size) { "The array's length ${array.size} is not enough." }
        val origin: Array<T> = if (inPlace) array.clone() else array
        val dest = if (inPlace) array else array.clone()
        for (i in array.indices) {
            dest[apply(i)] = origin[i]
        }
        return dest
    }

    /**
     * Permutes the array by this permutation in place.
     * Let `arr2 = permute(arr)`, then `arr2[apply(i)] = arr[i]`.
     *
     * @param array the array to permute, which will be modified.
     * @see Permutation.permute
     */
    @Suppress("DuplicatedCode") // for non-generic types
    fun permute(array: IntArray, inPlace: Boolean = false): IntArray {
        require(array.size >= size) { "The array's length ${array.size} is not enough." }
        val origin: IntArray = if (inPlace) array.clone() else array
        val dest = if (inPlace) array else array.clone()
        for (i in array.indices) {
            dest[apply(i)] = origin[i]
        }
        return dest
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

    companion object{

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
         * Returns a list of Permutation.that contains all the `n`-size Permutation.
         * The Permutation.are ordered by the [Permutation.index] of the permutation.
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
         * Composes all the Permutation.in the list: `list[0] · list[1] · ... · list[n-1]`.
         *
         * @param list a list of Permutation. not empty
         * @return the result as a permutation
         */
        fun composeAll(list: List<Permutation>): Permutation {
            require(list.isNotEmpty()) { "Empty list" }
            val size = list[0].size
            return composeAll(list, size)
        }

        /**
         * Composes all the Permutation.in the list: `list[0] · list[1] · ... · list[n-1]`.
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
                return Permutation.isEqual(x, y)
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
}
