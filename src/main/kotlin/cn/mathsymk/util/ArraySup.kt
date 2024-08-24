package cn.mathsymk.util

import java.util.*
import java.util.function.*
import java.util.function.Function
import kotlin.math.max
import kotlin.math.min


object ArraySup {
    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    const val MAX_ARRAY_SIZE: Int = Int.MAX_VALUE - 8


    @Suppress("UNCHECKED_CAST")
    fun <T> fillArr(length: Int, t: T, clazz: Class<T>?): Array<T> {
        val array = java.lang.reflect.Array.newInstance(clazz, length) as Array<T>
        Arrays.fill(array, t)
        return array
    }


    fun fillArr(length: Int, num: Int): IntArray {
        val arr = IntArray(length)
        Arrays.fill(arr, num)
        return arr
    }

    fun fillArr(length: Int, num: Char): CharArray {
        val arr = CharArray(length)
        Arrays.fill(arr, num)
        return arr
    }

    fun ranFillArr(arr: IntArray) {
        val rd = Random()
        for (i in arr.indices) {
            arr[i] = rd.nextInt()
        }
    }

    /**
     * Randomly fill the array by given random
     *
     */
    fun ranFillArr(arr: IntArray, rd: Random) {
        for (i in arr.indices) {
            arr[i] = rd.nextInt()
        }
    }

    fun ranFillArr(arr: IntArray, bound: Int) {
        val rd = Random()
        for (i in arr.indices) {
            arr[i] = rd.nextInt(bound)
        }
    }

    /**
     * Randomly fill the array by given random
     *
     * @param arr
     * @param rd
     */
    fun ranFillArr(arr: IntArray, bound: Int, rd: Random) {
        for (i in arr.indices) {
            arr[i] = rd.nextInt(bound)
        }
    }


    fun ranFillArr(arr: DoubleArray) {
        val rd = Random()
        for (i in arr.indices) {
            arr[i] = rd.nextDouble()
        }
    }

    fun ranFillArr(arr: DoubleArray, mutilplier: Double) {
        val rd = Random()
        for (i in arr.indices) {
            arr[i] = rd.nextDouble() * mutilplier
        }
    }

    /**
     * fill an array with random double
     *
     * @param arr
     * @param mutilplier
     */
    fun ranFillArrNe(arr: DoubleArray, mutilplier: Double) {
        val rd = Random()
        for (i in arr.indices) {
            var temp = rd.nextDouble()
            temp = if (rd.nextBoolean()) temp else -temp
            arr[i] = temp * mutilplier
        }
    }

    /**
     * Create an random array which length is as given.
     *
     * @param length
     * @return
     */
    fun ranArr(length: Int): IntArray {
        val arr = IntArray(length)
        ranFillArr(arr)
        return arr
    }

    /**
     * Create an array which length is as given.The random object will be
     * used to create values.
     *
     * @param length
     * @return
     */
    fun ranArr(length: Int, rd: Random): IntArray {
        val arr = IntArray(length)
        ranFillArr(arr, rd)
        return arr
    }

    /**
     * Create an array which length is as given.The values in the array are in
     * [0,bound-1].
     *
     * @param length
     * @param bound
     * @return
     */
    fun ranArr(length: Int, bound: Int): IntArray {
//		return new Random().ints(length,0,bound).toArray();
        val arr = IntArray(length)
        ranFillArr(arr, bound)
        return arr
    }

    /**
     * Create an array which length is as given.The values in the array are in
     * [0,bound-1].The random object will be
     * used to create values.
     *
     * @param length
     * @param bound
     * @return
     */
    fun ranArr(length: Int, bound: Int, rd: Random): IntArray {
        val arr = IntArray(length)
        ranFillArr(arr, bound, rd)
        return arr
    }

