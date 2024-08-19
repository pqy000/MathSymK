package model

import cn.mathsymk.model.struct.RingModel
import cn.mathsymk.structure.Ring
import util.ArraySup
import util.DataStructureUtil


/*
 * Created at 2018/12/12 18:49
 * @author  liyicheng
 *
 * Re-written from 2024/08/19
 */



@JvmRecord
data class CharacterPow(val ch: String, val pow: Int) : Comparable<CharacterPow> {
    override fun compareTo(other: CharacterPow): Int {
        val c = ch.compareTo(other.ch)
        return if (c != 0) c else pow - other.pow
    }
}


@JvmRecord
data class MTerm<T>(
    /**
     * A sorted array of characters and their powers.
     */
    val chs: Array<CharacterPow>,
    val c: T
) : Comparable<MTerm<T>> {

    override fun compareTo(other: MTerm<T>): Int {
        return ArraySup.compareLexi(chs, other.chs)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MTerm<*>

        if (!chs.contentEquals(other.chs)) return false
        if (c != other.c) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chs.contentHashCode()
        result = 31 * result + (c?.hashCode() ?: 0)
        return result
    }


    fun getCharPow(ch: String): Int {
        return chs.binarySearch(CharacterPow(ch, 0)).let {
            if (it < 0) 0 else chs[it].pow
        }
    }


    companion object {
        fun multiplyChars(chs1: Array<CharacterPow>, chs2: Array<CharacterPow>): Array<CharacterPow> {
            return DataStructureUtil.mergeSorted2(chs1, chs2,
                comparing = { x, y -> x.compareTo(y) },
                merger2 = { x, y -> if (x.pow + y.pow == 0) null else CharacterPow(x.ch, x.pow + y.pow) })
        }
    }

//    operator fun times(y: MTerm<T>): MTerm<T> {
//
////        val newChs = ArraySup.merge(chs, y.chs)
////        return MTerm(newChs, c * y.c)
//    }
}


class Multinomial<T : Any>
internal constructor(
    val model: Ring<T>,
    val terms: List<MTerm<T>>
) : RingModel<Multinomial<T>> {

    /*
    Basic operations
     */

    override fun isZero(): Boolean {
        return terms.isEmpty()
    }


    private inline fun mapTermsNonZeroT(transform: (MTerm<T>) -> MTerm<T>): Multinomial<T> {
        val newTerms = terms.map(transform)
        return Multinomial(model, newTerms)
    }

    private inline fun mapTermsNonZero(transform: (T) -> T): Multinomial<T> {
        return mapTermsNonZeroT { MTerm(it.chs, transform(it.c)) }
    }


    /*
    Multinomial as a ring
     */

    override fun plus(y: Multinomial<T>): Multinomial<T> {
        return addTerms2(model, terms, y.terms)
    }

    override fun unaryMinus(): Multinomial<T> {
        return mapTermsNonZero { model.negate(it) }
    }

    override fun times(y: Multinomial<T>): Multinomial<T> {
        return multiplyTerms(model, terms, y.terms)
    }


    companion object {

        private inline fun <T : Any, R : Any> mapTermsPossiblyZeroT(
            terms: List<MTerm<T>>,
            model: Ring<R>,
            transform: (MTerm<T>) -> MTerm<R>
        ): Multinomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val t2 = transform(t)
                if (model.isZero(t2.c)) null else t2
            }
            return Multinomial(model, newTerms)
        }

        private inline fun <T : Any, R : Any> mapTermsPossiblyZero(
            terms: List<MTerm<T>>,
            model: Ring<R>,
            transform: (T) -> R
        ): Multinomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val v = transform(t.c)
                if (model.isZero(v)) null else MTerm(t.chs, v)
            }
            return Multinomial(model, newTerms)
        }

        private fun <T : Any> add2Term(model: Ring<T>, a: MTerm<T>, b: MTerm<T>): MTerm<T>? {
            val r = model.add(a.c, b.c)
            return if (model.isZero(r)) null else MTerm(a.chs, r)
        }

        private fun <T : Any> addMultiTerm(model: Ring<T>, list: List<MTerm<T>>, tempList: ArrayList<T>): MTerm<T>? {
            tempList.clear()
            list.mapTo(tempList) { it.c }
            val sum = model.sum(tempList)
            return if (model.isZero(sum)) null else MTerm(list[0].chs, sum)
        }

        private fun <T : Any> mergeTerms(
            model: Ring<T>,
            rawTerms: List<MTerm<T>>,
            maxMergeSizeEst: Int = 3,
            estimatedSize: Int = rawTerms.size
        ): List<MTerm<T>> {
            val tempList = ArrayList<T>(maxMergeSizeEst)
            return DataStructureUtil.mergeRawList(
                rawTerms,
                comparing = { x, y -> x.compareTo(y) },
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) },
                estimatedSize = estimatedSize
            )
        }

        private fun <T : Any> addTerms2(model: Ring<T>, a: List<MTerm<T>>, b: List<MTerm<T>>): Multinomial<T> {
            val result = DataStructureUtil.mergeSorted2(
                a, b,
                comparing = { x, y -> x.compareTo(y) },
                merger2 = { x, y -> add2Term(model, x, y) },
            )
            return Multinomial(model, result)
        }

        private fun <T : Any> addTermsAll(model: Ring<T>, termsList: List<List<MTerm<T>>>): Multinomial<T> {
            val tempList = ArrayList<T>(termsList.size)
            val resultTerms = DataStructureUtil.mergeSortedK(
                termsList,
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) }
            )
            return Multinomial(model, resultTerms)
        }

        private fun <T : Any> multiplyTerms(model: Ring<T>, a: List<MTerm<T>>, b: List<MTerm<T>>): Multinomial<T> {
            if (a.isEmpty() || b.isEmpty()) {
                return Multinomial(model, emptyList())
            }
            val result = ArrayList<MTerm<T>>(a.size * b.size)
            for (ai in a) {
                for (bj in b) {
                    val v = model.multiply(ai.c, bj.c)
                    if (!model.isZero(v)) {
                        val newChs = MTerm.multiplyChars(ai.chs, bj.chs)
                        result.add(MTerm(newChs, v))
                    }
                }
            }
            val resTerms = mergeTerms(model, result, estimatedSize = a.size + b.size)
            return Multinomial(model, resTerms)
        }

    }
}