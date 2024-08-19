package model

import cn.mathsymk.model.struct.AlgebraModel
import cn.mathsymk.model.struct.RingModel
import cn.mathsymk.structure.Ring
import cn.mathsymk.structure.UnitRing
import util.ArraySup
import util.DataStructureUtil
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/*
 * Created at 2018/12/12 18:49
 * @author  liyicheng
 *
 * Re-written from 2024/08/19
 */



@JvmRecord
data class ChPow(val ch: String, val pow: Int) : Comparable<ChPow> {
    override fun compareTo(other: ChPow): Int {
        val c = ch.compareTo(other.ch)
        return if (c != 0) c else pow - other.pow
    }

    override fun toString(): String {
        if (pow == 1) {
            return ch
        }
        return "$ch^$pow"
    }
}

typealias TermChs = Array<ChPow>

@JvmRecord
data class MTerm<T>(
    /**
     * A sorted array of characters and their powers.
     */
    val chs: TermChs,
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
        return chs.binarySearch(ChPow(ch, 0)).let {
            if (it < 0) 0 else chs[it].pow
        }
    }

    /**
     * Normalizes the term, which means removing the characters with power 0.
     */
    fun normalize(): MTerm<T> {
        if (chs.all { it.pow != 0 }) {
            return this
        }
        return MTerm(chs.filter { it.pow != 0 }.toTypedArray(), c)
    }

    override fun toString(): String {
        return if (chs.isEmpty()) {
            c.toString()
        } else {
            val sb = StringBuilder()
            sb.append(c)
            for (ch in chs) {
                sb.append(ch)
            }
            sb.toString()
        }
    }

    companion object {
        fun multiplyChars(chs1: TermChs, chs2: TermChs): TermChs {
            return DataStructureUtil.mergeSorted2(chs1, chs2,
                comparing = { x, y -> x.compareTo(y) },
                merger2 = { x, y -> if (x.pow + y.pow == 0) null else ChPow(x.ch, x.pow + y.pow) })
        }

        private val CHAR_PATTERN =
            Pattern.compile("((?<ch1>[a-zA-Z]_?\\d*)|(\\{(?<ch2>[^}]+)}))(\\^(?<pow>[+-]?\\d+))?")


        /**
         * Parses the character part and combines it with the coefficient to build a term. The format of
         * character is:
         *
         *     character  ::= ch1 | ("{" ch2 "}")
         *     ch1        ::= [a-zA-Z]\w*
         *     ch2        ::= [^{]+
         *     power      ::= (+|-)? \d+
         *
         * `ch1` or `ch2` will be the characters stored in the resulting term.
         *
         */
        fun parseChar(chs: String): TermChs {
            val map = TreeMap<String, Int>()
            val matcher = CHAR_PATTERN.matcher(chs)
            while (matcher.lookingAt()) {
                val ch = matcher.group("ch1") ?: matcher.group("ch2")
                val pow = matcher.group("pow")?.toInt() ?: 1
                map.merge(ch, pow, Int::plus)
                matcher.region(matcher.end(), chs.length)
            }
            if (!matcher.hitEnd()) {
                throw IllegalArgumentException("Illegal format: $chs")
            }
            return map.map { (k, v) -> ChPow(k, v) }.toTypedArray()
        }

        fun <T> parse(c: T, chs: String): MTerm<T> {
            return MTerm(parseChar(chs), c)
        }
    }
}


class Multinomial<T : Any>
internal constructor(
    val model: Ring<T>,
    val terms: List<MTerm<T>>
) : AlgebraModel<T, Multinomial<T>> {


    /*
    Math object
     */
    override fun toString(): String {
        return terms.joinToString(" + ") { it.toString() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Multinomial<*>) return false

        if (model != other.model) return false
        if (terms != other.terms) return false

        return true
    }

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
    Term-wise operations
     */
    override fun times(k: T): Multinomial<T> {
        return mapTermsPossiblyZero(terms, model) { model.multiply(k, it) }
    }

    override fun div(k: T): Multinomial<T> {
        require(model is UnitRing)
        return mapTermsNonZero { model.exactDivide(it, k) }
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


        fun <T : Any> zero(model: Ring<T>): Multinomial<T> {
            return Multinomial(model, emptyList())
        }

        fun <T : Any> fromTerms(model: Ring<T>, terms: List<MTerm<T>>): Multinomial<T> {
            val filteredTerms = terms.map { it.normalize() }.filter { !model.isZero(it.c) }
            return Multinomial(model, mergeTerms(model, filteredTerms))
        }


        fun <T : Any> of(mc: Ring<T>, vararg terms: Pair<T, String>): Multinomial<T> {
            return fromTerms(mc, terms.map { MTerm.parse(it.first, it.second) })
        }

        fun <T:Any> constant(model: Ring<T>, c: T): Multinomial<T>{
            return if(model.isZero(c)){
                zero(model)
            }else{
                Multinomial(model, listOf(MTerm(emptyArray(), c)))
            }
        }

        fun <T : Any> monomial(model: Ring<T>, t: MTerm<T>): Multinomial<T> {
            if (model.isZero(t.c)) {
                return zero(model)
            }
            return Multinomial(model, listOf(t.normalize()))
        }

        fun <T : Any> monomial(model: Ring<T>, c: T, ch: String, pow: Int = 1): Multinomial<T> {
            if(model.isZero(c)){
                return zero(model)
            }
            if(pow == 0){
                return constant(model, c)
            }
            return monomial(model, MTerm(arrayOf(ChPow(ch, pow)), c))
        }


        fun <T : Any> of(mc: UnitRing<T>, builderAction: MultinomialBuilderScope<T>.() -> Multinomial<T>): Multinomial<T> {
            return MultinomialBuilderScope(mc).builderAction()
        }
    }
}

class MultinomialBuilderScope<T : Any>(val model: UnitRing<T>) {

    val x = Multinomial.monomial(model, model.one, "x")
    val y = Multinomial.monomial(model, model.one, "y")
    val z = Multinomial.monomial(model, model.one, "z")
    val one = Multinomial.constant(model, model.one)
    val zero = Multinomial.zero(model)


    operator fun T.times(chs: String): Multinomial<T> {
        val term = MTerm.parse(this, chs)
        return Multinomial(model, listOf(term))
    }

    operator fun T.times(m: Multinomial<T>): Multinomial<T> {
        return m.times(this)
    }

}