    /**
     * Create an array which length is as given.The values in the array are in
     * [0,bound-1].Each value are different,so if length > bound , exception will be
     * thrown.
     *
     * @param length
     * @param bound
     * @return an random array
     * @throws IllegalArgumentException if length>bound
     */
    fun ranArrNoSame(length: Int, bound: Int): IntArray {
        require(length <= bound) { "Length>bound" }
        val arr = IntArray(length)
        val rd = Random()
        for (c in 0 until length) {
            cal@ while (true) {
                val t = rd.nextInt(bound)
                //check for the identity
                for (i in 0 until c) {
                    if (arr[i] == t) continue@cal
                }
                arr[c] = t
                break
            }
        }
        return arr
    }

    /**
     * Create an array which length is as given.The values in the array are in
     * [0,bound-1].Each value are different,so if length > bound , exception will be
     * thrown. The random object will be
     * used to create values.
     *
     * @param length
     * @param bound
     * @return an random array
     * @throws IllegalArgumentException if length>bound
     */
    fun ranArrNoSame(length: Int, bound: Int, rd: Random): IntArray {
        require(length <= bound) { "Length>bound" }
        val arr = IntArray(length)
        for (c in 0 until length) {
            cal@ while (true) {
                val t = rd.nextInt(bound)
                //check for the identity
                for (i in 0 until c) {
                    if (arr[i] == t) continue@cal
                }
                arr[c] = t
                break
            }
        }
        return arr
    }

    /**
     * the ranDoubleArr is an array filled with random double number from [0,1)
     *
     * @param length
     * @return
     */
    fun ranDoubleArr(length: Int): DoubleArray {
        val arr = DoubleArray(length)
        ranFillArr(arr)
        return arr
    }

    /**
     * the ranDoubleArr is an array filled with random double number from [0,mutilplier)
     *
     * @param length
     * @param mutilplier
     * @return
     */
    fun ranDoubleArr(length: Int, mutilplier: Double): DoubleArray {
        val arr = DoubleArray(length)
        ranFillArr(arr, mutilplier)
        return arr
    }

    /**
     * return a double array with negate value
     *
     * @param length
     * @param mutilplier
     * @return
     */
    fun ranDoubleArrNe(length: Int, mutilplier: Double): DoubleArray {
        val arr = DoubleArray(length)
        ranFillArrNe(arr, mutilplier)
        return arr
    }


    fun findMaxPos(arr: IntArray): Int {
        var maxPos = 0
        for (i in 1 until arr.size) {
            maxPos = if ((arr[i] > arr[maxPos])) i else maxPos
        }
        return maxPos
    }

    fun findMax(arr: IntArray): Int {
        var max = arr[0]
        for (i in 1 until arr.size) {
            max = max(arr[i].toDouble(), max.toDouble()).toInt()
        }
        return max
    }


    /**
     * An array whose element is its index.
     *
     * @param length
     * @return
     */
    fun indexArray(length: Int): IntArray {
        return IntArray(length) { i -> i }
    }


    fun turnMatrix(mat: Array<IntArray>): Array<IntArray> {
        var width = -1
        for (arr in mat) {
            width = max(arr.size.toDouble(), width.toDouble()).toInt()
        }
        val re = Array(width) { IntArray(mat.size) }
        for (i in mat.indices) {
            for (j in mat[i].indices) {
                re[j][i] = mat[i][j]
            }
        }
        return re
    }

    /**
     * Flip an array.
     *
     * @param arr the array
     * @return a flipped array
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> filp(arr: Array<T>): Array<T> {
        val len = arr.size
        val re = java.lang.reflect.Array.newInstance(arr.javaClass.componentType, len) as Array<T>
        for (i in 0 until len) {
            re[len - i - 1] = arr[i]
        }
        return re
    }

    /**
     * Reverse the array.For example,input `reverse("123456789",3)`
     * returns a result `"456789123"`. This method will only use constant storage.
     *
     * @param arr
     * @param len
     * @return
     */
    fun <T> reverse(arr: Array<T>, len: Int) {
        require(len <= arr.size)
        reverse0(arr, len)
    }

    /**
     * Reverse the array. For example,input `reverse(123456789,3)`
     * returns a result `456789123`. This method will only use constant storage.
     *
     * @param arr
     * @param len
     */
    fun reverse(arr: IntArray, len: Int) {
        require(len <= arr.size)
        reverse0(arr, len)
    }

