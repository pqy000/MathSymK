package cn.mathsymk.util

import java.util.*
import kotlin.collections.ArrayList

object DataStructureUtil {


    /**
     * Given a list of unordered terms, sort the terms and merge the terms with the same index using the `comparing` function.
     */
    inline fun <T> mergeRawList(
        rawList: List<T>, crossinline comparing: (T, T) -> Int,
        crossinline merger2: (T, T) -> T?,
        crossinline mergerMulti: (List<T>) -> T?,
        estimatedSize: Int = rawList.size
    ): List<T> {
        val sortedTerms = rawList.sortedWith { a, b -> comparing(a, b) }
        return mergeSorted1(sortedTerms, comparing, merger2, mergerMulti, estimatedSize)
    }

    /**
     * Merge two sorted lists.
     */
    inline fun <T> mergeSorted1(
        sortedList: List<T>,
        crossinline comparing: (T, T) -> Int,
        crossinline merger2: (T, T) -> T?,
        crossinline mergerMulti: (List<T>) -> T?,
        estimatedSize: Int = sortedList.size
    ): List<T> {
//        println("$estimatedSize, $sortedList")
        // merge terms with the same index
        val result = ArrayList<T>(estimatedSize)
        var i = 0
        while (i < sortedList.size) {
            val term = sortedList[i]
            // find next terms with the same index
            var next = i + 1
            while (next < sortedList.size && comparing(term, sortedList[next]) == 0) {
                next++
            }
            if (next == i + 1) {
                // only one term
                result.add(term)
                i = next
                continue
            }
            val sum = if (next == i + 2) {
                // two terms
                merger2(term, sortedList[i + 1])
            } else {
                // more than two terms
                mergerMulti(sortedList.subList(i, next))
            }
            if (sum != null) {
                result.add(sum)
            }
            i = next
        }
        return result
    }

    inline fun <T> mergeSorted2(
        list1: List<T>,
        list2: List<T>,
        crossinline comparing: (T, T) -> Int,
        crossinline merger2: (T, T) -> T?
    ): List<T> {
        val result = ArrayList<T>(list1.size + list2.size)
        var i = 0
        var j = 0
        while (i < list1.size && j < list2.size) {
            val ai = list1[i]
            val bj = list2[j]
            val c = comparing(ai, bj)
            if (c == 0) {
                merger2(ai, bj)?.let { result.add(it) }
                i++
                j++
            } else if (c < 0) {
                result.add(ai)
                i++
            } else {
                result.add(bj)
                j++
            }
        }
        while (i < list1.size) {
            result.add(list1[i])
            i++
        }
        while (j < list2.size) {
            result.add(list2[j])
            j++
        }
        return result
    }

    inline fun <reified T> mergeSorted2(
        list1: Array<T>,
        list2: Array<T>,
        crossinline comparing: (T, T) -> Int,
        crossinline merger2: (T, T) -> T?
    ): Array<T> {
        val result = arrayOfNulls<T>(list1.size + list2.size)
        var pos = 0
        var i = 0
        var j = 0
        while (i < list1.size && j < list2.size) {
            val ai = list1[i]
            val bj = list2[j]
            val c = comparing(ai, bj)
            if (c == 0) {
                merger2(ai, bj)?.let { result[pos++] = it }
                i++
                j++
            } else if (c < 0) {
                result[pos++] = ai
                i++
            } else {
                result[pos++] = bj
                j++
            }
        }
        val finalSize = pos + list1.size - i + list2.size - j
        val result2 = arrayOfNulls<T>(finalSize)
        System.arraycopy(result, 0, result2, 0, pos)
        if (i < list1.size) {
            System.arraycopy(list1, i, result2, pos, list1.size - i)
        } else if (j < list2.size) {
            System.arraycopy(list2, j, result2, pos, list2.size - j)
        }
        @Suppress("UNCHECKED_CAST")
        return result2 as Array<T>
    }

    data class MergeEntry<T : Comparable<T>>(val v: T, val arrayIndex: Int, val elementIndex: Int) :
        Comparable<MergeEntry<T>> {
        override fun compareTo(other: MergeEntry<T>): Int {
            return v.compareTo(other.v)
        }
    }

    /**
     * Merge multiple sorted lists.
     */
    inline fun <T : Comparable<T>> mergeSortedK(
        lists: List<List<T>>,
        crossinline merger2: (T, T) -> T?,
        crossinline mergerMulti: (List<T>) -> T?,
    ): List<T> {
        // use a PriorityQueue
        if (lists.isEmpty()) {
            return emptyList()
        }

        val result = ArrayList<T>(lists.sumOf { it.size })

        val queue = PriorityQueue<MergeEntry<T>>(lists.size)
        for (i in lists.indices) {
            if (lists[i].isNotEmpty()) {
                queue.add(MergeEntry(lists[i][0], i, 0))
            }
        }
        while (queue.isNotEmpty()) {
            val entry = queue.poll()
            val arrayIndex = entry.arrayIndex
            val elementIndex = entry.elementIndex
            val v = entry.v
            result.add(v)
            val list = lists[arrayIndex]
            if (elementIndex + 1 < list.size) {
                queue.add(MergeEntry(list[elementIndex + 1], arrayIndex, elementIndex + 1))
            }
        }
        return mergeSorted1(
            result,
            { a, b -> a.compareTo(b) },
            merger2,
            mergerMulti,
            estimatedSize = result.size / lists.size + 1 // the mean size of each list
        )
    }
}