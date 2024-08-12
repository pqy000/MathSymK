/**
 *
 */
package discrete

import util.ArraySup
import util.exceptions.NumberValueException
import java.util.*
import java.util.function.Predicate

/**
 * A ordered enumerationer which returns an
 * int array as result and supports filter.
 * @author liyicheng
 */
abstract class Enumer
internal constructor(val elementCount: Int) : Iterable<IntArray?> {
    /**
     * Returns all the int[] arrays, ordered in a proper way.
     *
     * @return
     */
    abstract fun enumeration(): List<IntArray>

    /**
     * Returns an iterator, this iterator will always return n copy of the array.
     */
    abstract override fun iterator(): MutableIterator<IntArray>

    internal class LEnumer(val toSelect : Int, m: Int, val filter: Predicate<IntArray>) : Enumer(m) {
        private var total: Long = CombUtils.permutation(toSelect, m)
//        private val toSelect:  Int
//        private val filter: Predicate<IntArray>

        /**
         *
         */
        constructor(n: Int, m: Int) : this(n, m, Predicate { a: IntArray? -> true })


        private var enumeration: MutableList<IntArray>? = null

        private fun en0(): MutableList<IntArray> {
            if (total > Int.MAX_VALUE) {
                throw NumberValueException("Too many")
            }
            val re: MutableList<IntArray> = ArrayList(total.toInt())
            en1(re, ArraySup.fillArr(elementCount, UNSELECTED), 0, BooleanArray(toSelect))
            total = re.size.toLong()
            return re
        }

        private fun en1(list: MutableList<IntArray>, cur: IntArray, pos: Int, selected: BooleanArray) {
            for (i in selected.indices) {
                if (!selected[i]) {
                    //put this
                    cur[pos] = i
                    selected[i] = true
                    if (pos == elementCount - 1) {
                        if (filter.test(cur)) {
                            list.add(cur.clone())
                        }
                    } else {
                        en1(list, cur, pos + 1, selected)
                    }
                    selected[i] = false
                    cur[pos] = UNSELECTED
                }
            }
        }


        /* (non-Javadoc)
         * @see cn.ancono.math.prob.Enumer#enumration()
         */
        override fun enumeration(): List<IntArray> {
            val list: MutableList<IntArray>
            if (enumeration != null) {
                list = ArrayList(total.toInt())
                for (arr in enumeration!!) {
                    list.add(arr.clone())
                }
            } else {
                enumeration = en0()
                list = enumeration!!
            }
            return list
        }

        /* (non-Javadoc)
         * @see cn.ancono.math.prob.Enumer#iterator()
         */
        override fun iterator(): MutableIterator<IntArray> {
            return Lit()
        }

        private inner class Lit : MutableIterator<IntArray> {
            private val en = IntArray(this.elementCount)
            private val selected = BooleanArray(toSelect)

            fun increase(): Boolean {
                var i: Int = this.elementCount - 1
                while (i > -1) {
                    // pos is equal to i in the loop
                    var pos = en[i] + 1
                    while (pos < toSelect && selected[pos]) {
                        pos++
                    }
                    selected[en[i]] = false
                    if (pos == toSelect) {
                        // need to change further
                        i--
                        continue
                    } else {
                        selected[pos] = true
                        en[i] = pos
                        i++
                        var lastPos = 0
                        while (i < this.elementCount) {
                            for (j in lastPos until toSelect) {
                                if (!selected[j]) {
                                    // put this
                                    en[i] = j
                                    selected[j] = true
                                    lastPos = j + 1
                                    break
                                }
                            }
                            i++
                        }
                        return true
                    }
                }
                return false
            }


            private var finded = false
            private var ended = false

            init {
                for (i in 0 until this.elementCount - 1) {
                    en[i] = i
                    selected[i] = true
                }
                //a trick to increase firstly
                en[this.elementCount - 1] = this.elementCount - 2
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            override fun hasNext(): Boolean {
                if (ended) {
                    return false
                }
                if (!finded) {
                    //search for it.
                    do {
                        if (!increase()) {
                            ended = true
                            return false
                        }
                    } while (filter.test(en) == false)
                    finded = true
                }
                return true
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            override fun next(): IntArray {
                if (hasNext()) {
                    val toRe = en.clone()
                    finded = false
                    return toRe
                }
                throw NoSuchElementException()
            }
        }


        /* (non-Javadoc)
         * @see cn.ancono.math.prob.Enumer#getEnumCount()
         */
        override fun getEnumCount(): Long {
            return total
        }

        companion object {
            private const val UNSELECTED = -1
        }
    }

    class EnumBuilder internal constructor(private val n: Int, private val m: Int) {
        private val rs: MutableList<Predicate<IntArray>> = LinkedList()

        fun addRule(rule: Predicate<IntArray>): EnumBuilder {
            rs.add(rule)
            return this
        }

        fun build(): Enumer {
            val rsa: Array<Predicate<IntArray>> = rs.toArray<Predicate<*>>(arrayOf<Predicate<*>>())
            return LEnumer(n, m, Predicate<IntArray> { a: IntArray ->
                for (i in rsa.indices) {
                    if (!rsa[i].test(a)) {
                        return@Predicate false
                    }
                }
                true
            })
        }
    } //	public static void main(String[] args) {
    //        combination(5,2).enumration().forEach(Printer::print);
    //	}


    companion object {
        /**
         * Returns an enumer that enumerates all the m-size permutations of n numbers.
         * @param n the count of elements
         * @param m the length of the int array returned by the enumer
         * @return
         */
        fun permutation(n: Int, m: Int): Enumer {
            return LEnumer(n, m)
        }

        /**
         * Returns an enumer that enumerates all the m-size combinations of n numbers.
         * The elements in the int array returned by the enumer represent the index of the elements and
         * are ordered.
         * @param n the count of elements
         * @param m the length of the int array returned by the enumer
         * @return
         */
        fun combination(n: Int, m: Int): Enumer {
            return CEnumer(n, m)
        }


        fun getBuilder(n: Int, m: Int): EnumBuilder {
            return EnumBuilder(n, m)
        }
    }
}