    /**
     * Flip the array in the given range. For example, `flip(ABCDEFG,2,6)`
     * returns `ABFEDCG`.
     *
     * @param arr  the array
     * @param from index,inclusive
     * @param to   exclusive
     */
    fun <T> flip(arr: Array<T>, from: Int, to: Int) {
        require(to > from)
        if (to == from + 1) {
            return
        }
        var t: T
        val top = from + to - 1
        val mid = (to + from) / 2
        for (i in to until mid) {
            val j = top - i
            t = arr[i]
            arr[i] = arr[j]
            arr[j] = t
        }
    }

    /**
     * Flip the array in the given range. For example, `flip(1234567,2,6)`
     * returns `1265437`.
     *
     * @param arr  the array
     * @param from index,inclusive
     * @param to   exclusive
     */
    fun flip(arr: IntArray, from: Int, to: Int): IntArray {
        require(to > from)
        if (to == from + 1) {
            return arr
        }
        var t: Int
        val top = from + to - 1
        val mid = (to + from) / 2
        for (i in from until mid) {
            val j = top - i
            t = arr[i]
            arr[i] = arr[j]
            arr[j] = t
        }
        return arr
    }

    private  fun <T> reverse0(arr: Array<T>, flipLen: Int) {
        var flipLen = flipLen
        var length = arr.size
        var start = 0
        var exchangeSize: Int
        var bigger: Boolean
        var t: T
        while (flipLen > 0) {
            val re = length - flipLen
            var place: Int

            if (flipLen < re) {
                bigger = false
                exchangeSize = flipLen
                place = re
            } else {
                bigger = true
                exchangeSize = re
                place = flipLen
            }
            for (i in start until start + exchangeSize) {
                t = arr[place + i]
                arr[place + i] = arr[i]
                arr[i] = t
            }
            length -= exchangeSize
            if (bigger) {
                start += exchangeSize
                flipLen = place - exchangeSize
            } else {
                flipLen = exchangeSize
            }
        }
    }

    private fun reverse0(arr: IntArray, flipLen: Int) {
        var flipLen = flipLen
        var length = arr.size
        var start = 0
        var exchangeSize: Int
        var bigger: Boolean
        var t: Int
        while (flipLen > 0) {
            val re = length - flipLen
            var place: Int

            if (flipLen < re) {
                bigger = false
                exchangeSize = flipLen
                place = re
            } else {
                bigger = true
                exchangeSize = re
                place = flipLen
            }
            for (i in start until start + exchangeSize) {
                t = arr[place + i]
                arr[place + i] = arr[i]
                arr[i] = t
            }
            length -= exchangeSize
            if (bigger) {
                start += exchangeSize
                flipLen = place - exchangeSize
            } else {
                flipLen = exchangeSize
            }
        }
    }

    /**
     * Determines whether the two array is equal, this method will ignore the order of
     * specific element. The length of the two array should be the identity.
     *
     * @param a1
     * @param a2
     * @param testEqual a function that test whether two given object is equal, return true if equal
     * @return
     */
    fun <T> arrayEqualNoOrder(a1: Array<T>, a2: Array<T>, testEqual: BiFunction<T, T, Boolean>): Boolean {
        val length = a1.size
        if (a2.size != length) {
            return false
        }
        val mapped = BooleanArray(length)
        for (i in 0 until length) {
            val t = a1[i]
            var suc = false
            for (j in 0 until length) {
                if (mapped[j]) continue
                if (testEqual.apply(t, a2[j])) {
                    mapped[j] = true
                    suc = true
                    break
                }
            }
            if (!suc) {
                return false
            }
        }
        return true
    }

    /**
     * Determines whether the two array is equal, this method will ignore the order of
     * specific element. The length of the two array should be the identity.(Two elements <tt>e1</tt> and
     * <tt>e2</tt> are *equal* if <tt>(e1==null ? e2==null :
     * e1.equals(e2))</tt>.)
     *
     * @param a1
     * @param a2
     * @return
     */
    fun <T> arrayEqualNoOrder(a1: Array<T>, a2: Array<T>): Boolean {
        val length = a1.size
        if (a2.size != length) {
            return false
        }
        val mapped = BooleanArray(length)
        for (t in a1) {
            var suc = false
            for (j in 0 until length) {
                if (mapped[j]) continue
                val t2: T? = a2[j]
                if (if (t == null) t2 == null else (t == t2)) {
                    mapped[j] = true
                    suc = true
                    break
                }
            }
            if (!suc) {
                return false
            }
        }
        return true
    }

//
//    /**
//     * Determines whether the array contains the specific object.
//     *
//     * @param arr
//     * @param element
//     * @param testEqual a function to determines whether the two objects are the identity.
//     * @return
//     */
//    fun <T, S> arrayContains(arr: Array<T>, element: S, testEqual: BiFunction<T, S, Boolean>): Boolean {
//        for (anArr in arr) {
//            if (testEqual.apply(anArr, element)) {
//                return true
//            }
//        }
//        return false
//    }

//    fun arrayContains(arr: IntArray, element: Int): Boolean {
//        for (t in arr) {
//            if (t == element) {
//                return true
//            }
//        }
//        return false
//    }
//
//    /**
//     * @param element
//     * @param arr
//     * @param from
//     * @param to      exclusive
//     * @return
//     */
//    fun arrayContains(element: Int, arr: IntArray, from: Int, to: Int): Boolean {
//        for (i in from until to) {
//            val t = arr[i]
//            if (t == element) {
//                return true
//            }
//        }
//        return false
//    }

//    /**
//     * Return an array of the mapped elements, the actual returned type is an array of object.
//     *
//     * @param arr
//     * @param mapper
//     * @return
//     */
//    @Suppress("UNCHECKED_CAST")
//    fun <N, T> mapTo(arr: Array<T>, mapper: Function<in T, out N>): Array<N?> {
//        val re = arrayOfNulls<Any>(arr.size) as Array<N?>
//        for (i in arr.indices) {
//            re[i] = mapper.apply(arr[i])
//        }
//        return re
//    }
//
//    /**
//     * Return an array of the mapped elements, creates a new array.
//     *
//     * @param arr
//     * @param mapper
//     * @return
//     */
//    @Suppress("UNCHECKED_CAST")
//    fun <N, T> mapTo(arr: Array<T>, mapper: Function<in T, out N>, clazz: Class<N>?): Array<N> {
//        val re = java.lang.reflect.Array.newInstance(clazz, arr.size) as Array<N>
//        for (i in arr.indices) {
//            re[i] = mapper.apply(arr[i])
//        }
//        return re
//    }
//    @Suppress("UNCHECKED_CAST")
//    fun <T> mapTo(arr: LongArray, f: LongFunction<T>, clazz: Class<T>?): Array<T> {
//        val re = java.lang.reflect.Array.newInstance(clazz, arr.size) as Array<T>
//        for (i in arr.indices) {
//            re[i] = f.apply(arr[i])
//        }
//        return re
//    }
//
//    /**
//     * Return an array of the mapped elements, creates a new array.
//     *
//     * @param arr
//     * @param mapper
//     * @return
//     */
//    @Suppress("UNCHECKED_CAST")
//    fun <N, T> mapTo2(arr: Array<Array<T>>, mapper: Function<in T, out N>, clazz: Class<N>?): Array<Array<N>> {
//        val narrayType: Class<*> = java.lang.reflect.Array.newInstance(clazz, 0).javaClass
//        val re = java.lang.reflect.Array.newInstance(narrayType, arr.size) as Array<Array<N>>
//        for (i in arr.indices) {
//            re[i] = java.lang.reflect.Array.newInstance(clazz, arr[i].size) as Array<N>
//            for (j in re[i].indices) {
//                re[i][j] = mapper.apply(arr[i][j])
//            }
//        }
//        return re
//    }
//
//    fun <T> mapTo2(arr: Array<Array<T>>, mapper: ToIntFunction<in T>): Array<IntArray?> {
//        val re = arrayOfNulls<IntArray>(arr.size)
//        for (i in arr.indices) {
//            re[i] = IntArray(arr[i].size)
//            for (j in re[i]!!.indices) {
//                re[i]!![j] = mapper.applyAsInt(arr[i][j])
//            }
//        }
//        return re
//    }
//    @Suppress("UNCHECKED_CAST")
//    fun <T> mapTo2(arr: Array<DoubleArray>, mapper: DoubleFunction<out T>, clazz: Class<T>?): Array<Array<T>> {
//        val narrayType: Class<*> = java.lang.reflect.Array.newInstance(clazz, 0).javaClass
//        val re = java.lang.reflect.Array.newInstance(narrayType, arr.size) as Array<Array<T>>
//        for (i in arr.indices) {
//            re[i] = java.lang.reflect.Array.newInstance(clazz, arr[i].size) as Array<T>
//            for (j in re[i].indices) {
//                re[i][j] = mapper.apply(arr[i][j])
//            }
//        }
//        return re
//    }
//    @Suppress("UNCHECKED_CAST")
//    fun <T> mapTo2(arr: Array<IntArray>, mapper: IntFunction<T>, clazz: Class<T>?): Array<Array<T>> {
//        val narrayType: Class<*> = java.lang.reflect.Array.newInstance(clazz, 0).javaClass
//        val re = java.lang.reflect.Array.newInstance(narrayType, arr.size) as Array<Array<T>>
//        for (i in arr.indices) {
//            re[i] = java.lang.reflect.Array.newInstance(clazz, arr[i].size) as Array<T>
//            for (j in re[i].indices) {
//                re[i][j] = mapper.apply(arr[i][j])
//            }
//        }
//        return re
//    }


    /**
     * Creates a set from the array, uses HashSet by default.
     *
     * @param arr
     * @return
     */
    fun <T> createSet(arr: Array<T>): Set<T> {
        val set: MutableSet<T> = HashSet(arr.size)
        for (i in arr.indices) {
            set.add(arr[i])
        }
        return set
    }

    /**
     * Creates a set from the array, uses the supplier to create a new set.
     *
     * @param arr
     * @param sup
     * @return
     */
    fun <T> createSet(arr: Array<T>, sup: Supplier<MutableSet<T>>): Set<T> {
        val set = sup.get()
        for (i in arr.indices) {
            set.add(arr[i])
        }
        return set
    }

//    /**
//     * Sort the `null` values to the back of the array, returns the number of non-null
//     * objects in the array. The order of the original non-null objects will not be effected
//     * but null values between them will be removed.
//     *
//     * @param objs an array to sort
//     * @return the number of non-null objects.
//     */
//    fun <T> sortNull(objs: Array<T?>): Int {
//        val temp = arrayOfNulls<Any>(objs.size)
//        var n = 0
//        for (t in objs) {
//            if (t != null) {
//                temp[n++] = t
//            }
//        }
//        for (i in objs.indices) {
//            objs[i] = temp[i] as T?
//        }
//        return n
//    }


//    /**
//     * Test that this array contains no `null` element.
//     */
//    fun <T> notEmpty(arr: Array<T>): Array<T> {
//        for (anArr in arr) {
//            if (anArr == null) {
//                throw NullPointerException()
//            }
//        }
//        return arr
//    }

    /**
     * Copies the given array.
     *
     * @param arr
     * @return
     */
    fun <T> deepCopy(arr: Array<T>): Array<T> {
        if (arr.size == 0) {
            return arr.clone()
        }
        return deepCopy0(arr) as Array<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> deepCopy0(arr: Array<T>): Array<T> {
        val result = java.lang.reflect.Array.newInstance(arr.javaClass.componentType, arr.size) as Array<Any>
        for (i in arr.indices) {
            val element = arr[i]
            if (element is Array<*> && element.isArrayOf<Any>()) result[i] = deepCopy0(arr[i] as Array<Any>)
            else if (element is ByteArray) {
                val t = arr[i] as ByteArray
                result[i] = t.clone()
            } else if (element is ShortArray) {
                val t = arr[i] as ShortArray
                result[i] = t.clone()
            } else if (element is IntArray) {
                val t = arr[i] as IntArray
                result[i] = t.clone()
            } else if (element is LongArray) {
                val t = arr[i] as LongArray
                result[i] = t.clone()
            } else if (element is CharArray) {
                val t = arr[i] as CharArray
                result[i] = t.clone()
            } else if (element is FloatArray) {
                val t = arr[i] as FloatArray
                result[i] = t.clone()
            } else if (element is DoubleArray) {
                val t = arr[i] as DoubleArray
                result[i] = t.clone()
            } else if (element is BooleanArray) {
                val t = arr[i] as BooleanArray
                result[i] = t.clone()
            } else {
                result[i] = arr[i] as Array<Any>
            }
        }
        return result as Array<T>
    }

    /**
     * Modifies all the non-null elements in the array and replace the original elements.
     *
     * @param arr
     * @return
     */
    fun <T> modifyAll(arr: Array<T>, f: Function<in T, out T>): Array<T> {
        for (i in arr.indices) {
            val t: T? = arr[i]
            if (t != null) {
                arr[i] = f.apply(t)
            }
        }
        return arr
    }

    /**
     * Set the given index to `x`, lengthen the array by 1.5x when needed.
     *
     * @param arr
     * @param x
     * @param index
     * @return
     */
    fun ensureCapacityAndAdd(arr: LongArray, x: Long, index: Int): LongArray {
        var arr = arr
        if (arr.size <= index) {
            arr = arr.copyOf(max((arr.size * 3 / 2).toDouble(), (index + 1).toDouble()).toInt())
        }
        arr[index] = x
        return arr
    }

    fun ensureCapacityAndAdd(arr: IntArray, x: Int, index: Int): IntArray {
        var arr = arr
        if (index >= arr.size) {
            arr = arr.copyOf(arr.size * 3 / 2)
        }
        arr[index] = x
        return arr
    }

    /**
     * Set the given index to `x`, lengthen the array by 1.5x when needed.
     *
     * @param arr
     * @param x
     * @param index
     * @return
     */
    fun <T> ensureCapacityAndAdd(arr: Array<T?>, x: T, index: Int): Array<T?> {
        var arr = arr
        if (arr.size <= index) {
            arr = arr.copyOf(max((arr.size * 3 / 2).toDouble(), (index + 1).toDouble()).toInt())
        }
        arr[index] = x
        return arr
    }

    /**
     * Cast the number `n` to an integer as the length of an array, checking
     * whether it exceeds. Throws an exception if `n<0 || n> MAX_ARRAY_SIZE`
     *
     * @param n
     * @return
     */
    fun castToArrayLength(n: Long): Int {
        require(!(n < 0 || n > MAX_ARRAY_SIZE)) { "Size exceeds: $n" }
        return n.toInt()
    }

    /**
     * Applies the permutation to the array.
     *
     * @param arr
     * @param parr
     * @return
     */
    fun <T> applyPermutation(arr: Array<T?>, parr: IntArray): Array<T?> {
        val copy = arr.copyOf(arr.size)
        for (i in arr.indices) {
            arr[parr[i]] = copy[i]
        }
        return arr
    }

    fun <T> swap(arr: Array<T>, i: Int, j: Int) {
        val t = arr[i]
        arr[i] = arr[j]
        arr[j] = t
    }

    fun swap(arr: IntArray, i: Int, j: Int) {
        val t = arr[i]
        arr[i] = arr[j]
        arr[j] = t
    }

    fun swap(arr: BooleanArray, i: Int, j: Int) {
        val t = arr[i]
        arr[i] = arr[j]
        arr[j] = t
    }

    fun swap(arr: LongArray, i: Int, j: Int) {
        val t = arr[i]
        arr[i] = arr[j]
        arr[j] = t
    }

    fun swap(arr: DoubleArray, i: Int, j: Int) {
        val t = arr[i]
        arr[i] = arr[j]
        arr[j] = t
    }

    fun swap(arr: FloatArray, i: Int, j: Int) {
        val t = arr[i]
        arr[i] = arr[j]
        arr[j] = t
    }


    fun firstIndexOf(x: Int, arr: IntArray): Int {
        for (i in arr.indices) {
            if (arr[i] == x) {
                return i
            }
        }
        return -1
    }

    fun <T> firstIndexOf(arr: Array<T>, matcher: Predicate<T>): Int {
        for (i in arr.indices) {
            if (matcher.test(arr[i])) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the index of the max element that is smaller or equal to target.
     * If no such element exists, -1 will be returned.
     */
    fun binarySearchFloor(arr: IntArray, lo: Int, hi: Int, target: Int): Int {
        var lo = lo
        var hi = hi
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (target < arr[mid]) {
                hi = mid
            } else {
                lo = mid + 1
            }
        }
        return lo - 1
    }

    /**
     * Returns the index of the min element that is bigger or equal to target.
     * If no such element exists, hi+1 will be returned.
     */
    fun binarySearchCeiling(arr: IntArray, lo: Int, hi: Int, target: Int): Int {
        var lo = lo
        var hi = hi
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (arr[mid] < target) {
                lo = mid + 1
            } else {
                hi = mid
            }
        }
        return lo // lo == hi
    }

    /**
     * Returns the index of the max element that is smaller or equal to target.
     * If no such element exists, -1 will be returned.
     */
    fun binarySearchFloor(arr: DoubleArray, lo: Int, hi: Int, target: Double): Int {
        var lo = lo
        var hi = hi
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (target < arr[mid]) {
                hi = mid
            } else {
                lo = mid + 1
            }
        }
        return lo - 1
    }

    /**
     * Returns the index of the min element that is bigger or equal to target.
     * If no such element exists, hi+1 will be returned.
     */
    fun binarySearchCeiling(arr: DoubleArray, lo: Int, hi: Int, target: Double): Int {
        var lo = lo
        var hi = hi
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (arr[mid] < target) {
                lo = mid + 1
            } else {
                hi = mid
            }
        }
        return lo // lo == hi
    } //	public static void main(String[] args) {
    //		Integer[] arr = new Integer[10]; 
    //		Arrays.setAll(arr, i -> i);  
    //		reverse(arr,0);
    //		print(arr);
    //		print(flip(indexArray(10),1,8));
    //	}

    /**
     * Returns a pair of arrays, the first array is the sorted array, the second array is the index of the corresponding element in the original array.
     */
    fun sortWithIndex(arr: IntArray): Pair<IntArray,IntArray> {
        val withIndex = Array(arr.size) { i -> Pair(arr[i], i) }
        withIndex.sortBy { it.first }
        val sorted = IntArray(arr.size)
        val index = IntArray(arr.size)
        for (i in withIndex.indices) {
            sorted[i] = withIndex[i].first
            index[i] = withIndex[i].second
        }
        return Pair(sorted, index)
    }

    /**
     * Compare two arrays lexicographically.
     */
    fun compareLexi(arr1: IntArray, arr2: IntArray): Int {
        val len = min(arr1.size, arr2.size)
        for (i in 0 until len) {
            if (arr1[i] < arr2[i]) {
                return -1
            } else if (arr1[i] > arr2[i]) {
                return 1
            }
        }
        return arr1.size.compareTo(arr2.size)
    }

    /**
     * Compare two arrays lexicographically.
     */
    fun <T:Comparable<T>> compareLexi(arr1: Array<T>, arr2: Array<T>): Int {
        val len = min(arr1.size, arr2.size)
        for (i in 0 until len) {
            val c = arr1[i].compareTo(arr2[i])
            if (c != 0) {
                return c
            }
        }
        return arr1.size.compareTo(arr2.size)
    }

}
